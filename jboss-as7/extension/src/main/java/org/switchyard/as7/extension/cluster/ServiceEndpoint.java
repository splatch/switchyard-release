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

import javax.xml.namespace.QName;

import org.switchyard.Service;
import org.switchyard.metadata.ServiceInterface;

public class ServiceEndpoint {

    private QName _serviceName;
    private QName _domainName;
    private String _endpoint;
    private ServiceInterface _contract;
    
    public ServiceEndpoint() {
        
    }
    
    public ServiceEndpoint(QName serviceName, 
            QName domainName, 
            String endpoint, 
            ServiceInterface contract) {
        
        _serviceName = serviceName;
        _domainName = domainName;
        _endpoint = endpoint;
        _contract = contract;
    }
    
    public QName getServiceName() {
        return _serviceName;
    }
    
    public ServiceEndpoint setServiceName(QName serviceName) {
        _serviceName = serviceName;
        return this;
    }
    
    public QName getDomainName() {
        return _domainName;
    }
    
    public ServiceEndpoint setDomainName(QName domainName) {
        _domainName = domainName;
        return this;
    }
    
    public String getEndpoint() {
        return _endpoint;
    }
    
    public ServiceEndpoint setEndpoint(String endpoint) {
        _endpoint = endpoint;
        return this;
    }
    
    public ServiceInterface getContract() {
        return _contract;
    }
    
    public ServiceEndpoint setContract(ServiceInterface contract) {
        _contract = contract;
        return this;
    }
    
    public static ServiceEndpoint fromService(Service service) {
        return new ServiceEndpoint(service.getName(), 
                service.getDomain().getName(), 
                null, 
                RemoteInterface.fromInterface(service.getInterface()));
    }
    
    /*
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (_serviceName != null) {
            sb.append(_serviceName);
        }
        sb.append("|");
        if (_domainName != null) {
            sb.append(_domainName);
        }
        sb.append("|");
        if (_endpoint != null) {
            sb.append(_endpoint);
        }
        sb.append("|");
        if (_contract != null) {
            sb.append(_contract);
        }
        
        return sb.append("]").toString();
    }
    
    public static ServiceEndpoint fromString(final String endpointString) {
        if (!endpointString.startsWith("[") || !endpointString.endsWith("]")) {
            throw new IllegalArgumentException("Endpoint must start and end with '[ ]' : " + endpointString);
        }
        
        // peel off the brackets and split fields
        String[] fields = endpointString.substring(1, endpointString.length() - 1).split("\\|");
        ServiceEndpoint endpoint = new ServiceEndpoint();
        
        // this seems a tad inelegant
        if (fields.length > 0 && !fields[0].trim().isEmpty()) {
            endpoint.setServiceName(QName.valueOf(fields[0]));
        }
        if (fields.length > 1 && !fields[1].trim().isEmpty()) {
            endpoint.setDomainName(QName.valueOf(fields[1]));
        }
        if (fields.length > 2 && !fields[2].trim().isEmpty()) {
            endpoint.setEndpoint(fields[2]);
        }
        
        return endpoint;
    }
    */
}
