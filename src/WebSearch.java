import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Sri Venkata Subramaniyan Arunagiri
 */

import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;



public class WebSearch {
	
	private Directory dir;
	private Analyzer analyzer;
	private IndexWriterConfig iwc;
	private IndexWriter writer;

	public Set tags = new HashSet();
	Connection conn = null;
	Statement st = null;
	ResultSet rs = null;
	
	public WebSearch(String indexPath)
	{
		
		tags.add("java"); 		tags.add("javascript"); 		tags.add("inheritance");		tags.add("arrays");		tags.add("error");		tags.add("exception");
		tags.add("interface");		tags.add("polymorphism");		tags.add("constructor");		tags.add("class");		tags.add("overloading");
		tags.add("method");		tags.add("thread");		tags.add("object");		tags.add("string");		tags.add("variables");
		tags.add("arrays");		tags.add("collection");		tags.add("sort");		tags.add("interface");		tags.add("abstract");
		tags.add("loop");		tags.add("switch");

		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptiveweb","root","root");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//indexPath = "index5_new.dat";
		try {
			System.out.println("-------------------------");
			//System.out.println(" Websearch Path " + getClass().getClassLoader().getResourceAsStream("index5_new.dat"));
			dir = FSDirectory.open(new File(indexPath));
			System.out.println("-------------------------");
			analyzer = new StandardAnalyzer(Version.LUCENE_40);
			iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
			iwc.setOpenMode(OpenMode.CREATE/*_OR_APPEND*/);
			
			writer = new IndexWriter(dir, iwc);
		    System.out.println("Created index Writer ");	
			//writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public String stopWordRemoval(String query)
	{
		StringBuilder sbuilder = new StringBuilder();
		String token[] = query.split("\\s");
		for(String s: token)
		{
			if( tags.contains(s) )
			{
				sbuilder.append(s + " ");
			}
		}
		
		if(sbuilder.length() > 0)
		{
			sbuilder.substring(0, sbuilder.length()-1);
		}
		System.out.println(sbuilder.toString());
		return sbuilder.toString();
	}
	
	
	
	public void indexSite(String body, String title, String id) {
		Document doc = new Document();
		Field urlField = new StringField("id", id, Field.Store.YES);
		doc.add(urlField);
		Field textField = new TextField("body", body, Field.Store.YES);
		doc.add(textField);
		Field titleField = new TextField("title", title, Field.Store.YES);
		doc.add(titleField);
		if(writer.getConfig().getOpenMode() == OpenMode.CREATE) {
			System.out.println("adding:   " + id);
			try {
				writer.addDocument(doc);
			} catch (IOException e) {
				System.out.println(e);
			}
		}
		
		
	}
	
	public void dataPreprocessing()
	{
		String query_topic = "select id,body,title from queryresults";
		try {
			st = conn.createStatement();
			rs = st.executeQuery(query_topic);
			String id = null, body = null, title = null;
			while(rs.next())
			{				
				id = rs.getString("id");
				body = rs.getString("body");
				title = rs.getString("title");
				//System.out.println(body);
				indexSite(body, title, id);
		    }
			
		  writer.close();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
