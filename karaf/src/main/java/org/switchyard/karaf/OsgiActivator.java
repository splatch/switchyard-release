package org.switchyard.karaf;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.impl.osgi.tracker.BundleTracker;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class OsgiActivator implements BundleActivator {

    List<BundleTracker> bundleTrackers = new ArrayList<BundleTracker>();

    @Override
    public void start(BundleContext context) throws Exception {
//        bundleTrackers.add(new BundleTracker(context, Bundle.ACTIVE, new ComponentTrackerCustomizer()));
        bundleTrackers.add(new BundleTracker(context, Bundle.ACTIVE, new DeploymentTrackerCustomizer()));

        for (BundleTracker tracker : bundleTrackers) {
            tracker.open();
        }
    
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        for (BundleTracker tracker : bundleTrackers) {
            tracker.close();
        }
    }

}
