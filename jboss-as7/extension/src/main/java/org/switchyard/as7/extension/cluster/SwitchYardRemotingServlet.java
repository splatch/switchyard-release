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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.ServiceReference;
import org.switchyard.common.type.Classes;
import org.switchyard.deploy.ServiceDomainManager;
import org.switchyard.deploy.internal.Deployment;
import org.switchyard.internal.io.JSONProtostuffSerializer;
import org.switchyard.internal.io.Serializer;

/**
 * 
 */
public class SwitchYardRemotingServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static Logger _log = Logger.getLogger(SwitchYardRemotingServlet.class);
    
    private Serializer _serializer = new JSONProtostuffSerializer();
    private ServiceDomainManager _domainManager;

    /**
     * {@inheritDoc}
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    /**
     * {@inheritDoc}
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handle(request, response);
    }

    
    public void handle(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        System.out.println("=====-->> Message Received");
        RemoteMessage msg = _serializer.deserialize(request.getInputStream(), RemoteMessage.class);
        System.out.println("message content -> " + msg.getContent());
        
        ServiceDomain domain = _domainManager.getDomain(msg.getDomain());
        ClassLoader loader = (ClassLoader) domain.getProperties().get(Deployment.CLASSLOADER_PROPERTY);
        ClassLoader setTCCL = Classes.setTCCL(loader);
        try {
            ServiceReference service = domain.getServiceReference(msg.getService());
            Exchange ex = service.createExchange();
            Message m = ex.createMessage();
            ex.getContext().setProperties(msg.getContext().getProperties());
            m.setContent(msg.getContent());
            ex.send(m);
        } finally {
            Classes.setTCCL(setTCCL);
        }
    }
    
    public void setServiceDomainManager(ServiceDomainManager domainManager) {
        _domainManager = domainManager;
    }
}
