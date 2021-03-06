/* 
   J. Randomgeek
   CSC 365 Project A UI
*/

import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.math.*;

// main function. Contains main program loop
public class InnReservations {
   public static Connection conn;

   private static void createConnection() {
      // Read from file
      Scanner serverSettingsScaller;
      String address = null;
      String username = null;
      String password = null;

      try {
         serverSettingsScaller = new Scanner(new File("ServerSettings.txt"));
         address = serverSettingsScaller.nextLine();
         username = serverSettingsScaller.nextLine();
         password = serverSettingsScaller.nextLine();
      }
      catch (Exception e) {
         System.err.println("Error reading file \'ServerSettings.txt\'. Please make sure the file exists and is formatted as per the spec.");
         System.exit(1);
      }


      // Load JDBC driver
      try {
         Class.forName("com.mysql.jdbc.Driver").getDeclaredConstructor().newInstance();
      }
      catch (Exception e) {
         System.err.println("Error loading JDBC driver. Did you compile the program correctly?");
         System.exit(1);
      }

      // Make mysql connection
      try {
         conn = DriverManager.getConnection(address, username, password);
      }
      catch (Exception e) {
         System.err.println("Error connecting to mysql. Credentials provided in \'ServerSettings.txt\' might be incorrect.");
         System.exit(1);
      }
   }

   private static void checkTablesExist() {
      String rooms = "CREATE TABLE IF NOT EXISTS rooms LIKE INN.rooms;";
      String reservations = "CREATE TABLE IF NOT EXISTS reservations LIKE INN.reservations;";

      try {
         PreparedStatement roomStatement = conn.prepareStatement(rooms);
         PreparedStatement reservationsStatement = conn.prepareStatement(reservations);

         roomStatement.execute();
         reservationsStatement.execute();
      }
      catch (Exception e) {
         System.err.println("Couldn't create tables;");
         System.err.println(e);
      }
   }

   // enter main program loop
   public static void main(String args[]) {

      createConnection();
      checkTablesExist();

      boolean exit = false;
      Scanner input = new Scanner(System.in);

      // clear the screen to freshen up the display
      //clearScreen();
      while (!exit) {
	      displayMain();

         char option = input.nextLine().toLowerCase().charAt(0);

         switch(option) {
            case 'a':   clearScreen();
                        Admin.adminLoop();
               break;
            case 'o':   clearScreen();
                        Owner.ownerLoop();
               break;
            case 'g':   guestLoop();
               break;
            case 'q':   exit = true;
               break;
         }
      }

      input.close();
   }

   // Main UI display
   private static void displayMain() {
      // Clear the screen
      // clearScreen();

      // Display UI
      System.out.println("Welcome. Please choose your role:\n\n"
         + "- (A)dmin\n"
         + "- (O)wner\n"
         + "- (G)uest\n"
         + "- (Q)uit\n");
   }



   // Program loop for guest subsystem
   private static void guestLoop() {
      boolean exit = false;
      Scanner input = new Scanner(System.in);

      while (!exit) {
         displayGuest();

         char option = input.next().toLowerCase().charAt(0);

         switch(option) {
            case 'r':   System.out.println("roomsAndRates\n");
                        break;
            case 's':   System.out.println("viewStays\n");
                        break;
            case 'b':   exit = true;
                        break;
         }
      }
   }

   // Guest UI display
   private static void displayGuest() {
      // Clear the screen
      // clearScreen();

      // Display UI
      System.out.println("Welcome, Guest.\n\n"
         + "Choose an option:\n"
         + "- (R)ooms - View rooms and rates\n"
         + "- (S)tays - View availability for your stay\n"
         + "- (B)ack - Goes back to main menu\n");
   }

   // Clears the console screen when running interactive
   public static void clearScreen() {
      Console c = System.console();
      if (c != null) {

         // Clear screen for the first time
         System.out.print("\033[H\033[2J");
         System.out.flush();
         //c.writer().print(ESC + "[2J");
         //c.flush();

         // Clear the screen again and place the cursor in the top left
         System.out.print("\033[H\033[1;1H");
         System.out.flush();
         //c.writer().print(ESC + "[1;1H");
         //c.flush();
      }
   }


   // Get a date from input
   public static String getDate() {
      Scanner input = new Scanner(System.in);

      String monthName = input.next().toLowerCase();

      if (monthName.equals("null")) {
         return null;
      }
      int month = monthNum(monthName);
      int day = input.nextInt();
      String date = "2010-" + month + "-" + day;
      return date;
   }

   // Convert month name to month number
   private static int monthNum(String month) {
      switch (month) {
         case "january": return 1;
         case "february": return 2;
         case "march": return 3;
         case "april": return 4;
         case "may": return 5;
         case "june": return 6;
         case "july": return 7;
         case "august": return 8;
         case "september": return 9;
         case "october": return 10;
         case "november": return 11;
         case "december": return 12;
      }

      return 0;
   }
 
   // ask how many dates will be entered
   private static int getNumDates() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter number of dates (1 or 2): ");

      int numDates = input.nextInt();
      while (numDates != 1 && numDates != 2) {
         System.out.print("Enter number of dates (1 or 2): ");
         numDates = input.nextInt();
      }
      return numDates;
   }


   // get the room code or a 'q' response to back up the menu
   public static String getRoomCodeOrQ() {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter room code for more details "
	 + "(or (q)uit to exit): ");
      String roomCode = input.next();
      return roomCode;
   }


   // get the reservation code or a 'q' response to back up the menu
   public static String getReservCodeOrQ() {
      Scanner input = new Scanner(System.in);
      System.out.print("Enter reservation code for more details "
	 + "(or (q)uit to exit): ");
      String rvCode = input.next();
      return rvCode;
   }


   // Revenue and volume data subsystem -- option to continue or quit
   private static char revenueData() {
      Scanner input = new Scanner(System.in);
      char opt;
      System.out.print("Type (c)ount, (d)ays, or (r)evenue to view "
    + "different table data (or (q)uit to exit): ");
      opt = input.next().toLowerCase().charAt(0);

	   return opt;
   }



   // potentially useful for Rooms Viewing Subsystem -- gets option to
   // view room code or reservations room code or exit
   private static String viewRooms() {
      Scanner input = new Scanner(System.in);
	    System.out.print("Type (v)iew [room code] or "
	    + "(r)eservations [room code], or (q)uit to exit: ");

	   char option = input.next().toLowerCase().charAt(0);
	   String roomCode = String.valueOf(option);
	   if (option != 'q')
	      roomCode = roomCode + " '" + input.next() + "'";
	   return roomCode;
   }

   // ask user if they wish to quit
   private static char askIfQuit() {
      Scanner input = new Scanner(System.in);

	   System.out.print("Enter (q)uit to quit: ");
	   char go = input.next().toLowerCase().charAt(0);

	   return go;
   }


   // ask user if they wish to go back
   private static char askIfGoBack() {
      Scanner input = new Scanner(System.in);

	   System.out.print("Enter (b)ack to go back: ");
	   char go = input.next().toLowerCase().charAt(0);

	   return go;
   }


   // potentially useful for check availability subsystem
   private static char availabilityOrGoBack() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter (a)vailability, or "
	 + "(b)ack to go back: ");
      char option = input.next().toLowerCase().charAt(0);

      return option;
   }

   // Check availability subsystem:
   // ask if they want to place reservation or renege
   private static char reserveOrGoBack() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter (r)eserve to place a reservation, "
	 + "or (b)ack to go back: ");
      char option = input.next().toLowerCase().charAt(0);

      return option;
   }

   // Get the user's first name (for making a reservation)
   private static String getFirstName() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter your first name: ");
      String firstName = "'" + input.next() + "'";
      return firstName;
   }

   // Get the user's last name (for making a reservation)
   private static String getLastName() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter your last name: ");
      String lastName = "'" + input.next() + "'";
      return lastName;
   }

   // Get the number of adults for a reservation
   private static int getNumAdults() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter number of adults: ");
      int numAdults = input.nextInt();
      return numAdults;
   }

   // Get the number of children for a reservation
   private static int getNumChildren() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter number of children: ");
      int numChildren = input.nextInt();
      return numChildren;
   }

   // get discount for a room reservation
   private static String getDiscount() {
      Scanner input = new Scanner(System.in);

      System.out.print("Enter discount (AAA or AARP, if applicable): ");
      String dsName = input.nextLine().toUpperCase();

      return dsName;
   }
}
