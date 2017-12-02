import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.ArrayList;



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
			System.out.println("Created new user with UserID: " + newUserID);
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

		int friendID = 0; String message; boolean loop = true; String userResponse;

		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			while (loop) {
				System.out.println(
					"Please enter the ID of the friend you'd like to make: ");
				friendID = s.nextInt();

				query = "Select name from profile where userID = ?";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, friendID);
				resultSet = prepStatement.executeQuery();

				/*
					Error handling block, checks to make sure the profile exists,
					then checks to make sure the user found the right person,
					then checks to make sure they're not already friends
				 */
				if (!resultSet.next()) {
					System.out.println(
						"UserID " + friendID + " not found, please try again.");
				} else {
					//Confirm user choice
					System.out.println("You'd like to send a friend request to " + resultSet.getString(1)
						                   + ", is this correct? Enter 'yes' or 'no' without quotes");
					userResponse = s.nextLine();
					userResponse = s.nextLine();
					if (userResponse.equals("yes")) {
						//Checks to make sure they're not already friends, returns to main menu
						//if so.
						if (checkIfFriend(friendID)){
							System.out.println("You are already friends with this person, " +
								"returning to main menu.");
							return;
						}
						loop = false;
					} else {
						//User input does not confirm if profile is what they wanted, restarts
						//the loop
						continue;
					}
				}
				//END error handling
			}


			System.out.println("Now enter the message you'd like to send along with the request: ");
			message = s.nextLine();

			query = "insert into pendingFriends values (?, ?, ?)";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, loggedInUserID);
			prepStatement.setInt(2, friendID);
			prepStatement.setString(3, message);
			prepStatement.executeUpdate();
			connection.commit();
			System.out.println("Friend request successfully sent");
			resultSet.close();
			return;
		} catch (Exception e){
			System.out.println("Failed to add friend");
			e.printStackTrace();
		}

	}

	public synchronized void confirmFriendship(){
		int counter = 1; int choice = 0;
		boolean friendRequests = true;
		boolean groupRequests = true;

		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			while(true) {
				query = "Select fromID, message from pendingFriends where toID"
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public synchronized void displayFriends(){
		int counter = 0;
		boolean loop = true;
		int input;
		int choice;

		connect.setAutoCommit(false);
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		while(true) {
			try {
				resultSet = getFriends(loggedInUserID);

				if (!resultSet.isBeforeFirst()) {
					System.out.println("No friends found");
					return;
				}
				System.out.println("Here are your friends: \n");
				while (resultSet.next()) {
					System.out.println(counter++ + ")");
					System.out.println(
						resultSet.getInt(1));
					System.out.println(
						resultSet.getString(2) + "\n");
				}

				System.out.println(
					"Enter the userID you're interested in, or 0 to return to main menu");
				choice = s.nextInt();
				if (choice == 0) {
					connection.commit();
					return;
				} else {
					System.out.println("Would you like to\n" +
						"1.)Browse this user's friends\n" +
						"2.)See this user's profile\n" +
						"3.)Go back to browsing your friends");
					input = s.nextInt();
					switch (input){
						case 1:
							browseFriends(choice);
							break;
						case 2:
							printProfile(choice);
							break;
						case 3:
							continue;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
			prepStatement.setString(4, String.valueOf(limit));
			prepStatement.executeUpdate();

			query = "insert into groupmembership values (?, ?, ?)";
			prepStatement = connection.prepareStatement(query);

			prepStatement.setInt(1, newGID);
			prepStatement.setInt(2, loggedInUserID);
			prepStatement.setString(3, "manager");


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

	//Helper method for creating a friend request
	public boolean checkIfFriend(int friendID){
		try {
			query = "SELECT * from friends where userID1 = ? and userID2 = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, loggedInUserID);
			prepStatement.setInt(2, friendID);
			resultSet = prepStatement.executeQuery();

			//If result found, friendship already exists, return true
			if (resultSet.isBeforeFirst()) {
				return true;
			}

			//Check to see if IDs were flipped (if they are friends but their ID's were stored
			//in the opposite fields
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, friendID);
			prepStatement.setInt(2, loggedInUserID);
			resultSet = prepStatement.executeQuery();

			if (resultSet.isBeforeFirst()) {
				return true;
			} else {
				return false;
			}
		} catch (java.sql.SQLException e){
			e.printStackTrace();
			return true;
		}
	}

	//Helper method for finding friends lists
	public ResultSet getFriends(int ID){
		connection.setAutoCommit(false);
		connect.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		int input;
		try {
			query = "Select unique userID, name from profile where userID = " +
					        "(select userID1 from friends where userID2 = " + loggedInUserID + ") or " +
					        "(select userID2 from friends where userID1 = " + loggedInUserID + ")";
			prepStatement = connection.prepareStatement(query);
			resultSet = prepStatement.executeQuery();

			return resultSet;

			if (!resultSet.isBeforeFirst()) {
				System.out.println("No friends found");
				return;
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	//Helper method for browsing the friends of a friend
	public void browseFriends(int ID){
		connection.setAutoCommit(false);
		connect.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		resultSet = getFriends(ID);
		if (resultSet = null){
			System.out.println("No friends found");
			return;
		}
		while(true) {
			while (resultSet.next()) {
				System.out.println("\nUser ID: " + resultSet.getInt(1));
				System.out.println("User Name: " + resultSet.getString(2));
			}

			System.out.println("Select the userID whose profile you'd like to see, or 0 to return your friends");
			input = s.nextInt();
			if (input == 0) {
				return;
			} else {
				printProfile(input);
			}
		}
	}

	public void printProfile(int ID){
		connection.setAutoCommit(false);
		connect.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		try {
			query = "SELECT name, date_of_birth, lastlogin from profile where userID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, ID);
			resultSet = prepStatement.executeQuery();
			if (!resultSet.next()) {
				System.out.println("Profile not found");
				return;
			}
			System.out.println(
				"Name: " + resultSet.getString(1) +
					"\nDate of Birth: " + resultSet.getDate(2) +
					"\nLast login: " + resultSet.getTimestamp(3));
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void disconnect() {
			try {
				connection.close();
			} catch (Exception E){
				System.out.println("Connection couldn't close: " + E.toString());
			}
		}
}

/*
	Deprecated supporting code for confirmFriends, purely for my own reference later - Tomasz
	//Get friend request count
				query = "Select count(fromID) from pendingFriends where toID = ?";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				resultSet = prepStatement.executeQuery();

				if (!resultSet.next()){
					System.out.println("No pending friend requests");
					friendRequests = false;
				}

				//Get group request count
				query = "Select count(gID) from pendingGroupMembers where toID = ?";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				resultSet = prepStatement.executeQuery();

				if (!resultSet.next()){
					System.out.println("No pending group requests");
					groupRequests = false;
					if (!groupRequests && !friendRequests){
						System.out.println("No group or friend requests, returning to main menu");
						return;
					}
				}

				//Check if friend requests exist
				if (friendRequests == true) {

					query = "Select fromID, message from pendingFriends where toID = ?";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, loggedInUserID);
					resultSet = prepStatement.executeQuery();

					while (resultSet.next()){
						System.out.println(counter++ + ".)");
						System.out.println("UserID: " + resultSet.getInt(1));
						System.out.println("Message: "+ resultSet.getString(2) + "\n");
					}
				}

				//Check if group requests exist
				if (groupRequests == true){

					query = "Select gID, message from pendingGroupMembers where toID = ?";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, loggedInUserID);
					resultSet = prepStatement.executeQuery();

					while (resultSet.next()){
						System.out.println(counter++ + ".)");
						System.out.println("GroupID: g" + resultSet.getInt(1));
						System.out.println("Message: " + resultSet.getString(2) + "\n");
					}
				}


				System.out.println("Please enter the ID of the request you'd like to accept, or 0 to exit." +
					"\nIf you're choosing a group invite, please type the negative of the group ID number");;

				if (choice == 0){
					System.out.println("Returning to main menu");
					return;
				} else if (choice > 0){
					if (checkIfFriend(choice)){
						System.out.println("Please only select choices from the menu");
						continue;
					}
					query = "Insert into friends values (?, ?, ?, ?)";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, loggedInUserID);
					prepStatement.setInt(2, choice);
					java.util.Date date = new Date()
					prepStatement.setDate(3, new java.sql.Date(date.getTime()));
					prepStatement.setString(4, );

					prepStatement.executeUpdate();
					connection.commit();
					System.out.println("Friend request successfully accepted");

					//Write trigger to remove pendingFriends entry
				} else if (choice < 0){
					query = "Insert into groupMembership values (?, ?, ?)";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, (choice*-1));
					prepStatement.setInt(2, loggedInUserID);
					prepStatement.setString(3, "member");

					prepStatement.executeUpdate();
					connection.commit();
					System.out.println("Group membership request successfully accepted");
				}
			}

 */