
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

class QueryEngine {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private QueryParser parser;
	Connection conn = null;
	ResultSet rs = null;
	Statement st = null;
	
	public QueryEngine(String indexPath) {
		reader = null;
		searcher = null;
		analyzer = null;
		parser = null;
		
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
			searcher = new IndexSearcher(reader);
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			parser = new QueryParser(Version.LUCENE_40, "body", analyzer);
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptiveweb","root","root");
			//conn = DriverManager.getConnection("jdbc:mysql://127.5.163.2:3306/adaptiveweb1","adminD8V5YgM","V_LJyttUFEBY");
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public ArrayList<ArrayList<String>> query(String queryString, String original_query, String current_user) 
	{
		ArrayList<ArrayList<String>> res =new ArrayList<ArrayList<String>>();
		String result = "";
		
		Query query;
		HashMap<String,Integer> explicit_map = new HashMap<String,Integer>();
		HashMap<String,Integer> implicit_map = new HashMap<String,Integer>();
		HashMap<String,Integer> QueryString_map = new HashMap<String,Integer>();
		HashMap<String,Integer> preference_map = new HashMap<String,Integer> ();
		
		Set<String> token_preference_set = new HashSet<String>();
		String token_preference[] = null;
				
		int explicit_total = 0; // Should be zero for the main run
		int implicit_total = 0;  // Should be zero for the main run
		
		Iterator ite = null;
		
		try {
			
			
			
			String token_query[] = queryString.split("\\s");
			String token_original_query[] = original_query.split("\\s");
			//Find the weight of the explicit feedback.
			boolean bool = false; 
			for( String s1: token_query)
			{
				for(String s2:token_original_query )
				{
					if(s1.contains(s2))
					{
						bool = true;
					}
				}
			}
			if( bool == true)  // Check if the given query contains at least one key word
			{
			
				String query_topic = "select topics from preference where user_id =" +current_user +"";
				st = conn.createStatement();
				rs = st.executeQuery(query_topic);
				String str = null;
				while(rs.next())
				{				
					str = rs.getString("topics");
					System.out.println("topic Preference " + str );
				    
				}
				if(str != null)   // If there is an explicit user preference 
				{
					if(str.contains(","))   // Many explicit user preferences
					{
						token_preference = str.split(",");
						
						for( String s1: token_preference)
						{
							token_preference_set.add(s1);
						}
						
						 for(String s: token_query)
						 {
								
									 if(token_preference_set.contains(s))
									 {
										 explicit_map.put(s, 100);
										 explicit_total = explicit_total + 100;
									 }
									 else
									 {
										 //System.out.println( " s " + s);
										 
										 explicit_map.put(s, 10);   // Change the number to 30
										 explicit_total = explicit_total + 10;
									 }
								
						 }
					}
					else   // Only one explicit user preference
					{
						for(String s: token_query)
						 {
							if(s.contains(str))
							{
								explicit_map.put(s, 100);
								explicit_total = explicit_total + 100;
							}
							else
							{
								 explicit_map.put(s, 10);   // Change the number to 30
								 explicit_total = explicit_total + 10;
							}
						 }
					}
				
				}
				// Find the most similar user and give the appropriate results. 
				// Find weight of implicit feedback
				
				 String previous_search = "select tags from user_tags where user_id =" +current_user +"";
					st = conn.createStatement();
					rs = st.executeQuery(previous_search);
					str = null;
					
						
						while(rs.next())
						{			
							str = rs.getString("tags");
							System.out.println("tags " + str );
						    
						}
					
					if( str != null )    // If there is an implicit feedback data
					{
						
							token_preference = str.split(",");
						
						for(String s: token_preference)
						{
							String s1[] = s.split(":");
							preference_map.put(s1[0], Integer.parseInt(s1[1]) ); 
						}
						
						
					 for(String s: token_query)
					 {
						 
						 //Find value of each tag.
						
						 if( preference_map.containsKey(s))
						 {
							 implicit_map.put(s,preference_map.get(s) );
							 implicit_total = implicit_total + preference_map.get(s);
							 //Update the tag count for the tag appropriately in the user_tags table for the user.
							 
						 }
						  else
						 {
							 implicit_map.put(s, 0);
							//Update the tag count = 1 for the tag appropriately in the user_tags table for the user.
						 }
						 
						 
					   }
				 
					}
			
				//Calculation of query weights.
				System.out.println("Explicit map " + explicit_map);
				System.out.println(" Implicit map " + implicit_map);
				
				 queryString = "";
				 int total = 0;
				 for(String s: token_query)
				 {
					 int individual_string_total = 0;
					 if(!explicit_map.containsKey(s) && !implicit_map.containsKey(s)) // If both implicit and explicit feedback are not available
					 {
						 individual_string_total = 10;
						 QueryString_map.put(s, individual_string_total);
					 }
					 
					 else if(!explicit_map.containsKey(s) && implicit_map.containsKey(s)) // If implicit feedback is available and explicit feedback is not available
					 {
						 int t = implicit_map.get(s);
						 if (implicit_total > 0 )
							 individual_string_total += (t*40) / implicit_total;
						
						 QueryString_map.put(s, individual_string_total);
					 }
					 
					 else if(explicit_map.containsKey(s) && !implicit_map.containsKey(s)) // If implicit feedback is available and explicit feedback is not available
					 {
						 int t = explicit_map.get(s);
						 if (explicit_total > 0 )
							 individual_string_total += (t*60) / explicit_total;
						
						 QueryString_map.put(s, individual_string_total);
					 }
					 else
					 {
						 int t = explicit_map.get(s);
						 
						 if (explicit_total > 0 )
							 individual_string_total = (t * 60) /explicit_total;
						 t = implicit_map.get(s);
						 if (implicit_total > 0 )
							 individual_string_total += (t*40) / implicit_total;
						 QueryString_map.put(s, individual_string_total);
						 
					 }
					 
					 total += individual_string_total;
					  
				     if(preference_map.containsKey(s))
				     {
				    	 preference_map.put(s,preference_map.get(s)+1);
				     }
				     else
				     {
				    	 preference_map.put(s,1);
				     }
				 }
				
				 ite = QueryString_map.keySet().iterator();
				 while( ite.hasNext() )
				 {	 
					 String key = (String) ite.next();
					 int t = QueryString_map.get(key);
					 t = t*20/total;
					 if(t == 0)
						 queryString += key;
					 else
					 {
						 for(int i = 0; i< t; i++)
						 {
							 queryString += key + " ";
						 }
					 }
					 System.out.println(" Token " + key + " Total : " + t);
				 }
				 
				 System.out.println(" Preference Map after update " + preference_map); 
				 
				 //Update previous_search table.
				 ite = preference_map.keySet().iterator();
				 String tags = "";
				 while(ite.hasNext())
				 {
					String temp = (String) ite.next();
					int t = preference_map.get(temp);
					tags = tags + temp + ":" + t +",";
				 }
				 
				 // UPDATE USER TAGS in user_tags
				 tags = tags.substring(0,tags.length()-1);
				 String query_previous_search_delete = "delete from user_tags where user_id = " + current_user+"" ;
			     PreparedStatement preparedStmt = conn.prepareStatement(query_previous_search_delete);
			     preparedStmt.executeUpdate();
			     String query_previous_search_insert = "INSERT INTO `adaptiveweb`.`user_tags` (`user_id`, `tags`) VALUES (" +current_user + ",'" + tags + "')";
			     preparedStmt = conn.prepareStatement(query_previous_search_insert);
			     preparedStmt.execute();
			     
			     //UPDATE USER TAGS 
			     ite = preference_map.keySet().iterator();
			     while( ite.hasNext() )
			     {
			    	 String column = (String) ite.next();
			    	 String query_previous_search_update = "update overall set " + column +" = " +preference_map.get(column) +" where user_id = " + current_user  ;
			    	 System.out.println(query_previous_search_update);
			    	 preparedStmt = conn.prepareStatement(query_previous_search_update);
				     preparedStmt.executeUpdate();
				     
			     }
			     
			     Date date= new Date();
		         //getTime() returns current time in milliseconds
			     long time = date.getTime();
		         //Passed the milliseconds to constructor of Timestamp class 
			     Timestamp ts = new Timestamp(time);
			 
			     String query_user_query = "INSERT INTO `adaptiveweb`.`user_query` (`user_id`, `timestamp_query`, `query`) VALUES (" +current_user + ",'" + ts + "', '" + original_query+"')";
			     System.out.println(" query_user_query " + query_user_query);
			     preparedStmt = conn.prepareStatement(query_user_query);
			     preparedStmt.execute();
			     
			     
				if(queryString.length() > 0)
				{
					queryString = queryString.substring(0,queryString.length()-1);
				}
				if(total == 0 || queryString.length() == 0)
					queryString = original_query;
			}
			else
			{
				queryString = original_query;
			}
			System.out.println(" Result query " + queryString); 
			//queryString = "inheritance array polymorphism";
			query = parser.parse(queryString);
			
			TopDocs results = null;
			results = searcher.search(query, 10);
			System.out.println(" Total Hits " + results.totalHits);
			ScoreDoc[] hits = results.scoreDocs;
			System.out.println("Number of results: " + hits.length);
			
			
			for(ScoreDoc h : hits) {
				Document doc = searcher.doc(h.doc);
			    ArrayList<String> arr1 = new ArrayList<String>(); 	
				arr1.add(" <a href = 'http://stackoverflow.com/questions/" +doc.getField("id").stringValue() + "'> "   +doc.getField("title").stringValue() +"</a> ");
				//arr1.add(" Title "+ doc.getField("title").stringValue());
				arr1.add(doc.getField("body").stringValue());
				//result += h + "\n";
				res.add(arr1);
				System.out.println(" Id " +doc.getField("id").stringValue());
				System.out.println("Title "+ doc.getField("title").stringValue());
				System.out.println("Body " +doc.getField("body").stringValue());
				
				//System.out.println(h);
				
				System.out.println();
			}
			return res;
		} 
		catch (Exception e) {
			System.out.println(e);
			return res;
		} 
	}
	
	
}
