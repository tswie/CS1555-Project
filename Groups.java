
class Groups{
	
	private final int gID;
	private final String name;
	private final String description;
	
	
	public Groups(int gID, String name, String desc){
		this.gID = gID;
		this.name = name;
		this.description = desc;
	}
	
	public String makeInsertStatement(){
		return "insert into groups(gID, name, description) values ('" +
		gID + "', '" +
		name + "', '" + 
		description + "');";
	}
}