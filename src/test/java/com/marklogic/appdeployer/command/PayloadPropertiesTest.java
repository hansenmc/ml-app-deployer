package com.marklogic.appdeployer.command;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.databases.DeployOtherDatabasesCommand;
import com.marklogic.mgmt.resource.databases.DatabaseManager;
import com.marklogic.rest.util.Fragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class PayloadPropertiesTest extends AbstractAppDeployerTest {

	@AfterEach
	public void teardown() {
		undeploySampleApp();
	}

	@Test
	public void testExcludeProperties() {
		appConfig.getFirstConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		appConfig.setExcludeProperties(new String[] {"triggers-database"});

		initializeAppDeployer(new DeployOtherDatabasesCommand(1));
		appDeployer.deploy(appConfig);

		DatabaseManager dbMgr = new DatabaseManager(this.manageClient);

		appDeployer.deploy(appConfig);

		Fragment db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());
		assertEquals("false", db.getElementValue("//m:triple-index"));
		assertNull(db.getElementValue("//m:triggers-database"));
	}

	@Test
	public void testIncludeProperties() {
		appConfig.getFirstConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		appConfig.setExcludeProperties(new String[] {"triple-index"});

		initializeAppDeployer(new DeployOtherDatabasesCommand(1));
		appDeployer.deploy(appConfig);

		DatabaseManager dbMgr = new DatabaseManager(this.manageClient);
		Fragment db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());

		assertEquals("true", db.getElementValue("//m:triple-index"));
		assertEquals("Triggers", db.getElementValue("//m:triggers-database"));

		appConfig.setExcludeProperties((String[])null);
		appConfig.setIncludeProperties(new String[] {"triple-index", "database-name"});
		appDeployer.deploy(appConfig);

		db = dbMgr.getPropertiesAsXml(appConfig.getContentDatabaseName());
		assertEquals("false", db.getElementValue("//m:triple-index"));
	}

	@Test
	public void testException() {
		appConfig.getFirstConfigDir().setBaseDir(new File("src/test/resources/payload-properties-test/json/ml-config"));
		appConfig.setExcludeProperties(new String[]{"triple-index"});
		try {
			appConfig.setIncludeProperties(new String[]{"triggers-database"});
		} catch (IllegalStateException e) {
			assertNotNull(e);
		}
	}

}
