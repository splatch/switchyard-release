package org.switchyard.karaf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.camel.impl.osgi.tracker.BundleTrackerCustomizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.switchyard.common.type.reflect.Construction;
import org.switchyard.deploy.Component;

public class ComponentTrackerCustomizer implements BundleTrackerCustomizer {

    private static final String COMPONENT_FILE = "META-INF/services/org.switchyard.deploy.Component";

    @SuppressWarnings("rawtypes")
    public Object addingBundle(Bundle bundle, BundleEvent event) {
        URL entry = bundle.getEntry(COMPONENT_FILE);
        if (entry != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(entry.openStream()));
                String componentClass = reader.readLine();
                Dictionary properties = new Hashtable();
                ClassLoader classLoader = bundle.adapt(BundleWiring.class).getClassLoader();
                Object component = Construction.construct(componentClass, classLoader);
                return bundle.getBundleContext().registerService(Component.class.getName(), component, properties);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {

    }

    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        ((ServiceRegistration) object).unregister();
    }

}
