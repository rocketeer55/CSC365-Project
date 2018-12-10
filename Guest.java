import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.math.*;

public class Guest {
	
		// Program loop for guest sub
		public static void guestLoop() {
			boolean exit = false;
			Scanner input = new Scanner(System.in);
			
			while(!exit) {
				displayGuest();
				
				String[] tokens = input.nextLine().toLowerCase().split(" ");
				char option = tokens[0].charAt(0);
				
				switch(option) {
					case 'r': roomsAndRates();
						break;
					case 'e': reservations();
						break;
					case 'q': exit = true;
						break;
				}
			}
			InnReservations.clearScreen();
		}
		
		// Guest interface display
		public static void displayGuest() {
			
			// here it is
			System.out.println("Welcome, Guest.\n\n"
			+ "- (R)ooms and Rates - Displays rooms, their rates, and their availabilities\n"
			+ "- R(e)servations - Reserve a room\n"
			+ "- (Q)uit - return to main\n");
		}
		
		// prints our rooms table to show the guest the available rooms
		public static void roomsAndRates() {
			InnReservations.clearScreen();
			
			// display the rooms themselves
			try {
                PreparedStatement ps = InnReservations.conn.prepareStatement("SELECT * FROM rooms;");
                ResultSet rs = ps.executeQuery();
                System.out.println("Viewing rooms\n\n");
                System.out.println("RoomId  RoomName");

                while (rs.next()) {
                    String RoomId = rs.getString("RoomId");
                    String RoomName = rs.getString("RoomName");

                    System.out.println(RoomId + "  " + RoomName);
					}	
                System.out.println("\n");
				}
				catch (Exception e) {
                System.err.println("Error reading from table");
			}
			
				// ask user to select a room 
				System.out.println("\nEnter RoomId to view detailed information on the room.");
				Scanner input = new Scanner(System.in);
				try {
					input = Integer.parseInt(input);
					PreparedStatement roomStatement = InnReservations.conn.prepareStatement(
					"SELECT * " +
					"FROM rooms " +
					"WHERE rooms.RoomId = " + input + ";");
					
					ResultSet roomResult = roomStatement.executeQuery();
					if (roomResult.next()) {
						System.out.println("\nRoomId  RoomName  Beds  BedType  MaxOcc  BasePrice  Decor");
						String RoomId = roomResult.getString("RoomId");
						String RoomName = roomResult.getString("RoomName");
						int Beds = roomResult.getInt("Beds");
						String BedType = roomResult.getString("BedType");
						int MaxOcc = roomResult.getInt("MaxOcc");
						int BasePrice = roomResult.getInt("BasePrice");
						String Decor = roomResult.getString("Decor");
					}
					else {
						System.out.println("Room " + input + " does not exist");
					}
				}				
				catch (Exception e){
					System.err.println("Error: Incorrect input type")
				}    
			}
	
	
}