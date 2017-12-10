import java.util.Scanner;

public class SocialPanther {

  private static Scanner keyboard = new Scanner(System.in);
  private static FaceSpaceManagement fsm;

  public static void main(String[] args) {

    connectDB();
    int choice = 0;
    boolean done = false;

    while(true) {
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
            fsm.disconnect();
            System.exit(0);
          default:
            System.out.println("Pick one of the options!\n");
        }
      }while(!done);

      do {
        mainMenu();

        System.out.print("Pick an option: ");
        choice = keyboard.nextInt();

        switch(choice) {

          case 1:
            fsm.initiateFriendship();
            break;
          case 2:
            fsm.confirmFriendship();
            break;
          case 3:
            fsm.displayFriends();
            break;
          case 4:
            fsm.createGroup();
            break;
          case 5:
            fsm.initiateAddingGroup();
            break;
          case 6:
            fsm.sendMessageToUser();
            break;
          case 7:
            fsm.sendMessageToGroup();
            break;
          case 8:
            fsm.displayMessages();
            break;
          case 9:
            fsm.displayNewMessages();
            break;
          case 10:
            fsm.searchForUser();
            break;
          case 11:
            fsm.threeDegrees();
            break;
          case 12:
            fsm.topMessages();
            break;
          case 13:
            fsm.dropUser();
            fsm.logOut();
            break;
          case 14:
            fsm.logOut();
            break;
          default:
            System.out.println("Pick one of the options!\n");
        }
      }while(choice != 14 && choice!=13);
    }
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

  public static void mainMenu() {

    System.out.println("\n1. Start New Friendship");
    System.out.println("2. Confirm Friendship and Group Members");
    System.out.println("3. Display Friends");
    System.out.println("4. Create a Group");
    System.out.println("5. Request to be Added to a Group");
    System.out.println("6. Send Message to User");
    System.out.println("7. Send Message to Group");
    System.out.println("8. Display Messages");
    System.out.println("9. Display New Messages");
    System.out.println("10. Search for User");
    System.out.println("11. Check for a Path");
    System.out.println("12. Print Top Messages");
    System.out.println("13. Drop User");
    System.out.println("14. Log Out\n");
  }
}
