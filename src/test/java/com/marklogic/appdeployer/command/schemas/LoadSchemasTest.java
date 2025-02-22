package com.marklogic.appdeployer.command.schemas;

import com.marklogic.appdeployer.AbstractAppDeployerTest;
import com.marklogic.appdeployer.command.Command;
import com.marklogic.appdeployer.command.databases.DeployOtherDatabasesCommand;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.document.GenericDocumentManager;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileFilter;

import static org.junit.jupiter.api.Assertions.*;

public class LoadSchemasTest extends AbstractAppDeployerTest {

	@AfterEach
	public void cleanup() {
		undeploySampleApp();
	}

	@Test
	public void databaseSpecificPaths() {
		initializeAppDeployer(new DeployOtherDatabasesCommand(), newCommand());

		File configDir = new File("src/test/resources/sample-app/multiple-schema-databases/ml-config");
		appConfig.getFirstConfigDir().setBaseDir(configDir);
		appConfig.setSchemasDatabaseName("sample-app-schemas1");
		appConfig.getSchemaPaths().clear();
		appConfig.getSchemaPaths().add("src/test/resources/sample-app/multiple-schema-databases/ml-schemas");

		// Create an empty schemas dir to ensure this doesn't cause any failures
		File emptyDatabaseDir = new File(appConfig.getFirstConfigDir().getDatabasesDir(), "doesnt-exist-db");
		File emptySchemasDir = new File(emptyDatabaseDir, "schemas");
		emptySchemasDir.mkdirs();

		deploySampleApp();

		DatabaseClient client = appConfig.newSchemasDatabaseClient();
		GenericDocumentManager mgr = client.newDocumentManager();
		assertNotNull(mgr.exists("/default-schema.xsd"));
		assertNotNull(mgr.exists("/schema1.xsd"));
		assertNull(mgr.exists("/schema2.xsd"));
		client.release();

		client = appConfig.newAppServicesDatabaseClient("sample-app-schemas2");
		mgr = client.newDocumentManager();
		assertNull(mgr.exists("/default-schema.xsd"));
		assertNull(mgr.exists("/schema1.xsd"));
		assertNotNull(mgr.exists("/schema2.xsd"));
		client.release();
	}

	@Test
	public void testSchemaLoading() {
		initializeAppDeployer(new DeployOtherDatabasesCommand(1), newCommand());

		appConfig.getCustomTokens().put("%%replaceMe%%", "world");
		appDeployer.deploy(appConfig);

		DatabaseClient client = appConfig.newSchemasDatabaseClient();

		GenericDocumentManager docMgr = client.newDocumentManager();

		assertNull(docMgr.exists("notExists"), "Rules document loaded");
		assertNotNull(docMgr.exists("/my.rules").getUri(), "Rules document loaded");
		assertNotNull(docMgr.exists("/x.xsd").getUri(), "XSD document loaded");
		assertNull(docMgr.exists("/.do-not-load"));
		assertNull(docMgr.exists(".do-not-load"));

		String content = new String(docMgr.read("/x.xsd", new BytesHandle()).get());
		assertTrue(content.contains("<hello>world</hello>"),
			"Tokens should be replaced when schemas are loaded; actual content: " + content);
	}

	@Test
	public void testCustomSchemasPathWithCustomFileFilter() {
		initializeAppDeployer(new DeployOtherDatabasesCommand(1), newCommand());

		appConfig.getSchemaPaths().clear();
		appConfig.getSchemaPaths().add("src/test/resources/schemas-marklogic9");
		appConfig.setSchemasFileFilter(new CustomFileFilter());
		appConfig.setTdeValidationEnabled(false);
		appDeployer.deploy(appConfig);

		DatabaseClient client = appConfig.newSchemasDatabaseClient();

		GenericDocumentManager docMgr = client.newDocumentManager();

		assertNotNull(docMgr.exists("/x.tdex").getUri(), "TDEXML document loaded");
		assertNotNull(docMgr.exists("/x.tdej").getUri(), "TDEJSON document loaded");
		assertNull(docMgr.exists("/to-be-ignored/test.xml"));
		assertNull(docMgr.exists("to-be-ignored/test.xml"));

		for (String uri : new String[]{"/x.tdex", "/x.tdej"}) {
			DocumentMetadataHandle h = docMgr.readMetadata(uri, new DocumentMetadataHandle());
			assertEquals("http://marklogic.com/xdmp/tde", h.getCollections().iterator().next(),
				"Files ending in tdex and tdej go into a special collection");
		}
	}

	@Test
	public void nullSchemaPath() {
		initializeAppDeployer(newCommand());
		appConfig.setSchemaPaths(null);
		deploySampleApp();
		logger.info("Verifies that no error occurs when the schemas path is null");
	}

	@Test
	public void tdeValidationEnabled() {
		initializeAppDeployer(new DeployOtherDatabasesCommand(1), newCommand());
		appConfig.getFirstConfigDir().setBaseDir(new File("src/test/resources/sample-app/tde-validation"));
		try {
			deploySampleApp();
			fail("The deploy should have failed because of a bad TDE template");
		} catch (Exception ex) {
			String message = ex.getCause().getMessage();
			assertTrue(message.startsWith("TDE template failed validation"));
			assertTrue(message.contains("TDE-REPEATEDCOLUMN"));
		}
	}

	@Test
	public void multipleSchemaPaths() {
		File projectDir = new File("src/test/resources/schemas-project");

		initializeAppConfig(projectDir);
		appConfig.getSchemaPaths().add(new File(projectDir, "src/main/more-schemas").getAbsolutePath());

		initializeAppDeployer(new DeployOtherDatabasesCommand(1), new LoadSchemasCommand());
		deploySampleApp();

		DatabaseClient client = appConfig.newSchemasDatabaseClient();
		GenericDocumentManager docMgr = client.newDocumentManager();
		assertNotNull(docMgr.exists("/tde/template1.json"));
		assertNotNull(docMgr.exists("/tde/template2.json"));

		assertTrue(docMgr.readMetadata("/tde/template1.json", new DocumentMetadataHandle()).getCollections().contains("http://marklogic.com/xdmp/tde"));
		assertTrue(docMgr.readMetadata("/tde/template2.json", new DocumentMetadataHandle()).getCollections().contains("http://marklogic.com/xdmp/tde"));
	}

	private Command newCommand() {
		return new LoadSchemasCommand();
	}

}

class CustomFileFilter implements FileFilter {
	@Override
	public boolean accept(File pathname) {
		return !(pathname.isDirectory() && "to-be-ignored".equals(pathname.getName()));
	}
}
