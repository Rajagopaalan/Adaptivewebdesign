import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Preference {


	public String getPreference(String user)
	{
		Connection conn = null;
		ResultSet rs = null;
		Statement st = null;
		String index_file = "";
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/adaptiveweb","root","root");
			String query_topic = "select options from preference where user_id ='" +user +"'";
			st = conn.createStatement();
			rs = st.executeQuery(query_topic);
			String preference = null;
			while(rs.next())
			{				
				preference = rs.getString("options");
				System.out.println("Preference " + preference );
			    
			}
			if(preference.equals("0"))
				index_file = "index_code";
			else
				index_file = "index_text";
			return index_file;
		}
		
		catch(Exception e)
		{
			return index_file;
		}
	}
}
