import java.sql.Date;
import java.sql.Timestamp;

class Profile{
	
	private final String userID;
	private final String name;
	private final String email;
	private final String password;
	private final Date dateOfBirth;
	private final Timestamp lastlogin;
	
	public Profile(String userId, String name, String email, String password, Date dateOfBirth, Timestamp lastlogin){
		
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
		name + ", '" + 
		email + ", '" + 
		password + "', '" + 
		dateOfBirth + "', '" +
		lastlogin + "');"
		
	}
}