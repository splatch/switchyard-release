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

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;

import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.Loader;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.InstanceManager;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.web.deployment.WebCtxLoader;
import org.jboss.logging.Logger;
import org.switchyard.as7.extension.util.ServerUtil;
import org.switchyard.deploy.ServiceDomainManager;

/**
 * Publishes standalone HTTP endpoint.
 */
public class RemoteEndpointListener {

    private static final String SERVER_TEMP_DIR = System.getProperty(ServerEnvironment.SERVER_TEMP_DIR);
    private static final String SERVLET_NAME = "SwitchYardRemotingServlet";
    
    private static Logger _log = Logger.getLogger(RemoteEndpointListener.class);
    
    private String _contextName;
    private StandardContext _serverContext;
    private ServiceDomainManager _domainManager;
    
    public RemoteEndpointListener(String context, ServiceDomainManager domainManager) {
        _contextName = context;
        _domainManager = domainManager;
    }

    public void start() throws Exception {
        
        Host host = ServerUtil.getDefaultHost().getHost();
        _serverContext = (StandardContext) host.findChild("/" + _contextName);
        if (_serverContext == null) {
            _serverContext = new StandardContext();
            _serverContext.setPath("/" + _contextName);
            File docBase = new File(SERVER_TEMP_DIR, _contextName);
            if (!docBase.exists()) {
                if (!docBase.mkdirs()) {
                    throw new RuntimeException("Unable to create temp directory " + docBase.getPath());
                }
            }
            _serverContext.setDocBase(docBase.getPath());
            _serverContext.addLifecycleListener(new ContextConfig());

            final Loader loader = new WebCtxLoader(Thread.currentThread().getContextClassLoader());
            loader.setContainer(host);
            _serverContext.setLoader(loader);
            _serverContext.setInstanceManager(new LocalInstanceManager());

            Wrapper wrapper = _serverContext.createWrapper();
            wrapper.setName(SERVLET_NAME);
            wrapper.setServletClass(SwitchYardRemotingServlet.class.getName());
            wrapper.setLoadOnStartup(1);
            _serverContext.addChild(wrapper);
            _serverContext.addServletMapping("/*", SERVLET_NAME);
            

            host.addChild(_serverContext);
            _serverContext.create();
            _serverContext.start();
            

            SwitchYardRemotingServlet remotingServlet = (SwitchYardRemotingServlet) wrapper.getServlet();
            remotingServlet.setServiceDomainManager(_domainManager);
            _log.info("Published Remote Service Endpoint " + _serverContext.getPath());
        } else {
            throw new RuntimeException("Context " + _contextName + " already exists!");
        }
    }
    
    public void stop() {
        if (_serverContext != null) {
            // Destroy the web context unless if it is default
            if (!_serverContext.getPath().equals("/")) {
                try {
                    Container container = _serverContext.getParent();
                    container.removeChild(_serverContext);
                    _serverContext.stop();
                    _serverContext.destroy();
                    _log.info("Destroyed HTTP context " + _serverContext.getPath());
                } catch (Exception e) {
                    _log.error("Unable to destroy web context", e);
                }
            }
        }
    }
    
    public String getAddress() {
        Connector connector = ServerUtil.getDefaultConnector();
        String address = connector.getScheme() + "://" 
                + ServerUtil.getDefaultHost().getHost().findAliases()[0] 
                + ":" + connector.getPort() + "/" + _contextName;
        return address;
    }

    private static class LocalInstanceManager implements InstanceManager {
        LocalInstanceManager() {
        }
        @Override
        public Object newInstance(String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
            return Class.forName(className).newInstance();
        }

        @Override
        public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
            return Class.forName(fqcn, false, classLoader).newInstance();
        }

        @Override
        public Object newInstance(Class<?> c) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException {
            return c.newInstance();
        }

        @Override
        public void newInstance(Object o) throws IllegalAccessException, InvocationTargetException, NamingException {
            throw new IllegalStateException();
        }

        @Override
        public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        }
    }
}
