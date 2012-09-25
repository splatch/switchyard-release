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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.internal.DefaultMessage;
import org.switchyard.internal.io.JSONProtostuffSerializer;
import org.switchyard.internal.io.Serializer;

public class ClusteredServiceHandler extends BaseHandler {
    
    private URL _endpoint;
    private Serializer _serializer;

    public ClusteredServiceHandler(String address) {
        _serializer = new JSONProtostuffSerializer();
        try {
            _endpoint = new URL(address);
        } catch (MalformedURLException badURL) {
            throw new IllegalArgumentException("Invalid URL for remote endpoint: " + address, badURL);
        }
    }
    
    @Override
    public void handleMessage(Exchange exchange) throws HandlerException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)_endpoint.openConnection();
            conn.setDoOutput(true);
            conn.connect();
            OutputStream os = conn.getOutputStream();
            RemoteMessage msg = new RemoteMessage()
                //.setDomain(exchange.getProvider().getDomain().getName())
                .setService(exchange.getProvider().getName())
                .setContent(exchange.getMessage().getContent())
                .setContext(exchange.getContext());
            _serializer.serialize(msg, RemoteMessage.class, os);
            os.flush();
            os.close();
            System.out.println("response code => " + conn.getResponseCode());
            
        } catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
            throw new HandlerException("Failed to connect to remote service endpoint for " 
                    + exchange.getProvider().getName(), ioEx);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        
    }
}
