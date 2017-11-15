//Need 100 users, 200 friendships, 10 groups, 300 messages

import java.util.ArrayList;
import java.util.Random;

class Main{
	public static void main(String[] args){
		
		
		private static final String[] firstNames       = {"Tim", "Tom", "Mary", "Fred", "Joanne", "Kim", "Mark", "Leeroy",
                "Brittany", "Martha", "Eileen", "Eleanor", "Linda", "Larry",
                "Sebastian", "Seymour", "Jennifer", "Ellie", "Selene", "Carly",
                "David", "James", "John", "Bill", "Daniel"};
		
		private static final String[] lastNames        = {"Johnson", "Jenkins", "Smith", "Williams", "Brown", "Jones",
                "Moore", "Anderson", "Jackson", "Davis", "Miller", "Wilson",
                "Harris", "Martin", "Thompson", "Garcia", "Lopez", "Valladares",
                "Sharma", "Suh", "Rogers", "Vick", "Griffin", "Laboon", "Mosse"};
		
		//Generate users
		ArrayList<Profile> users = new ArrayList<Profile>();
		
		//Match them up to make random friendships
		ArrayList<Friends> friendships = new ArrayList<Friends>();
		
		//Create groups, no need to populate
		ArrayList<Groups> groups = new ArrayList<Groups>();
		
		//Create messages, not necessarily between friends
		ArrayList<Messages> messages = new ArrayList<Messages>();
		
	}
	
	private String getRandomName() {
		
	}
	
	private String getRandomEmail() {
		Random r = new Random();
		char c;
		StringBuilder email = new StringBuilder() {
			for (int i = 0; i<3, i++) {
				c = (char)(r.nextInt(26) + 'a');
				email.append(c);
			}
		}
	}
	
	private String getRandomPassword() {
		
	}
	
	private String getRandomDateOfBirth() {
		
	}
	
	private String getRandomLogin() {
		
	}
}