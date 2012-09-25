package org.switchyard.as7.extension.cluster;

import javax.xml.namespace.QName;

import org.switchyard.Context;

public class RemoteMessage {

    private Context _context;
    private Object _content;
    private QName _service;
    private QName _domain;
    private String _operation;
    
    public RemoteMessage() {
        
    }
    
    public RemoteMessage(QName domain, QName service) {
        _domain = domain;
        _service = service;
    }
    
    public Context getContext() {
        return _context;
    }
    
    public RemoteMessage setContext(Context context) {
        _context = context;
        return this;
    }
    
    public Object getContent() {
        return _content;
    }
    
    public RemoteMessage setContent(Object content) {
        _content = content;
        return this;
    }
    
    public QName getService() {
        return _service;
    }
    
    public RemoteMessage setService(QName service) {
        _service = service;
        return this;
    }
    
    public QName getDomain() {
        return _domain;
    }
    
    public RemoteMessage setDomain(QName domain) {
        _domain = domain;
        return this;
    }
    
    public String getOperation() {
        return _operation;
    }
    
    public RemoteMessage setOperation(String operation) {
        _operation = operation;
        return this;
    }
    
    
}
