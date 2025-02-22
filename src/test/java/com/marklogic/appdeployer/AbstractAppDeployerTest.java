package com.marklogic.appdeployer;

import com.marklogic.appdeployer.command.Command;
import com.marklogic.appdeployer.command.modules.DefaultModulesLoaderFactory;
import com.marklogic.appdeployer.command.modules.LoadModulesCommand;
import com.marklogic.appdeployer.impl.SimpleAppDeployer;
import com.marklogic.client.ext.modulesloader.impl.DefaultModulesLoader;
import com.marklogic.mgmt.AbstractMgmtTest;
import com.marklogic.xcc.template.XccTemplate;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;

/**
 * Base class for tests that depend on an AppDeployer instance. You can extend this directly to write a test for a
 * particular resource, but check out AbstractManageResourceTest (and its subclasses) to see if that will work for you
 * instead, as that saves a lot of work.
 */
public abstract class AbstractAppDeployerTest extends AbstractMgmtTest {

    public final static String SAMPLE_APP_NAME = "sample-app";

    protected final static Integer SAMPLE_APP_REST_PORT = 8540;
    protected final static Integer SAMPLE_APP_TEST_REST_PORT = 8541;

    // Intended to be used by subclasses
    protected AppDeployer appDeployer;
    protected AppConfig appConfig;

    @BeforeEach
    public void initialize() {
        initializeAppConfig();
    }

    protected void initializeAppConfig() {
    	initializeAppConfig(new File("src/test/resources/sample-app"));
    }

    protected void initializeAppConfig(File projectDir) {
	    appConfig = new AppConfig(projectDir);

	    appConfig.setName(SAMPLE_APP_NAME);
	    appConfig.setRestPort(SAMPLE_APP_REST_PORT);

	    // Assume that the manager user can also be used as the REST admin user
	    appConfig.setRestAdminUsername(manageConfig.getUsername());
	    appConfig.setRestAdminPassword(manageConfig.getPassword());
    }

    /**
     * Initialize an AppDeployer with the given set of commands. Avoids having to create a Spring configuration.
     *
     * @param commands
     */
    protected void initializeAppDeployer(Command... commands) {
        appDeployer = new SimpleAppDeployer(manageClient, adminManager, commands);
    }

    protected void deploySampleApp() {
        appDeployer.deploy(appConfig);
    }

    protected void undeploySampleApp() {
    	if (appDeployer != null) {
		    try {
			    appDeployer.undeploy(appConfig);
		    } catch (Exception e) {
			    throw new RuntimeException("Unexpected error while undeploying sample app: " + e.getMessage(), e);
		    }
	    }
    }

    protected XccTemplate newModulesXccTemplate() {
    	return new XccTemplate(appConfig.getHost(), appConfig.getAppServicesPort(), appConfig.getRestAdminUsername(),
		    appConfig.getRestAdminPassword(), appConfig.getModulesDatabaseName());
    }

    /**
     * This command is configured to always load modules, ignoring the cache file in the build directory.
     * @return
     */
    protected LoadModulesCommand buildLoadModulesCommand() {
        LoadModulesCommand command = new LoadModulesCommand();
        appConfig.setModuleTimestampsPath(null);
        DefaultModulesLoader loader = (DefaultModulesLoader)(new DefaultModulesLoaderFactory().newModulesLoader(appConfig));
        loader.setModulesManager(null);
        command.setModulesLoader(loader);
        return command;
    }

    protected void setConfigBaseDir(String path) {
        appConfig.getFirstConfigDir().setBaseDir(new File("src/test/resources/" + path));
    }
}
