package com.marklogic.appdeployer.app;

import java.util.List;

import com.marklogic.appdeployer.mgmt.ManageClient;
import com.marklogic.appdeployer.mgmt.admin.AdminManager;

/**
 * Simple implementation that allows for a list of plugins to be set. Useful for testing purposes in particular - i.e.
 * for testing plugins together.
 */
public class SimpleAppManager extends AbstractAppManager {

    private List<AppPlugin> appPlugins;

    public SimpleAppManager(ManageClient manageClient, AdminManager adminManager) {
        super(manageClient, adminManager);
    }

    @Override
    protected List<AppPlugin> getAppPlugins() {
        return appPlugins;
    }

    public void setAppPlugins(List<AppPlugin> appPlugins) {
        this.appPlugins = appPlugins;
    }

}
