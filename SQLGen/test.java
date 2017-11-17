import java.util.Random;
import java.sql.Date;

class test{
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
		
		System.out.println(new Date((long) Math.abs(r.nextInt(18000) * 86400000.0)));
	}		
}
