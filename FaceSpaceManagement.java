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
			ve.printStackTrace();
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
			System.out.println("Logging in");
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
		int friendRequests = 0;
		int groupRequests = 0;

		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			while(true) {
				query = "Select count(*) from pendingFriends where toID = ? ";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				resultSet = prepStatement.executeQuery();
				resultSet.next();
				friendRequests = resultSet.getInt(1);

				query = "Select count(*) from pendingGroupMembers where gID = (Select unique gID from groupMembership where userID = ? and role" +
					        "= 'manager')";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				resultSet = prepStatement.executeQuery();
				resultSet.next();
				groupRequests = resultSet.getInt(1);

				if (groupRequests == 0 && friendRequests == 0) {
					System.out.println("You have no outstanding group or friend invitations left.\n");
					connection.commit();
					return;
				}

				System.out.println("You have " + friendRequests + " friend requests and " + groupRequests + " group requests");

				Listing[] listings = new Listing[groupRequests + friendRequests];

				query = "Select fromID, message from pendingFriends where toID = ? for update";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				resultSet = prepStatement.executeQuery();

				int i = 0;

				while (resultSet.next()) {
					listings[i] = new Listing('f', resultSet.getInt(1), resultSet.getString(2));
					i++;
				}

				query = "Select userID, message, gID from pendingGroupMembers where gID = ANY(Select unique gID from groupMembership" +
					       " where userID = ? and role = 'manager')";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				resultSet = prepStatement.executeQuery();

				while (resultSet.next()) {
					listings[i] = new Listing('g', resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3));
					i++;
				}

				/*
				Takes the array of requests and prints them based on type.
				 */
				for (i = 0; i < listings.length; i++) {
					System.out.print("\n" + (i +1) + "). ");
					if (listings[i].getType() == 'f') { System.out.println("FRIEND REQUEST");
						System.out.println("FROM " + getProfileName(listings[i].getId()) + "with user ID: " +
						listings[i].getId());
						System.out.println("MESSAGE: " + listings[i].getMessage());
					} else {
						System.out.println("GROUP MEMBERSHIP REQUEST");
						System.out.println("FROM " + getGroupName(listings[i].getGroupId()) + " with userID ID: " +
						listings[i].getId());
						System.out.println("MESSAGE: " + listings[i].getMessage());
					}
				}

				System.out.println("\nPlease enter the listing you'd like to accept, or type -1 to accept them all.\n" +
					                   "To reject an option, merely leave the menu and all remaining options will be rejected." +
					                   "To leave the menu, type 0.");
				choice = s.nextInt();
				if (choice == 0){
					rejectListings(listings);
					connection.commit();
					return;
				} else if (choice == -1){
					acceptListings(listings);
					connection.commit();
					return;
				} else if (choice < listings.length+1 && choice > 0){
					acceptSingleListing(listings[i-1]);
					continue;
				} else {
					System.out.println("Please choose one of the options from the list.");
				}


			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}
		/*
		Accepts a single one of the menu listings when confirming requests and processes it.
		 */
	public void acceptSingleListing(Listing listing){
		try{

			if (listing.getType() == 'f'){
				query = "INSERT INTO FRIENDS VALUES (?, ?, LOCALTIMESTAMP(6), ?)";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				prepStatement.setInt(2, listing.getId());
				prepStatement.setString(3, listing.getMessage());
				prepStatement.executeQuery();
				connection.commit();
				System.out.println("\nFriend request successfully accepted");
			} else {
				query = "insert into groupMembership values (?, ?, 'member')";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, listing.getGroupId());
				prepStatement.setInt(2, listing.getId());
				prepStatement.executeQuery();
				connection.commit();
				System.out.println("\nGroup request successfully accepted");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	/*
	Accepts all listings in the array of listings.
	 */

	public void acceptListings(Listing[] listings){
		int i = 0;
		try {
			for (i = 0; i < listings.length; i++){
				if (listings[i].getType() == 'f'){
					query = "INSERT INTO FRIENDS VALUES (?, ?, LOCALTIMESTAMP(6), ?)";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, loggedInUserID);
					prepStatement.setInt(2, listings[i].getId());
					prepStatement.setString(3, listings[i].getMessage());
					prepStatement.executeQuery();
 				} else {
					query = "INSERT INTO GROUPMEMBERSHIP VALUES(?, ?, ?)";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, listings[i].getGroupId());
					prepStatement.setInt(2, listings[i].getId());
					prepStatement.setString(3, "member");
					prepStatement.executeQuery();
				}
			}
			connection.commit();
			System.out.println("All requests successfully accepted");
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	/*
	Rejects all the listings in the array
	 */
	public void rejectListings(Listing[] listings){
		int i = 0;
		try {

			for (i = 0; i < listings.length; i++){
				if (listings[i].getType() == 'f'){
					query = "DELETE FROM PENDINGFRIENDS WHERE TOID = ? AND FROMID = ?";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, loggedInUserID);
					prepStatement.setInt(2, listings[i].getId());
					prepStatement.executeQuery();
					connection.commit();
				} else {
					query = "DELETE FROM PENDINGGROUPMEMBERS WHERE TOID = ? AND GID = ?";
					prepStatement = connection.prepareStatement(query);
					prepStatement.setInt(1, loggedInUserID);
					prepStatement.setInt(2, listings[i].getId());
					prepStatement.executeQuery();
					connection.commit();
				}
			}
			connection.commit();
			System.out.println("All requests successfully rejected.");
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	/*
	Prints out a list of the user's friends. Upon selecting one of the friends, the user can then
	view THEIR friends or view this person's profile. This does not support the friends of the friends
	of a friend however.
	 */
	public synchronized void displayFriends(){
		int counter = 1;
		boolean loop = true;
		int input;
		int choice;

		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (Exception e){
			e.printStackTrace();
		}

		while(true) {
			try {

				resultSet = getFriends(loggedInUserID);

				if (resultSet == null){
					System.out.println("No friends found");
					return;
				}
				if (!resultSet.isBeforeFirst()) {
					System.out.println("No friends found");
					return;
				}
				System.out.println("--------------\nHere are your friends: \n");
				while (resultSet.next()) {
					//System.out.println(counter++ + ")");
					System.out.println("UserID: " +
						resultSet.getInt(1));
					System.out.println("Name: " +
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
						"3.)Go back to browsing your friends\n");
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

	/*
	This is handled in confirm users

	public void confirmGroupMembers() {

	}
	*/

	public synchronized void initiateAddingGroup(){
		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			int id;
			String message;
			while (true){
				System.out.println("Please enter the ID of the group you would like to join, or 0 to exit.");
				id = s.nextInt();
				if (id == 0){
					return;
				}
				query = "select * from groups where gID = ?";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, id);
				resultSet = prepStatement.executeQuery();

				if (!resultSet.isBeforeFirst()){
					continue;
				}

				System.out.println("Please enter the message you'd like to accompany the request.");
				message = s.nextLine();
				message = s.nextLine();
				query = "insert into pendingGroupMembers values (?,?,?)";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, id);
				prepStatement.setInt(2, loggedInUserID);
				prepStatement.setString(3, message);

				prepStatement.executeUpdate();
				connection.commit();
				return;


			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}

	public synchronized void sendMessageToUser(){
		try {
			int userID;
			connection.setAutoCommit(false); //We want every createUser call to be its own transaction
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();

			System.out.print("Enter in the userID of the person you want to send a message to : ");
			userID = s.nextInt();

			query = "select name from profile where userID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setString(1, String.valueOf(userID));
			resultSet = prepStatement.executeQuery();

			resultSet.next();
			String name = resultSet.getString(1);
			System.out.printf("\nSending Message to %s\n", name);

			StringBuilder message = new StringBuilder("");
			String line = "";
			System.out.print("Message (Enter -1 to finish): ");
			while(!line.equals("-1")) {

				line = s.nextLine();

				if(!line.equals("-1")) {
					message.append(line);
				}
			}

			query = "SELECT max(msgID) from messages";
			resultSet = statement.executeQuery(query);
			resultSet.next();
			int newMsgID = resultSet.getInt(1);
			newMsgID++;
			System.out.println(newMsgID);

			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			//Calendar c = Calendar.getInstance();
			StringBuilder date = new StringBuilder("");
			//date.append(c.get(c.YEAR) + "-");
			//date.append(c.get(Calendar.MONTH)+1 + "-");
			//date.append(c.get(Calendar.DAY_OF_MONTH));
			//System.out.println("My date : " + date);
			//System.out.println("Cal date : " + c.get(c.DATE));

			query = "insert into messages(msgID, fromID, message, toUserID, dateSent) values (?, ?, ?, ?, LOCALTIMESTAMP(6))";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, newMsgID);
			prepStatement.setInt(2, loggedInUserID);
			prepStatement.setString(3, message.toString());
			prepStatement.setInt(4, userID);
			//prepStatement.setDate(5, df.parse(date.toString()));
			prepStatement.executeUpdate();
			connection.commit();
		}
		catch(Exception e) {
			System.out.println("Failed to send message");
			e.printStackTrace();
		}
	}

	public synchronized void sendMessageToGroup(){
		try {
			connection.setAutoCommit(false); //We want every createUser call to be its own transaction
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();

			System.out.print("Enter in the ID of the group you want to send a message to : ");
			int groupID = s.nextInt();
			query = "select gID from groupmembership where userID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, loggedInUserID);
			resultSet = prepStatement.executeQuery();

			if (!resultSet.isBeforeFirst()){
				System.out.println("Your not in the group");
				return;
			}
			else {
				System.out.printf("\nSending Message to group %s\n", getGroupName(groupID));

				StringBuilder message = new StringBuilder("");
				String line = "";
				System.out.print("Message (Enter -1 to finish): ");
				while(!line.equals("-1")) {

					line = s.nextLine();

					if(!line.equals("-1")) {
						message.append(line);
					}
				}

				query = "SELECT max(msgID) from messages";
				resultSet = statement.executeQuery(query);
				resultSet.next();
				int newMsgID = resultSet.getInt(1);
				newMsgID++;
				System.out.println(newMsgID);

				query = "select userID from groupmembership where gID = ?";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, groupID);
				resultSet = prepStatement.executeQuery();
				ArrayList<Integer> groupMemberIDs = new ArrayList<Integer>();

				while(resultSet.next()) {

						query = "insert into messages values (?, ?, ?, ?, ?, LOCALTIMESTAMP(6))";
						prepStatement = connection.prepareStatement(query);
						prepStatement.setInt(1, newMsgID);
						System.out.println("Creating a new message with msgID: " + newMsgID);
						prepStatement.setInt(2, loggedInUserID);
						prepStatement.setString(3, message.toString());
						prepStatement.setInt(4, resultSet.getInt(1));
						prepStatement.setInt(5, groupID);
						prepStatement.executeUpdate();
						newMsgID++;
				}

				connection.commit();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Failed to send message to group");
		}
	}

	public synchronized void displayMessages(){
		try {

			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			query = "Select fromID, message from messages where toUserID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, loggedInUserID);
			resultSet = prepStatement.executeQuery();

			if (!resultSet.isBeforeFirst()){
				System.out.println("No messages found");
				return;
			}

			while (resultSet.next()){
				System.out.println("\nFrom userID: " + resultSet.getInt(1));
				System.out.println("Message contents: " + resultSet.getString(2));
			}
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public synchronized void displayNewMessages(){
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			query = "Select lastlogin from profile where userID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, loggedInUserID);
			resultSet = prepStatement.executeQuery();
			java.sql.Timestamp login;

			if (!resultSet.next()){
				displayMessages();
			} else {
				login = resultSet.getTimestamp(1);
				query = "Select fromID, message from messages where toUserID = ? and dateSent > ?";
				prepStatement = connection.prepareStatement(query);
				prepStatement.setInt(1, loggedInUserID);
				prepStatement.setTimestamp(2, login);
				resultSet = prepStatement.executeQuery();

				if (!resultSet.isBeforeFirst()){
					System.out.println("\nNo new messages. \n");
					return;
				} else {
					while (resultSet.next()){
						System.out.println("\nFrom userID: " + resultSet.getInt(1));
						System.out.println("Message contents: " + resultSet.getString(2));
					}
					return;
				}
			}

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void searchForUser(){
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = connection.createStatement();

			query = "select userID, name, email, date_of_birth from profile";
			ResultSet resultSet = statement.executeQuery(query);
			StringBuilder sb = new StringBuilder("");

			System.out.print("\nSearch for a user: ");
			String pat = s.nextLine();
			System.out.printf("Profiles that match: %s\n", pat);


			while(resultSet.next()) {
				sb.append(resultSet.getInt(1) + " ");
				sb.append(resultSet.getString(2) + " ");
				sb.append(resultSet.getString(3) + " ");
				sb.append(resultSet.getString(4));

				if(sb.lastIndexOf(pat) != -1 && resultSet.getInt(1) != loggedInUserID) {
					System.out.println("\n" + sb.toString());
				}

				sb = new StringBuilder("");
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void threeDegrees(){

	}

	public void topMessages(){
		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			query = "select fromID, count(fromID) as most_common from messages group by fromID order by count(*) desc";
			prepStatement = connection.prepareStatement(query);
			resultSet = prepStatement.executeQuery();

			resultSet.next();
			System.out.println("The user with the most messages sent is " + getProfileName(resultSet.getInt(1)));
			return;
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/*
	Most of the work is done using the dropuser trigger in spdb.sql
	 */
	public int dropUser(){
		int choice;
		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			query = "Delete from profile where userID = ?";
			System.out.println("Enter the ID of the user you'd like to delete?");
			choice = s.nextInt();
			if (choice == loggedInUserID){
				return -1;
			}

			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, choice);
			prepStatement.executeUpdate();
			connection.commit();
			return 0;
		} catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}

	public void logOut() {
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			query = "Update profile set lastlogin = LOCALTIMESTAMP(6) where userID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, loggedInUserID);
			prepStatement.executeUpdate();
			connection.commit();
			loggedInUserID = 0;
			System.out.println("Log-out successful.");
		} catch (Exception e){
			e.printStackTrace();
		}
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

		int input;
		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			query = "Select unique userID, name from profile where userID = ANY " +
					        "(select userID1 from friends where userID2 = ?) or " +
					        "userID = ANY(select userID2 from friends where userID1 = ?)";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1, ID);
			prepStatement.setInt(2, ID);
			resultSet = prepStatement.executeQuery();

			if (!resultSet.isBeforeFirst()) {
				System.out.println("No friends found");
				return null;
			} else {
				return resultSet;
			}
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	//Helper method for browsing the friends of a friend
	public void browseFriends(int ID){
		resultSet = getFriends(ID);
		int input;
		if (resultSet == null){
			System.out.println("No friends found");
			return;
		}

		try{
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		} catch (Exception e){
			e.printStackTrace();
		}
		try {
			while (true) {

				while (resultSet.next()) {
					System.out.println("\nUser ID: " + resultSet.getInt(1));
					System.out.println("User Name: " + resultSet.getString(2));
				}

				System.out.println("\nSelect the userID whose profile you'd like to see, or 0 to return your friends");
				input = s.nextInt();
				if (input == 0) { return;
				} else { printProfile(input);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	public String getGroupName(int ID){
		try {
			query = "Select name from groups where gID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1,ID);
			resultSet = prepStatement.executeQuery();

			if (!resultSet.next()){
				return "GROUP NOT FOUND HELP!";
			}
			return resultSet.getString(1);
		} catch (Exception e){
			e.printStackTrace();
		}
		return "Something goofed";
	}

	public String getProfileName(int ID){
		try {
			query = "Select name from profile where userID = ?";
			prepStatement = connection.prepareStatement(query);
			prepStatement.setInt(1,ID);
			resultSet = prepStatement.executeQuery();

			if (!resultSet.next()){
				return "PROFILE NOT FOUND HELP!";
			}
			return resultSet.getString(1);
		} catch (Exception e){
			e.printStackTrace();
		}
		return "Something goofed";
	}

	public void printProfile(int ID){

		try {
			connection.setAutoCommit(false);
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
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

	private class Listing{
		char type;
		int id;
		int gid;
		String message;

		Listing(char type, int id, String message){
			this.type = type;
			this.id = id;
			this.message = message;
		}

		Listing (char type, int id, String message, int gid){
			this.type = type;
			this.id = id;
			this.message = message;
			this.gid = gid;
		}

		char getType(){
			return type;
		}

		int getId(){
			return id;
		}

		int getGroupId(){
			return gid;
		}

		String getMessage(){
			return message;
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
