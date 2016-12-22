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

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

public class GetClasses {
	public static String main(String[] args) throws Exception {
		String result = "";

		/*
		 * Read graph from parquet
		 */
		DataFrame schemaRDF = WebService.sqlContext
				.parquetFile(Configuration.properties.getProperty("Storage") + args[0] + ".parquet");
		schemaRDF.cache().registerTempTable("Graph");

		// SQL can be run over RDDs that have been registered as tables.
		DataFrame predicatesFrame = WebService.sqlContext.sql(
				"SELECT DISTINCT object FROM Graph WHERE predicate='<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>'");

		// The results of SQL queries are DataFrames and support all the normal
		// RDD operations.
		Row[] resultRows = predicatesFrame.collect();

		if (args[1].equals("Normal")) {
			result = "<table class=\"table table-striped\">";
			result += "<thead><tr><th style=\"text-align: center;\">Class</th></tr></thead>";
			for (Row r : resultRows) {
				result += "<tr><td data-toggle=\"tooltip\" title=\"" + r.getString(0) + "\">"
						+ Configuration.shortenURI(r.getString(0)) + "</td></tr>";
			}
			result += "</table>";
		} else if (args[1].equals("Chart")) {
			for (Row r : resultRows) {
				result += Configuration.shortenURI(r.getString(0)) + ",";
			}
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}
}
