package com.marklogic.appdeployer.command.databases;

import org.junit.Test;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.rest.mgmt.databases.DatabaseManager;
import com.marklogic.rest.mgmt.forests.ForestManager;

public class CreateTriggersDatabaseTest extends AbstractAppDeployerTest {

    @Test
    public void createAndDelete() {
        initializeAppDeployer(new CreateTriggersDatabaseCommand());

        appDeployer.deploy(appConfig);

        DatabaseManager dbMgr = new DatabaseManager(manageClient);
        ForestManager forestMgr = new ForestManager(manageClient);

        String dbName = "sample-app-triggers";
        String forestName = dbName + "-1";

        try {
            assertTrue("The triggers database should have been created", dbMgr.exists(dbName));
            assertTrue("A forest for the triggers database should have been created",
                    forestMgr.forestExists(forestName));
            assertTrue("The forest should be attached", forestMgr.isForestAttached(forestName));
        } finally {
            undeploySampleApp();
            assertFalse("The triggers database should have been deleted", dbMgr.exists(dbName));
            assertFalse("The triggers forest should have been deleted", forestMgr.forestExists(forestName));
        }
    }
}
