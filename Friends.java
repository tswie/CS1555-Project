import java.sql.Date;

class Friends{
	
	private final int userIDOne;
	private final int userIDTwo;
	private final String jdate;
	private final String message;
	
	public Friends(int one, int two, String date, String message){
		this.userIDOne = one;
		this.userIDTwo = two;
		this.jdate = date;
		this.message = message;
	}
	
	public String makeInsertStatement(){
		return "insert into friends(userID1,userID2,JDate,message) values ('" +
		userIDOne + "', '"+
		userIDTwo + "', "+
		"TO_DATE('" + jdate +"', 'YYYY-MM-DD'),'" + 
		message + "');";
	}
	
}