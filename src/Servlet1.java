

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;



/**
 * Servlet implementation class Servlet1
 */
@WebServlet("/Servlet1")
public class Servlet1 extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Servlet1() {
        super();
        // TODO Auto-generated constructor stub
    } @Override
    public void init() throws ServletException {
    	// TODO Auto-generated method stub
    	super.init();
    	System.out.println("Accessing servlet init");
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		System.out.println("Accessing servlet get");
		
		HttpSession session =  request.getSession();
		String user = "" +session.getAttribute("user_id");
		String originalString = request.getParameter("query");
		String index_file = ""; 
		String latest_queries_result = "";
		//System.out.println(" Path " + new java.io.File( "." ).getCanonicalPath());
		
		try
		{
			ServletContext sc = this.getServletContext();
			
			//Find the preference of the user from the Database.
			Preference pref = new Preference();
			index_file = pref.getPreference(user);
			System.out.println(" Path " + sc.getRealPath("/" + index_file) );
			
			LatestQueries latest_queries = new LatestQueries();
			latest_queries_result = latest_queries.getLatesttopTenQueries(user);
			
			WebSearch websearch = new WebSearch(sc.getRealPath("/" + index_file) );
			QueryEngine q = new QueryEngine(sc.getRealPath("/" + index_file) );
			
		
			System.out.println("Printing userId " + user);
		    System.out.println(" Query String " + originalString);
			//String str = "difference between inheritance and javascript arrays  and polymorphism";
			//String user = "1";
			String queryString = websearch.stopWordRemoval(originalString);
			//websearch.dataPreprocessing();
			
			ArrayList<ArrayList<String>> queryResult = q.query(queryString,originalString,user);
			request.getSession().setAttribute("queryresult", queryResult);
			request.getSession().setAttribute("latestqueryresult", latest_queries_result);
			getServletContext().getRequestDispatcher("/query-page.jsp").forward(request, response);
			/*PrintWriter out = response.getWriter();
			out.write(t);
			out.close();*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
