//Need 100 users, 200 friendships, 10 groups, 300 messages

import java.util.ArrayList;
import java.util.Random;
import java.sql.Date;

class Main{
	
	private static final String[] firstNames = {"Tim", "Tom", "Mary", "Fred", "Joanne", "Kim", "Mark", "Leeroy",
                "Brittany", "Martha", "Eileen", "Eleanor", "Linda", "Larry",
                "Sebastian", "Seymour", "Jennifer", "Ellie", "Selene", "Carly",
                "David", "James", "John", "Bill", "Daniel"};
		
	private static final String[] lastNames = {"Johnson", "Jenkins", "Smith", "Williams", "Brown", "Jones",
            "Moore", "Anderson", "Jackson", "Davis", "Miller", "Wilson",
            "Harris", "Martin", "Thompson", "Garcia", "Lopez", "Valladares",
            "Sharma", "Suh", "Rogers", "Vick", "Griffin", "Laboon", "Mosse"};
	
	public static void main(String[] args){
		
		Random r = new Random();
		
		//Generate users
		Profile[] profiles = new Profile[100];
		for (int i = 0; i<profiles.length; i++){
			profiles[i] = new Profile(i+1, getRandomName(),getRandomEmail(),getRandomPassword(), getRandomDateOfBirth(), getRandomLogin());
		}
		for (int i =0; i<profiles.length; i++){
			System.out.println(profiles[i].makeInsertStatement());
		}
		
		boolean[][] pairings = new boolean[101][101];
		//Match them up to make random friendships
		Friends[] friends = new Friends[200];
		for (int i = 0; i<100; i++){
			int friendA = (i+1)%101;
			if (friendA == 0){
				friendA++;
			}
			int friendB = (friendA+1)%101;
			if (friendB==0){
				friendB++;
			}
			friends[i] = new Friends(friendA, friendB++, getRandomJDate(), "Hello, I would like to be your friend");
			System.out.println(friends[i++].makeInsertStatement());
			if (friendB == 101){
				friendB = 1;
			}
			if (i== 200){
				break;
			}
			friends[i] = new Friends(friendA, friendB, getRandomJDate(), "Hello, I would like to be your friend");
			System.out.println(friends[i--].makeInsertStatement());
			
		}
		
		//Create groups, no need to populate
		Groups[] groups = new Groups[10];
		for (int i = 0; i<groups.length; i++){
			groups[i] = new Groups(i+1, getRandomMessage(20), getRandomMessage(80));
			System.out.println(groups[i].makeInsertStatement());
		}
		
		//Create messages, not necessarily between friends
		Messages[] messages = new Messages[100];
		for (int i = 0; i<messages.length; i++){
			int k = r.nextInt(100) + 1;
			int j = k;
			while (j == k){
				j = r.nextInt(100) + 1;
			}
			messages[i] = new Messages(i+1, k , getRandomMessage(50), j, getRandomJDate());
			System.out.println(messages[i].makeInsertStatement());
		}
		
		
	}
	
	private static String getRandomName() {
		Random r = new Random();
		StringBuilder name = new StringBuilder();
		
		name.append(firstNames[r.nextInt(firstNames.length)]);
		name.append(" ");
		name.append(lastNames[r.nextInt(lastNames.length)]);
		//System.out.println(name);
		
		return name.toString();
		
	}
	
	private static String getRandomEmail() {
		Random r = new Random();
		char c;
		StringBuilder email = new StringBuilder();
		for (int i = 0; i<3; i++) {
			c = (char)(r.nextInt(26) + 'a');
			email.append(c);
		} 
		c = (char)(r.nextInt(10) + '0');
		email.append(c);
		c = (char)(r.nextInt(10) + '0');
		email.append(c);
		email.append("@pitt.edu");
		
		//System.out.println(email);
		
		return email.toString();
	}
	
	private static String getRandomPassword() {
		Random r = new Random();
		char c;
		StringBuilder password = new StringBuilder();
		for (int i = 0; i<10; i++){
			c = (char) (r.nextInt(26) + 'a');
			password.append(c);
		}

		String s = password.toString();
		return s;
	}
	
	private static String getRandomDateOfBirth() {
		Random r = new Random();
		StringBuilder dob = new StringBuilder();
		dob.append('1');
		dob.append('9');
		dob.append(r.nextInt(9));
		dob.append(r.nextInt(9));
		dob.append('-');
		dob.append('0');
		dob.append(r.nextInt(8)+1);
		dob.append('-');
		dob.append(r.nextInt(2));
		dob.append(r.nextInt(8)+1);
		return dob.toString();
		//System.out.println(dob);
	}
	
	private static String getRandomLogin() {
		Random r = new Random();
		StringBuilder lastlogin = new StringBuilder();
		lastlogin.append('2');
		lastlogin.append('0');
		lastlogin.append('0');
		lastlogin.append(r.nextInt(9));
		lastlogin.append('-');
		lastlogin.append('0');
		lastlogin.append(r.nextInt(8)+1);
		lastlogin.append('-');
		lastlogin.append(r.nextInt(2));
		lastlogin.append(r.nextInt(8)+1);
		
		lastlogin.append(' ');
		lastlogin.append("00:00.01.010000");
		
		return lastlogin.toString();
		//System.out.println(lastlogin);
	}
	
	private static String getRandomMessage(int l){
		Random r = new Random();
		StringBuilder message = new StringBuilder();
		char c;
		for (int i = 0; i<l;i++){
			c = (char)(r.nextInt(26) + 'a');
			message.append(c);
		}
		return message.toString();
	}
	
	private static String getRandomJDate(){
		Random r = new Random();
		StringBuilder dob = new StringBuilder();
		dob.append('2');
		dob.append('0');
		dob.append('0');
		dob.append(r.nextInt(9));
		dob.append('-');
		dob.append('0');
		dob.append(r.nextInt(8)+1);
		dob.append('-');
		dob.append(r.nextInt(2));
		dob.append(r.nextInt(8)+1);
		return dob.toString();
		//System.out.println(dob);
	}
	
	
}