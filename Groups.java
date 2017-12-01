
class Groups{
	
	private final int gID;
	private final String name;
	private final String description;
	private final int glimit;
	
	
	public Groups(int gID, String name, String desc, int glimit){
		this.gID = gID;
		this.name = name;
		this.description = desc;
		this.glimit = glimit;
	}
	
	public String makeInsertStatement(){
		return "insert into groups(gID, name, description, glimit) values ('" +
		gID + "', '" +
		name + "', '" + 
		description + "', '" +
		glimit + "');";
	}
}