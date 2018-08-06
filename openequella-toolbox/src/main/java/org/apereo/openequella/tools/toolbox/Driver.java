/*
 * Copyright 2018 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.openequella.tools.toolbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apereo.openequella.tools.toolbox.Config.ToolboxFunction;

public class Driver {
	private static Logger LOGGER = LogManager.getLogger(Driver.class);
	
	
	/** 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 1) {
			LOGGER.error("Exiting - requires a config file.");
			return;
		}
		Config config = new Config(args[0]);
		if(!config.isValidConfig() ) {
			LOGGER.error("Exiting - invalid config.");
			return;
		}
		
		// Since the config is valid, this is guaranteed to work.
		ToolboxFunction tool = ToolboxFunction.valueOf(config.getConfig(Config.TOOLBOX_FUNCTION));
		switch (tool) {
		case MigrateToKaltura: {
			(new MigrateItemsToKalturaDriver()).execute(config);
			break;
		} case ExportItems: {
			(new ExportItemsDriver()).execute(config);
			break;
		} default: {
			LOGGER.error("Exiting - Unimplemented toolbox function of: {}.", tool);
			return;
		}
		}	
	}
}
