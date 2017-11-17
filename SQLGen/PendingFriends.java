class pendingFriends{
	
	private final String fromID;
	private final String toID;
	private final String message;
	
	pendingFriends(String from, String to, String message){
		this.fromID = from;
		this.toID = to;
		this.message = message;
	}
}