import java.sql.*;
import java.text.ParseException;

class FaceSpaceManagement{
	private static Connection connection; //used to hold the jdbc connection to the DB
	private Statement statement; //used to create an instance of the connection
	private PreparedStatement prepStatement; //used to create a prepared statement, that will be later reused
	private ResultSet resultSet; //used to hold the result of your query (if one exists)
	private String query;  //this will hold the query we are using

	/*
		Initiates DB Connection
	 */
	public static void main(String[] args){

		String username = "";
		String password = "";

		try{
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("Connection made");
		} catch (Exception Ex){
			System.out.println("Couldn't connect to database. Machine Error: " +
				Ex.toString());
		}

		finally{
			try {
				connection.close();
			} catch (Exception E){
				System.out.println("Connection couldn't close: " + E.toString());
			}
		}
	}

}