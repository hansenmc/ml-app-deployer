package com.marklogic.appdeployer.command.data;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.DataConfig;
import com.marklogic.appdeployer.command.restapis.DeployRestApiServersCommand;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.DocumentMetadataHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LoadDataTest extends AbstractAppDeployerTest {

	@AfterEach
	public void teardown() {
		undeploySampleApp();
	}

	/**
	 * The tests in ml-javaclient-util verify that all the features of GenericFileLoader work correctly, so this test
	 * just does some basic assertions to make sure all is well.
	 */
	@Test
	public void multiplePaths() {
		DataConfig dataConfig = appConfig.getDataConfig();
		dataConfig.getDataPaths().add(new File(dataConfig.getProjectDir(), "src/main/more-data").getAbsolutePath());

		initializeAppDeployer(new DeployRestApiServersCommand(), new LoadDataCommand());
		appConfig.getCustomTokens().put("%%TOKEN_TEST%%", "this was replaced");
		appConfig.getCustomTokens().put("%%roleName%%", "manage-user");
		deploySampleApp();

		DatabaseClient client = appConfig.newDatabaseClient();
		GenericDocumentManager mgr = client.newDocumentManager();

		assertNotNull(mgr.exists("/test1.json"));
		String json = mgr.readAs("/test1.json", String.class);
		assertEquals("{\"hello\":\"this was replaced\"}", json);

		assertNotNull(mgr.exists("/test4.json"), "This should be loaded from the additional data path");

		assertNull(mgr.exists("/.DS_Store/shouldBeIgnored.json"),
			"Files starting with a . or in a directory starting with a . should not be loaded by default");

		DocumentMetadataHandle metadata = mgr.readMetadata("/child/test2.xml", new DocumentMetadataHandle());
		assertTrue(metadata.getCollections().contains("xml-data"));
		assertFalse(metadata.getCollections().contains("text-data"));

		metadata = mgr.readMetadata("/child/test3.txt", new DocumentMetadataHandle());
		assertFalse(metadata.getCollections().contains("xml-data"));
		assertTrue(metadata.getCollections().contains("text-data"));
		DocumentMetadataHandle.DocumentPermissions permissions = metadata.getPermissions();
		Set<DocumentMetadataHandle.Capability> capabilities = permissions.get("manage-user");
		assertTrue(capabilities.contains(DocumentMetadataHandle.Capability.READ));
		assertTrue(capabilities.contains(DocumentMetadataHandle.Capability.UPDATE));

		assertNull(mgr.exists("/child/collections.properties"));
		assertNull(mgr.exists("/child/permissions.properties"));
	}

	@Test
	public void databaseNameIsSet() {
		LoadDataCommand command = new LoadDataCommand();
		DatabaseClient client = command.determineDatabaseClient(appConfig);
		assertNull(client.getDatabase(), "The database property isn't set on a DatabaseClient when no value is provided when the " +
			"DatabaseClient is constructed");
		client.release();

		appConfig.getDataConfig().setDatabaseName("Documents");
		client = command.determineDatabaseClient(appConfig);
		assertEquals("Documents", client.getDatabase());
		assertEquals(appConfig.getAppServicesPort(), client.getPort());
		client.release();
	}
}
