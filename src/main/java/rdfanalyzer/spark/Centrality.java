package rdfanalyzer.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;



public class Centrality {

	public static ConnAdapter objAdapter = new ConnAdapter();
	public static ArrayList<Threader> threadsList = new ArrayList<Threader>();

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
		
		/*
		 * 
		 * 	Calculate steps this node needs to take to reach other nodes. 
		 *  Suppose this node take 2,3,4 steps to reach 3 other nodes in the graph.
		 *  Than we sum these values and take an inverse of it to find the closeness of this node.
		 *  
		 *  In this case 2+3+4 = 9 and than inverse of 9 is 1/9 = 0.1111.
		 *
		 * 	Rules:
		 * 	
		 * 	Be careful about the predicate directions i.e
		 *  Node1 --> Node2 --> Node3 <--- Node4 In this case Node3 is unreachable from Node1
		 *  Hence the distance from Node1 to Node4 is 0 while Node1 to Node3 is 2.
		 * 
		 */
		
		Row[] outDegreeObjects = Threader.GetNodeObjects(node);
		System.out.println("[LOG] GetNodeObjects Finished1"+ outDegreeObjects[0].getString(0));
		String startnode = calculateStartNode();
		return startnode;

		// Run SQL over loaded Graph.
//		DataFrame resultsFrame = Service.sqlCtx()
//				.sql("SELECT MyTable2.subject FROM "
//						+ "("
//						+ "		SELECT DISTINCT MyTable1.subject FROM "
//								+ "("
//								+ "	SELECT subject FROM Graph"
//								+ " UNION ALL SELECT object FROM Graph" + " "
//								+ ") MyTable1"
//						+ ") "
//					+ "MyTable2 "
//					+ "WHERE MyTable2.object");

		
//		for(int i=0;i<outDegreeObjects.length;i++){
//
//			Threader thread = new Threader(outDegreeObjects[i].toString());
//			thread.start();
//			threadsList.add(thread);
//			
//		}

//		Threader thread = new Threader(outDegreeObjects);
//		thread.start();
//		
//		DataFrame teenagers = Service.sqlCtx().sql("SELECT * FROM Graph ");
//		List<String> teenagerNames = teenagers.javaRDD().map(new Function<Row, String>() {
//		  public String call(Row row) {
//		    
//			  System.out.println("[LOG]Name: " + row.getString(0));
//
//			  return "";
//		  }
//		}).collect();
//		
//		return "closeness";
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
			
			if(maxOutdegreeTotal > maxIndegreeTotal){
				return rowMaxOutDegree[0].getString(0);
			}
			return rowMaxInDegree[0].getString(0);

	}

}
