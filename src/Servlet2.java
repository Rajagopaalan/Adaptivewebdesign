
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Servlet2
 */
@WebServlet("/Servlet2")
public class Servlet2 extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Servlet2() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
	
		try {
			String userName = "root";
			String password = "root";
			String URL = "jdbc:mysql://localhost:3306/adaptiveweb";

			Class.forName("com.mysql.jdbc.Driver");
			Connection con = DriverManager.getConnection(URL, userName, password);
			Statement st = con.createStatement();
			ResultSet rs;

			System.out.println("Inside Servlet 1");
//			Connection conn;
//			ResultSet rs;
//			Statement st;// = conn.createStatement();
			int[][] data = {};
			HttpSession session = request.getSession();
			//String user_id = "" + session.getAttribute("user_id");
			// Find the user tag pattern from the Database.
			System.out.println("Inside Servlet 2");
	

			// --------------------------------------------------------------------------------

			

			// getting details from form
			String user_name = request.getParameter("login-user-name");
			String pwd = request.getParameter("login-password");
			int user_id_login =-1;

			rs = st.executeQuery(
					"SELECT * FROM registration WHERE user_name = '" + user_name + "' and password = '" + pwd + "'");

			// if there exists no user as entered
			if (!rs.next()) {
				System.out.println("Sorry, could not find that user. ");
				response.sendRedirect("index.html#user-login-section");
				// if there exists a user as entered
			} else {
				user_id_login = Integer.parseInt(rs.getString("user_id"));

				// setup session attribute and navigate to query page
				session.setAttribute("user_id", user_id_login);
				session.setMaxInactiveInterval(600);

			}

			// ---------------------------------------------------------------------------------
			String query_topic = "select * from overall; ";
		//	st = conn.createStatement();
			rs = st.executeQuery("SELECT COUNT(*) FROM overall");

			// get the number of rows from the result set
			rs.next();
			int rowCount = rs.getInt(1);
			//int rowCount = 7;
			// get the data
			System.out.println("Row count " +rowCount);
			int colCount = 8;
			int data1[][] = new int[rowCount][colCount];
			int j = 0;
			rs = st.executeQuery(query_topic);
			System.out.println("----------Connection established and got results");

			// user index mapping

			int iter = 0;
			while (rs.next()) {
				hm.put(iter, rs.getInt(1)); // insert each user record into hash
											// map

				if (user_id_login == rs.getInt(1)) {
					index = iter;
				}
				iter++;
				for (int i = 1; i < colCount + 1; i++) {
					data1[j][i - 1] = rs.getInt(i);

				}
				System.out.println();
				++j;
			}
			System.out.println("----------data collected user id=" + user_id_login + " index = " + index);

			// calculating nearest neighbours
			double dataNew[][] = new double[rowCount][colCount];
			for (int i = 0; i < rowCount; i++) {
				for (int k = 0; k < colCount - 1; k++) {
					dataNew[i][k] = data1[i][k + 1];
					System.out.print(dataNew[i][k] + ", ");
				}
				System.out.println();
			}

			double[][] temp = new double[dataNew[0].length][dataNew.length];
			for (int i = 0; i < dataNew.length; i++) {
				for (int j1 = 0; j1 < dataNew[0].length; j1++) {
					temp[j1][i] = dataNew[i][j1];
					System.out.println(temp[j1][i]);
				}
				System.out.println();
			}

			computeAllPairwiseCorrelations(temp, index);

			System.out.println("hashmap val " + hm_corr);
			System.out.println("prq val " + prq);

			prq.poll();
			nn_index_val1 = prq.poll();
			nn_index_val2 = prq.poll();
			nn_index1 = hm_corr.get(nn_index_val1);
			nn_index2 = hm_corr.get(nn_index_val2);

			System.out.println("The user's index is " + index);
			System.out.println("Nearest Neighbours are " + nn_index1 + " and " + nn_index2);

			if (nn_index_val1 > 0 && nn_index_val2 > 0) {
				request.getSession().setAttribute("id_of_user", hm.get(index));
				request.getSession().setAttribute("id_of_nn1", hm.get(nn_index1));
				request.getSession().setAttribute("id_of_nn2", hm.get(nn_index2));
			}

			// response.sendRedirect("query-page.jsp");
			getServletContext().getRequestDispatcher("/query-page.jsp").forward(request, response);

			response.getWriter().append("Served at: ").append(request.getContextPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

//			rs.close();
//			st.close();
//			con.close();

		}
	}

	static int nn_index1 = -1, nn_index2 = -1, index = 0;
	static double nn_index_val1 = 0.0, nn_index_val2 = 0.0;
	static HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
	static HashMap<Double, Integer> hm_corr = new HashMap<Double, Integer>();
	static PriorityQueue<Double> prq = new PriorityQueue<Double>(10,Collections.reverseOrder());

	public static void computeAllPairwiseCorrelations(double[][] data, int index) {
		int numCols = data[0].length;
		double correlation = 0.0;
		for (int i = 0; i < numCols; i++) {
			for (int j = i; j < numCols; j++) {
				if (i == index || j == index) {
					// call the correlation computation method here
					correlation = computeCorrelation(i, j, data);

					prq.add(correlation);
					if (i != index)
						hm_corr.put(correlation, i);
					else
						hm_corr.put(correlation, j);

					System.out.println("Correlation between " + i + " and " + j + "--->" + correlation); // compute
				}
			}
		}

	}

	public static double computeCorrelation(int i, int j, double[][] data) {
		double x = 0.0, y = 0.0, xx = 0.0, yy = 0.0, xy = 0.0, n = 0.0;
		n = data.length;
		for (int row = 0; row < data.length; row++) {
			x += data[row][i];
			y += data[row][j];
			xx += Math.pow(data[row][i], 2.0d);
			yy += Math.pow(data[row][j], 2.0d);
			xy += data[row][i] * data[row][j];
		}
		double numerator = xy - ((x * y) / n);
		double denominator1 = xx - (Math.pow(x, 2.0d) / n);
		double denominator2 = yy - (Math.pow(y, 2.0d) / n);
		double denominator = Math.sqrt(xx * yy);
		double corr = numerator / denominator;
		return corr;
	}

}
