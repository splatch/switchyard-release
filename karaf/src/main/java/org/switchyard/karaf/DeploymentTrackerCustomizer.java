package org.switchyard.karaf;

import java.io.IOException;
import java.net.URL;

import org.apache.camel.impl.osgi.tracker.BundleTrackerCustomizer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.switchyard.deploy.SwitchYard;

public class DeploymentTrackerCustomizer implements BundleTrackerCustomizer {

    private static final String SWITCHYARD_XML = "META-INF/switchyard.xml";

    public Object addingBundle(Bundle bundle, BundleEvent event) {
        URL entry = bundle.getEntry(SWITCHYARD_XML);
        if (entry != null) {
            try {
                SwitchYard switchYard = new SwitchYard(entry.openStream());
                switchYard.start();
                return switchYard;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {

    }

    public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
        ((SwitchYard) object).stop();
    }

}
