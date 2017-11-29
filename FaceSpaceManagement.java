import java.sql.*;
import java.text.ParseException;



class FaceSpaceManagement{
	private static Connection connection; //used to hold the jdbc connection to the DB
	private Statement statement; //used to create an instance of the connection
	private PreparedStatement prepStatement; //used to create a prepared statement, that will be later reused
	private ResultSet resultSet; //used to hold the result of your query (if one exists)
	private String query;  //this will hold the query we are using
	Scanner s = new Scanner(System.in);

	/*
		Initiates DB Connection
	 */
	public static void main(String[] args){

		String username = "tos27";
		String password = "3959421";

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

	public void CreateUser(){

		connect.setAutoCommit(false); //We want every createUser call to be its own transaction
		connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		statement = connection.createStatement();

		query = "SELECT max(userID) from profile;";
		ResultSet resultSet = statement.executeQuery(query);
		resultSet.next();
		int newUserID = resultSet.getInt();
		newUserID++;

		/*
			Prompts the user for the information that will make up the profile.
		 */
		System.out.println("Please enter the user's full name: ");
		String name = s.nextLine();
		System.out.println("Please enter the user's email address: ");
		String email = s.nextLine();
		System.out.println("Please enter the user's password: ");
		String password = s.nextLine();
		System.out.println("Please enter the user's date of birth in YYYY-MM-DD format: ");
		String dob = s.nextLine();

		//Parse date from user input
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("YYYY-MM-DD");
		java.sql.Date date_birth = new java.sql.Date(df.parse(dob).getTime());

		//First prepare the query structure, using question marks as placeholders
		query = "insert into profile values (?, ?, ?, ?, ?, ?)";

		//Now replace the question marks with the appropriate user values
		prepStatement.setInt(1, newUserID);
		prepStatement.setString(2, name);
		prepStatement.setString(3, email);
		prepStatement.setString(4, password);
		prepStatement.setDate(5, date_birth);
		prepStatement.setNull(6, java.sql.Date);

		//Use executeUpdate for inserts and updates;
		prepStatement.executeUpdate();


	}

	public void LogIn(){

	}

	public void initiateFriendship(){

	}

	public void confirmFriendship(){

	}

	public void displayFriends(){

	}

	public void createGroup(){

	}

	public void initiateAddingGroup(){

	}

	public void sendMessageToUser(){

	}

	public void sendMessageToGroup(){

	}

	public void displayMessages(){

	}

	public void displayNewMessages(){

	}

	public void searchForUser(){

	}

	public void threeDegrees(){

	}

	public void topMessages(){

	}

	public void dropUser(){

	}

	public void logOut(){

	}
}