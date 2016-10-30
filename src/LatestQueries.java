import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class LatestQueries {

	public String getLatesttopTenQueries(String user)
	{
		String result ="";
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptiveweb","root","root");
			String query_latest = "select query from user_query where user_id ='" + user +"' order by timestamp_query desc LIMIT 10";
			st = conn.createStatement();
			rs = st.executeQuery(query_latest);
			String preference = null;
			while(rs.next())
			{				
				preference = rs.getString("query");
				System.out.println("Query " + preference );
				result = result + "\n";
			    result +=  preference;
			}
		}
		catch(Exception e)
		{
			
		}
		return result;
		
		
	}
}
