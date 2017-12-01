import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;



public class FaceSpaceManagement {

	private static Connection connection; //used to hold the jdbc connection to the DB
	private Statement statement; //used to create an instance of the connection
	private PreparedStatement prepStatement; //used to create a prepared statement, that will be later reused
	private ResultSet resultSet; //used to hold the result of your query (if one exists)
	private String query;  //this will hold the query we are using
	private Scanner s = new Scanner(System.in);
	private int loggedInUserID;

	/*
		Initiates DB Connection
	 */
	public FaceSpaceManagement(String username, String password) {

		try{
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("Connection made");
		} catch (Exception Ex){
			System.out.println("Couldn't connect to database. Machine Error: " +
				Ex.toString());
		}

		//finally{
			//try {
			//	connection.close();
			//} catch (Exception E){
		//		System.out.println("Connection couldn't close: " + E.toString());
		//	}
		//}
	}

	public synchronized boolean createUser(){

		try {
			connection.setAutoCommit(false); //We want every createUser call to be its own transaction
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();

			query = "SELECT max(userID) from profile";
			resultSet = statement.executeQuery(query);
			resultSet.next();
			int newUserID = resultSet.getInt(1);
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
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date date_birth = new Date(df.parse(dob).getTime());

			//First prepare the query structure, using question marks as placeholders
			query = "insert into profile values (?, ?, ?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(query);

			//Now replace the question marks with the appropriate user values
			prepStatement.setInt(1, newUserID);
			prepStatement.setString(2, name);
			prepStatement.setString(3, email);
			prepStatement.setString(4, password);
			prepStatement.setDate(5, date_birth);
			prepStatement.setString(6, null);

			//Use executeUpdate for inserts and updates;
			prepStatement.executeUpdate();
			connection.commit();
			resultSet.close();
			loggedInUserID = newUserID;
			return true;
		}
		catch(SQLIntegrityConstraintViolationException ve) {
			System.out.println("That email is already registered to a user");
		}
		catch(Exception e) {
			System.out.println("Failed to create user");
			e.printStackTrace();
		}

		return false;
	}

	public synchronized boolean logIn(){
		String email, password;

		try {
			connection.setAutoCommit(false); //We want every createUser call to be its own transaction
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			System.out.println("Enter your email and password to log in.");
			System.out.print("Email : ");
			email = s.nextLine();
			System.out.print("Password : ");
			password = s.nextLine();

			query = "select userID from profile where email = ? and password = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setString(1, email);
			prepStatement.setString(2, password);
			resultSet = prepStatement.executeQuery();

			int counter = 0;
			int currentID = 0;
			while(resultSet.next()) {
				counter++;
				currentID = resultSet.getInt(1);
			}

			if(counter == 1) {
				loggedInUserID = currentID;
				System.out.println("Log In Successful");
				System.out.println("Log In ID : " + loggedInUserID);
				return true;
			}
		}
		catch(Exception e) {
			System.out.println("Incorrect username or password.");
			e.printStackTrace();
		}

		return false;
	}

	public synchronized void initiateFriendship(){

	}

	public synchronized void confirmFriendship(){

	}

	public synchronized void displayFriends(){

	}

	public synchronized void createGroup(){

		try {
			String gName, description;
			int limit;

			connection.setAutoCommit(false); //We want every createUser call to be its own transaction
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();

			query = "SELECT max(gID) from groups";
			ResultSet resultSet = statement.executeQuery(query);
			resultSet.next();
			int newGID = resultSet.getInt(1);
			newGID++;

			System.out.print("Name the New Group : ");
			gName = s.nextLine();
			System.out.print("Enter a Description for the Group : ");
			description = s.nextLine();
			System.out.print("Enter the Membership Limit for the Group : ");
			limit = s.nextInt();

			query = "insert into groups values (?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(query);

			prepStatement.setInt(1, newGID);
			prepStatement.setString(2, gName);
			prepStatement.setString(3, description);
			prepStatement.setInt(4, limit);
			prepStatement.executeUpdate();

			query = "insert into groupmembership values (?, ?, ?)";
			prepStatement = connection.prepareStatement(query);

			prepStatement.setInt(1, newGID);
			prepStatement.setInt(2, loggedInUserID);
			prepStatement.setString(3, "manager");
			prepStatement.setInt(4, limit);

			prepStatement.executeUpdate();
			connection.commit();
		}
		catch(Exception e) {
			System.out.println("Failed to Create Group");
			e.printStackTrace();
		}
	}

	public void confirmGroupMembers() {

	}

	public synchronized void initiateAddingGroup(){

	}

	public synchronized void sendMessageToUser(){

	}

	public synchronized void sendMessageToGroup(){

	}

	public synchronized void displayMessages(){

	}

	public synchronized void displayNewMessages(){

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

	public void disconnect() {
			try {
				connection.close();
			} catch (Exception E){
				System.out.println("Connection couldn't close: " + E.toString());
			}
		}
}
