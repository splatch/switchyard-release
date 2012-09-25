/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.switchyard.as7.extension.cluster;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.switchyard.ExchangeHandler;
import org.switchyard.Service;
import org.switchyard.internal.DefaultServiceRegistry;
import org.switchyard.internal.ServiceImpl;
import org.switchyard.internal.io.JSONProtostuffSerializer;
import org.switchyard.internal.io.Serializer;
import org.switchyard.spi.ServiceRegistry;

public class DistributedServiceRegistry implements ServiceRegistry {

    private static Logger _log = Logger.getLogger(DistributedServiceRegistry.class);
    
    private String _localAddress;
    private DefaultServiceRegistry _localServices;
    private Cache<QName, List<String>> _remoteServices;
    private Serializer _serializer;
    
    public DistributedServiceRegistry(Cache<QName, List<String>> cache, String localAddress) {
        _localServices = new DefaultServiceRegistry();
        _localAddress = localAddress;
        _remoteServices = cache;
        _serializer = new JSONProtostuffSerializer();
    }

    @Override
    public Service registerService(Service service) {
        // add to local registry
        _localServices.registerService(service);
        
        // only add to remote registry if service is promoted
        if (service.getProviderMetadata().isImplementation()) {
            try {
                addRemoteService(service);
            } catch (java.io.IOException ioEx) {
                _log.warn("Failed to add service " + service.getName() + " to remote registry.", ioEx);
            }
        }
        
        return service;
    }

    @Override
    public void unregisterService(Service service) {
        _localServices.unregisterService(service);
        List<String> remotes = _remoteServices.get(service.getName());
        remotes.remove(service.getName());
        _remoteServices.put(service.getName(), remotes);
    }

    @Override
    public List<Service> getServices() {
        List<Service> services = new LinkedList<Service>();
        services.addAll(_localServices.getServices());
        // Add logic to query remotes here - entrySet on a cache could get ugly
        
        return services;
    }

    @Override
    public List<Service> getServices(QName serviceName) {
        List<Service> services = new LinkedList<Service>();
        // locals appear first in the list
        services.addAll(_localServices.getServices(serviceName));
        // add remotes and prune the local entry
        List<String> remotes = _remoteServices.get(serviceName);
        if (remotes != null) {
            for (String remote : remotes) {
                try {
                    ServiceEndpoint ep = _serializer.deserialize(remote.getBytes(), ServiceEndpoint.class);
                    if (!_localAddress.equals(ep.getEndpoint())) {
                        ExchangeHandler handler = new ClusteredServiceHandler(ep.getEndpoint());
                        services.add(new ServiceImpl(ep.getServiceName(), ep.getContract(), null, handler));
                    }
                } catch (java.io.IOException ioEx) {
                    _log.warn("Failed to deserialize remote endpoint: " + remote, ioEx);
                }
            }
        }
        return services;
    }
    
    private void addRemoteService(Service service) throws java.io.IOException {
        List<String> serviceList = fetchRemoteServiceList(service.getName());
        
        // strictly speaking, this should never be null
        if (serviceList != null) {
            // this really needs to be transacted or we're gonna lose entries
            // with concurrent updates
            for (String epStr : serviceList) {
                ServiceEndpoint ep = _serializer.deserialize(epStr.getBytes(), ServiceEndpoint.class);
                if (_localAddress.equals(ep.getEndpoint())) {
                    // don't add a duplicate entry to the list
                    return;
                }
            }
            // add the endpoint
            ServiceEndpoint newEp = ServiceEndpoint.fromService(service).setEndpoint(_localAddress);
            serviceList.add(new String(_serializer.serialize(newEp, ServiceEndpoint.class)));
            _remoteServices.put(service.getName(), serviceList);
        } else {
            _log.warn("Failed to add service " + service.getName() + " to the distributed registry: "
                    + "fetch of service list returned null.");
        }
    }
    
    private List<String> fetchRemoteServiceList(QName serviceName) {
        // defensive putIfAbsent just in case the entry doesn't exist yet
        _remoteServices.putIfAbsent(serviceName, new LinkedList<String>());
        return _remoteServices.get(serviceName);
    }
}
