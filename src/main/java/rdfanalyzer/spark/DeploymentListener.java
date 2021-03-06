/*
 * Copyright (C) 2016 University of Freiburg.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rdfanalyzer.spark;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 * This class gets called on deploy and undeploy events of Tomcat. It creates
 * and respectively destroys required services.
 * 
 * @author marcoprobst
 */
public class DeploymentListener implements ServletContextListener {
	private final static Logger logger = Logger.getLogger(DeploymentListener.class);

	@Override
	public void contextInitialized(ServletContextEvent ctxEvent) {
		String storageDir = Configuration.storage();
		String appName = Service.sparkCtx().appName();

		logger.info("Using Storage Directory: " + storageDir);
		logger.info("Runnig Spark App '" + appName + "'");

		Configuration.setupHadoop();
	}

	@Override
	public void contextDestroyed(ServletContextEvent ctxEvent) {
		Service.shutdown();
	}
}
