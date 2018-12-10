import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.math.*;

public class Owner {

    // Program loop for owner subsystem
    public static void ownerLoop() {
        boolean exit = false;
        Scanner input = new Scanner(System.in);

        while (!exit) {
            displayOwner();

            String[] tokens = input.nextLine().toLowerCase().split("\\s");
            char option = tokens[0].charAt(0);
            char dataOpt = 0;

            if (tokens.length == 2)
                dataOpt = tokens[1].charAt(0);

            switch(option) {
                case 'o':   InnReservations.clearScreen();
                            occupancy();
                    break;
                case 'd':   System.out.println("revenueData\n");
                    break;
                case 's':   System.out.println("browseRes()\n");
                    break;
                case 'r':   System.out.println("viewRooms\n");
                    break;
                case 'b':   InnReservations.clearScreen();
                            exit = true;
                    break;
            }
        }
    }

    // Owner UI display
    private static void displayOwner() {
    
        // Display UI
        System.out.println("Welcome, Owner.\n\n"
            + "Choose an option:\n"
            + "- (O)ccupancy - View occupancy of rooms\n"
            + "- (D)ata [(c)ounts|(d)ays|(r)evenue] - View data on "
            + "counts, days, or revenue of each room\n"
            + "- (S)tays - Browse list of reservations\n"
            + "- (R)ooms - View list of rooms\n"
            + "- (B)ack - Goes back to main menu\n");
    }


    private static void occupancy() {
        System.out.println("Please enter the first date (January 1)");
        String date1 = InnReservations.getDate();

        System.out.println("\nIf you want to search a range of dates, please enter the second date. Otherwise, type \"null\"");
        String date2 = InnReservations.getDate();

        if (date1 != null && date2 == null) {
            // Only searching for 1 date
            System.out.println("\nNow showing a list of rooms and their availability for " + date1 + "\n");

            try {
                PreparedStatement availability = InnReservations.conn.prepareStatement(
                    "SELECT RoomId " + 
                    "FROM rooms " +
                    "GROUP BY RoomId;"
                );

                ResultSet availabilityResult = availability.executeQuery();

                while(availabilityResult.next()) {
                    String room = availabilityResult.getString("RoomId");
                    String status = checkAvailability(room, date1);
                    
                    System.out.println(room + " :: " + status);
                }

                System.out.println();
                String room = InnReservations.getRoomCodeOrQ();
                if (room.equals("q")) {
                    InnReservations.clearScreen();
                    return;
                }
                else {
                    reservationInfo(room, date1);
                }

            }
            catch (Exception e) {
                System.err.println("Error reading from database");
            }

        }
        else if (date1 != null && date2 != null) {
            // Searching for range of dates
            System.out.println("\nNow showing a list of rooms and their availability between dates " + date1 + " and " + date2 + "\n");

            try {
                PreparedStatement availability = InnReservations.conn.prepareStatement(
                    "SELECT RoomId " +
                    "FROM rooms " +
                    "GROUP BY RoomId;"
                );

                ResultSet availabilityResult = availability.executeQuery();

                while (availabilityResult.next()) {
                    String room = availabilityResult.getString("RoomId");
                    String status = checkAvailabilityRange(room, date1, date2);

                    System.out.println(room + " :: " + status);
                }

                System.out.println("\n");
                String room = InnReservations.getRoomCodeOrQ();
                if (room.equals("q")) {
                    InnReservations.clearScreen();
                    return;
                }
                else {
                    reservationInfoRange(room, date1, date2);
                }
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
            }

        }
        else {
            // Error in input
            System.err.println("Error: Entered date(s) formatted incorrectly");
            return;
        }
    }

    private static String checkAvailability(String room, String date) {
        try {
            PreparedStatement availability = InnReservations.conn.prepareStatement(
                "SELECT * " + 
                "FROM rooms " +
                    "JOIN reservations ON rooms.RoomId = reservations.Room " +
                "WHERE rooms.RoomId = (?) AND reservations.CheckIn <= (?) AND reservations.CheckOut > (?);"
            );
                
            availability.setString(1, room);
            availability.setString(2, date);
            availability.setString(3, date);

            ResultSet availabilityResult = availability.executeQuery();

            if (availabilityResult.next()) {
                return "Occupied";
            }
            else {
                return "Empty";
            }
        }
        catch (Exception e) {
            return null;
        }
    }

    private static String checkAvailabilityRange(String room, String date1, String date2) {
        try {
            PreparedStatement datediff = InnReservations.conn.prepareStatement("SELECT DATEDIFF((?), (?)) AS days");
            datediff.setString(1, date2);
            datediff.setString(2, date1);

            ResultSet datediffResult = datediff.executeQuery();
            int days = 0;
            if (datediffResult.next()) {
                days = datediffResult.getInt("days");
            }

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            cal.setTime(sdf.parse(date1));

            int occupied_nights = 0;
            int nights = days;
            while (nights > 0) {
                String newDate = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE);

                if (checkAvailability(room, newDate).equals("Occupied")) {
                    occupied_nights++;
                }

                cal.add(Calendar.DATE, 1);
                nights--;
            }

            if (occupied_nights == 0) {
                return "Empty";
            }
            if (occupied_nights == days) {
                return "Fully Occupied";
            }
            return "Partially Occupied";


        }
        catch (Exception e) {
            System.err.println("Error reading from database");
            System.out.println(e);
        }
        return null;
    }

    private static void reservationInfo(String room, String date1) {
        System.out.println("\n");

        try {
            PreparedStatement reservation = InnReservations.conn.prepareStatement(
                "SELECT * " +
                "FROM reservations " +
                "WHERE reservations.Room = (?) AND reservations.CheckIn <= (?) AND reservations.CheckOut > (?);"
            );

            reservation.setString(1, room);
            reservation.setString(2, date1);
            reservation.setString(3, date1);

            ResultSet reservationResults = reservation.executeQuery();

            if (reservationResults.next()) {
                System.out.println("Code  Room  CheckIn  CheckOut  Rate  LastName  FirstName  Adults  Kids");
                int Code = reservationResults.getInt("Code");
                String Room = reservationResults.getString("Room");
                String CheckIn = reservationResults.getString("CheckIn");
                String CheckOut = reservationResults.getString("CheckOut");
                int Rate = reservationResults.getInt("Rate");
                String LastName = reservationResults.getString("LastName");
                String FirstName = reservationResults.getString("FirstName");
                int Adults = reservationResults.getInt("Adults");
                int Kids = reservationResults.getInt("Kids");
                System.out.println(Code + "  " + Room + "  " + CheckIn + "  " + CheckOut + "  " + Rate + "  " + LastName + "  " + FirstName + "  " + Adults + "  " + Kids);
            }
            else {
                System.out.println("There is no reservation for room " + room + " on date " + date1);
            }

            System.out.println("\n\n");
        }
        catch (Exception e) {
            System.err.println("Error reading from database");
        }
    }

    private static void reservationInfo(String reservation) {
        System.out.println();
        
        try {
            PreparedStatement res = InnReservations.conn.prepareStatement(
                "SELECT * " +
                "FROM reservations " + 
                "WHERE reservations.Code = (?);"
            );

            res.setInt(1, Integer.parseInt(reservation));

            ResultSet reservationResults = res.executeQuery();

            if (reservationResults.next()) {
                System.out.println("Code  Room  CheckIn  CheckOut  Rate  LastName  FirstName  Adults  Kids");
                int Code = reservationResults.getInt("Code");
                String Room = reservationResults.getString("Room");
                String CheckIn = reservationResults.getString("CheckIn");
                String CheckOut = reservationResults.getString("CheckOut");
                int Rate = reservationResults.getInt("Rate");
                String LastName = reservationResults.getString("LastName");
                String FirstName = reservationResults.getString("FirstName");
                int Adults = reservationResults.getInt("Adults");
                int Kids = reservationResults.getInt("Kids");
                System.out.println(Code + "  " + Room + "  " + CheckIn + "  " + CheckOut + "  " + Rate + "  " + LastName + "  " + FirstName + "  " + Adults + "  " + Kids);
            }
            else {
                System.out.println("Error finding reservation " + reservation + " in database");
            }
        }
        catch (Exception e) {
            System.err.println("Error finding reservation " + reservation + " in database");
        }
    }

    private static void reservationInfoRange(String room, String date1, String date2) {
        System.out.println();

        try {
            PreparedStatement reservations = InnReservations.conn.prepareStatement(
                "SELECT Code " +
                "FROM reservations " +
                "WHERE reservations.Room = (?) AND reservations.CheckIn <= (?) AND reservations.CheckOut > (?);"   
            );

            reservations.setString(1, room);
            reservations.setString(2, date2);
            reservations.setString(3, date1);

            ResultSet reservationResults = reservations.executeQuery();

            boolean empty = true;
            while (reservationResults.next()) {
                empty = false;

                System.out.println(reservationResults.getString("Code"));
            }

            if (empty) {
                System.out.println("There is no reservation for room " + room + " between dates " + date1 + " and " + date2);
            }
            else {
                System.out.println();
                String reservation = InnReservations.getReservCodeOrQ();
                if (reservation.equals("q")) {
                    InnReservations.clearScreen();
                    return;
                }
                else {
                    reservationInfo(reservation);
                }
            }

            System.out.println("\n\n");
        }
        catch (Exception e) {
            System.err.println("Error reading from database");
        }
    }

}