import java.util.Scanner;

public class SocialPanther {

  private static Scanner keyboard = new Scanner(System.in);
  private static FaceSpaceManagement fsm;

  public static void main(String[] args) {

    connectDB();
    int choice = 0;
    boolean done = false;

    do {
      logInMenu();

      System.out.print("Pick an option: ");
      choice = keyboard.nextInt();

      switch(choice) {

        case 1:
          done = fsm.logIn();
          break;
        case 2:
          done = fsm.createUser();
          break;
        case 3:
          System.exit(0);
        default:
          System.out.println("Pick one of the options!\n");
      }
    }while(!done);
  }

  public static void connectDB() {

    String username, password;

    System.out.println("Welcome to Social Panther.\n");
    System.out.println("Please enter your username and password to connect to the database.");
    System.out.print("Username : ");
    username = keyboard.nextLine();
    System.out.print("password : ");
    password = keyboard.nextLine();

    fsm = new FaceSpaceManagement(username, password);
  }

  public static void logInMenu() {

    System.out.println("\n1. Log In");
    System.out.println("2. Create a New User");
    System.out.println("3. Quit\n");
  }
}
