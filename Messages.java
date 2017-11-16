import java.sql.Date;

class Messages{
	
	private final int msgID;
	private final int fromID;
	private final String message;
	private final int toID;
	private final String toGroupID;
	private final String date;
	
	public Messages(int msgID, int fromID, String message, int toID, String date){
		this.msgID = msgID;
		this.fromID = fromID;
		this.message = message;
		this.toID = toID;
		this.date = date;
		this.toGroupID = null;
	}
	
	public String makeInsertStatement(){
		return "insert into messages(msgID,fromID,message,toUserID,dateSent) values ('" +
		msgID + "', '" +
		fromID + "', '" +
		message + "', '" +
		toID + "', " +
		"TO_DATE('" + date + "', 'YYYY-MM-DD'));";
	}
	
}