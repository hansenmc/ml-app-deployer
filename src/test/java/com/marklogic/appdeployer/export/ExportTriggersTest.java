package com.marklogic.appdeployer.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.appdeployer.command.databases.DeployContentDatabasesCommand;
import com.marklogic.appdeployer.command.databases.DeploySchemasDatabaseCommand;
import com.marklogic.appdeployer.command.databases.DeployTriggersDatabaseCommand;
import com.marklogic.appdeployer.command.triggers.DeployTriggersCommand;
import com.marklogic.mgmt.selector.PropertiesResourceSelector;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

public class ExportTriggersTest extends AbstractExportTest {

	@After
	public void teardown() {
		undeploySampleApp();
	}

	@Test
	public void test() throws Exception {
		initializeAppDeployer(new DeploySchemasDatabaseCommand(), new DeployTriggersDatabaseCommand(),
			new DeployContentDatabasesCommand(1), new DeployTriggersCommand());
		appDeployer.deploy(appConfig);

		Properties props = new Properties();
		props.setProperty("triggers", "my-trigger");

		ExportedResources resources = new Exporter(manageClient)
			.withTriggersDatabase(appConfig.getTriggersDatabaseName())
			.select(new PropertiesResourceSelector(props))
			.export(exportDir);

		assertEquals(1, resources.getMessages().size());
		assertEquals("Each exported trigger has the 'id' field removed from it, as that field should be generated by MarkLogic.",
			resources.getMessages().get(0));

		assertEquals(1, resources.getFiles().size());

		File triggerFile = new File(exportDir, "triggers/my-trigger.json");
		JsonNode json = objectMapper.readTree(triggerFile);
		assertNull(json.get("id"));
		assertEquals("my-trigger", json.get("name").asText());
	}
}
