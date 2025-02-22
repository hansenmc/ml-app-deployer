package com.marklogic.appdeployer.command.forests;

import com.marklogic.appdeployer.command.CommandContext;
import com.marklogic.mgmt.api.forest.Forest;

import java.util.List;

public interface HostCalculator {

	/**
	 * Calculates which hosts can be used for primary and replica forests. The results are affected by the AppConfig
	 * properties that configure which groups and hosts are allowed to have forests for a particular database.
	 *
	 * In addition, if the database is configured to only have forests on one host, then the list of primary forests
	 * will have a single host. However, the list of replica forests will still have all candidate hosts so that
	 * replicas can still be created.
	 *
	 * @param databaseName
	 * @param context
	 * @param existingPrimaryForests
	 * @return
	 */
	ForestHostNames calculateHostNames(String databaseName, CommandContext context, List<Forest> existingPrimaryForests);

}
