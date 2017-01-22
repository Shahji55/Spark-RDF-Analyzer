package rdfanalyzer.spark;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.sound.sampled.AudioFormat.Encoding;

import org.apache.commons.io.IOUtils;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import ranking.Ranking;

import org.apache.spark.sql.Dataset;

public class Centrality {

	public static ConnAdapter objAdapter = new ConnAdapter();
	public static DataFrame graphFrame;
	
	public static String main(String metricType,String dataset, String nodeName){

		graphFrame = Service.sqlCtx().parquetFile(Configuration.storage() + dataset + ".parquet");
		graphFrame.cache().registerTempTable("Graph");

		for(int i=0;i<graphFrame.schema().fieldNames().length;i++){
			System.out.println("[LOG]Schema FieldName : "+graphFrame.schema().fieldNames()[i]);
		}
		
		nodeName = nodeName.replace("$", "/");
		
		System.out.println("[LOGS] Node name= "+nodeName);

		if(metricType.equals("1")){
			System.out.println("[LOGS] Present in metric type 1");
			return "<h1>"+ CalculateInDegree(nodeName) +"</h1>";
		}
		else if(metricType.equals("2")){
			System.out.println("[LOGS] Present in metric type 2");
			return "<h1>"+ CalculateOutDegree(nodeName) +"</h1>";
		}
		else if(metricType.equals("3")){
			System.out.println("[LOGS] Present in metric type 3");
			return CalculateBetweenness(nodeName);
		}
		else if(metricType.equals("4")){
			System.out.println("[LOGS] Present in metric type 4");
			return CalculateCloseness(nodeName);
		}
		
		return "none";
	}
	
	public static String CalculateInDegree(String node){

		String result = "";
		// Run SQL over loaded Graph.
		

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(subject) FROM Graph WHERE object = '"+node+"'");
		Row[] rows = resultsFrame.collect();

		result = Long.toString(rows[0].getLong(0));

		return result;
	}
	public static String CalculateOutDegree(String node){
		
		String result = "";

		DataFrame resultsFrame = Service.sqlCtx().sql("SELECT COUNT(object) from Graph where subject='"+node+"'");
		Row[] rows = resultsFrame.collect();

		result = Long.toString(rows[0].getLong(0));

		return result;
	}
	
	
	public static String CalculateBetweenness(String node){
		
		return "betweenness";
	}
	public static String CalculateCloseness(String node){
		
		DataFrame allSubjects = Service.sqlCtx().sql("SELECT subject,predicate,object FROM Graph");

		Ranking.CreateAdjacency(allSubjects);
		return "";
	}
	public static String readResource(final String fileName, Charset charset) throws IOException {
        return Resources.toString(Resources.getResource(fileName), charset);
	}
	
	public static String calculateStartNode(){
		
		/*
		 * We calculate 4 different values because. It is not necessary that the node which has
		 * highest in-degree also has highest outdegree. So if a node has highest out-degree we
		 * also calculate it's in-degree. And the other way around. Hence we end up with 4 
		 * values
		 */
		
		
		// node with highest out-degree
		DataFrame maxOutDegreeFrame = Service.sqlCtx()
				.sql("SELECT first(tbl1.subject),MAX(tbl1.OutdegreeCount) FROM"
						+ "(SELECT subject,COUNT(object) AS OutdegreeCount FROM Graph GROUP BY subject)tbl1");

		Row[] rowMaxOutDegree = maxOutDegreeFrame.collect();

		// in-degree of node with highest out-degree
		String maxInDegreeOfOutDegree = CalculateInDegree(rowMaxOutDegree[0].getString(0));

		
		// node with highest in-degree
		DataFrame maxInDegreeFrame = Service.sqlCtx()
				.sql("SELECT first(tbl1.object),MAX(tbl1.OutdegreeCount) FROM"
						+ "(SELECT object,COUNT(subject) AS OutdegreeCount FROM Graph GROUP BY object)tbl1");

		Row[] rowMaxInDegree = maxInDegreeFrame.collect();
		
		// out-degree of node with highest in-degree
		String maxOutDegreeOfInDegree = CalculateInDegree(rowMaxInDegree[0].getString(0));

		System.out.println("[LOG]Working until here yuppie");
			long maxOutdegreeTotal = rowMaxOutDegree[0].getLong(1) + Integer.parseInt(maxInDegreeOfOutDegree);
			long maxIndegreeTotal = rowMaxInDegree[0].getLong(1) + Integer.parseInt(maxOutDegreeOfInDegree);
			
			if(maxOutdegreeTotal < maxIndegreeTotal){
				return rowMaxOutDegree[0].getString(0);
			}
			return rowMaxInDegree[0].getString(0);

	}

}
