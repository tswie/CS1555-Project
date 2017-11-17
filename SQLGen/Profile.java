import java.sql.Date;
import java.sql.Timestamp;

class Profile{
	
	private final int userID;
	private final String name;
	private final String email;
	private final String password;
	private final String dateOfBirth;
	private final String lastlogin;
	
	public Profile(int userID, String name, String email, String password, String dateOfBirth, String lastlogin){
		
		this.userID = userID;
		this.name = name;
		this.email = email;
		this.password = password;
		this.dateOfBirth = dateOfBirth;
		this.lastlogin = lastlogin;
		
	}
	
	public String makeInsertStatement(){
		return "insert into profile(userID, name, email, password, date_of_birth, lastlogin) values(" + "'" + 
		userID + "', '" + 
		name + "', '" + 
		email + "', '" + 
		password + "', " + 
		"TO_DATE('" + dateOfBirth + "', 'YYYY-MM-DD')," +
		"TO_TIMESTAMP('" + lastlogin + "', 'YYYY-MM-DD HH24:MI.SS.FF'));";
		
	}
	
	public int getUserID(){
		return userID;
	}
}