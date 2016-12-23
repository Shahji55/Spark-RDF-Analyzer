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

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

/**
 * This class gets called on deploy and undeploy events of Tomcat. It creates
 * and respectively destroys required services.
 * 
 * @author marcoprobst
 */
public class DeploymentListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent ctxEvent) {
		WebService.sparkConf = new SparkConf().setAppName("JavaSparkSQL").setMaster("local[*]");
		WebService.ctx = new JavaSparkContext(WebService.sparkConf);
		WebService.sqlContext = new SQLContext(WebService.ctx);
		WebService.configuration = new Configuration();
		// TODO: Does Configuration need to be here? Why no static class?

		// TODO: Following comes from Cluster:
		// public static Configuration configuration = new Configuration();
		// public static SparkConf sparkConf =
		// SparkConfigurationHelper.getConfiguration();
		// public static JavaSparkContext ctx = new JavaSparkContext(sparkConf);
		// public static SQLContext sqlContext = new SQLContext(ctx);
		// public static ClassLoader classLoader =
		// WebService.class.getClassLoader();
	}

	@Override
	public void contextDestroyed(ServletContextEvent ctxEvent) {
		WebService.sqlContext.clearCache();
		SQLContext.clearActive();
		WebService.sqlContext = null;

		WebService.ctx.cancelAllJobs();
		WebService.ctx.close();
		WebService.ctx.stop();
		WebService.ctx = null;

		WebService.configuration = null;
		WebService.sparkConf = null;
	}
}
