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
                case 'd':   InnReservations.clearScreen();
                            revenue(tokens);
                    break;
                case 's':   InnReservations.clearScreen();
                            reservations();
                    break;
                case 'r':   InnReservations.clearScreen();
                            rooms();
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

    private static void revenue(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("No option provided");
            return;
        }
        if (tokens[1].toLowerCase().equals("c")) {
            try {
                PreparedStatement rooms = InnReservations.conn.prepareStatement(
                    "SELECT RoomId " +
                    "FROM rooms"
                );

                ResultSet roomsResult = rooms.executeQuery();

                int[] sums = new int[13];

                System.out.println("Room  Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec  Total");
                while (roomsResult.next()) {
                    String room = roomsResult.getString("RoomId");
                    System.out.print(room + "  ");

                    int rowSum = 0;
                    for (int i = 0; i < 12; i++) {
                        int curr = getCount(room, i + 1);
                        sums[i]+=curr;
                        rowSum+=curr;
                        System.out.print(curr + "  ");
                    }

                    System.out.print(rowSum + "\n");
                    sums[12]+= rowSum;
                }

                System.out.print("Total  ");
                for (int i = 0; i < sums.length; i++) {
                    System.out.print(sums[i] + "  ");
                }

                System.out.println("\n");
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
            }
        }
        else if (tokens[1].toLowerCase().equals("d")) {
            try {
                PreparedStatement rooms = InnReservations.conn.prepareStatement(
                    "SELECT RoomId " +
                    "FROM rooms"
                );

                ResultSet roomsResult = rooms.executeQuery();

                int[] sums = new int[13];

                System.out.println("Room  Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec  Total");
                while (roomsResult.next()) {
                    String room = roomsResult.getString("RoomId");
                    System.out.print(room + "  ");

                    int rowSum = 0;
                    for (int i = 0; i < 12; i++) {
                        int curr = getDaysOccupied(room, i + 1);
                        sums[i]+=curr;
                        rowSum+=curr;
                        System.out.print(curr + "  ");
                    }

                    System.out.print(rowSum + "\n");
                    sums[12]+= rowSum;
                }

                System.out.print("Total  ");
                for (int i = 0; i < sums.length; i++) {
                    System.out.print(sums[i] + "  ");
                }

                System.out.println("\n");
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
            }
        }
        else if (tokens[1].toLowerCase().equals("r")) {
            try {
                PreparedStatement rooms = InnReservations.conn.prepareStatement(
                    "SELECT RoomId " +
                    "FROM rooms"
                );

                ResultSet roomsResult = rooms.executeQuery();

                int[] sums = new int[13];

                System.out.println("Room  Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec  Total");
                while (roomsResult.next()) {
                    String room = roomsResult.getString("RoomId");
                    System.out.print(room + "  ");

                    int rowSum = 0;
                    for (int i = 0; i < 12; i++) {
                        int curr = getRevenue(room, i + 1);
                        sums[i]+=curr;
                        rowSum+=curr;
                        System.out.print(curr + "  ");
                    }

                    System.out.print(rowSum + "\n");
                    sums[12]+= rowSum;
                }

                System.out.print("Total  ");
                for (int i = 0; i < sums.length; i++) {
                    System.out.print(sums[i] + "  ");
                }

                System.out.println("\n");
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
            }
        }
        else {
            System.out.println("Option \'" + tokens[2] + "\' is not a valid option.");
        }
    }

    private static void reservations() {
        System.out.println("How would you like to search? (dates / room / both)");
        Scanner input = new Scanner(System.in);
        String response = input.next().toLowerCase();
        if (response.equals("dates")) {
            System.out.println("Please enter the first date (January 1)");
            String date1 = InnReservations.getDate();

            System.out.println("\nPlease enter the second date (January 2)");
            String date2 = InnReservations.getDate();

            try {
                PreparedStatement reservations = InnReservations.conn.prepareStatement(
                    "SELECT Code, CheckIn, CheckOut " +
                    "FROM reservations " + 
                    "WHERE reservations.CheckIn >= (?) AND reservations.CheckIn <= (?);"
                );

                reservations.setString(1, date1);
                reservations.setString(2, date2);

                ResultSet reservationsResult = reservations.executeQuery();

                if (reservationsResult.next()) {
                    System.out.println("\n\nCode  CheckIn  CheckOut");
                    while (reservationsResult.next()) {
                        System.out.println(reservationsResult.getString("Code") + "  " + reservationsResult.getString("CheckIn") + "  " + reservationsResult.getString("CheckOut"));
                    }
                }
                else {
                    System.out.println("No reservations between date " + date1 + " and " + date2);
                    return;
                }
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
                return;
            }
        }
        else if (response.equals("room")) {
            System.out.println("Please enter the room code (AOB)");
            String room = input.next().toLowerCase();

            try {
                PreparedStatement reservations = InnReservations.conn.prepareStatement(
                    "SELECT Code, CheckIn, CheckOut " +
                    "FROM reservations " +
                    "WHERE reservations.Room = (?);"
                );

                reservations.setString(1, room);

                ResultSet reservationsResult = reservations.executeQuery();

                if (reservationsResult.next()) {
                    System.out.println("\n\nCode  CheckIn  CheckOut");
                    while (reservationsResult.next()) {
                        System.out.println(reservationsResult.getString("Code") + "  " + reservationsResult.getString("CheckIn") + "  " + reservationsResult.getString("CheckOut"));
                    }
                }
                else {
                    System.out.println("No reservations for room " + room);
                    return;
                }
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
                return;
            }
        }
        else if (response.equals("both")) {
            System.out.println("Please enter the first date (January 1)");
            String date1 = InnReservations.getDate();

            System.out.println("\nPlease enter the second date (January 2)");
            String date2 = InnReservations.getDate();

            System.out.println("Please enter the room code (AOB)");
            String room = input.next().toLowerCase();

            try {
                PreparedStatement reservations = InnReservations.conn.prepareStatement(
                    "SELECT Code, CheckIn, CheckOut " +
                    "FROM reservations " +
                    "WHERE reservations.Room = (?) AND reservations.CheckIn >= (?) AND reservations.CheckIn <= (?);"
                );

                reservations.setString(1, room);
                reservations.setString(2, date1);
                reservations.setString(3, date2);

                ResultSet reservationsResult = reservations.executeQuery();

                if (reservationsResult.next()) {
                    System.out.println("\n\nCode  CheckIn  CheckOut");
                    while (reservationsResult.next()) {
                        System.out.println(reservationsResult.getString("Code") + "  " + reservationsResult.getString("CheckIn") + "  " + reservationsResult.getString("CheckOut"));
                    }
                }
                else {
                    System.out.println("No reservations for room " + room + " between date " + date1 + " and " + date2);
                    return;
                }
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
                return;
            }
        }
        else {
            System.out.println("Incorrect option \'" + response + "\'");
            return;
        }

        System.out.println("\n");
        response = InnReservations.getReservCodeOrQ();
        if (response.equals("q")) {
            InnReservations.clearScreen();
            return;
        }
        reservationInfo(response);
        System.out.println("\n");
    }

    private static void rooms() {
        try {
            PreparedStatement rooms = InnReservations.conn.prepareStatement(
                "SELECT RoomId, RoomName " +
                "FROM rooms;"
            );

            ResultSet roomsResult = rooms.executeQuery();

            if (roomsResult.next()) {
                System.out.println("RoomId  RoomName");
                System.out.println(roomsResult.getString("RoomId") + "  " + roomsResult.getString("RoomName"));
                while (roomsResult.next()) {
                    System.out.println(roomsResult.getString("RoomId") + "  " + roomsResult.getString("RoomName"));
                }
            }
            else {
                System.out.println("No rooms in the database");
                return;
            }
        }
        catch (Exception e) {
            System.err.println("Error reading from database");
            return;
        }

        System.out.println("\n");
        String room = InnReservations.getRoomCodeOrQ();
        if (room.equals("q")) {
            InnReservations.clearScreen();
            return;
        }
        System.out.println("\nEnter \'f\' to view full information on room " + room + " or \'r\' to view reservations for room " + room);
        Scanner input = new Scanner(System.in);
        String result = input.next().toLowerCase();
        if (result.equals("f")) {
            try {
                PreparedStatement roomStatement = InnReservations.conn.prepareStatement(
                    "SELECT * " +
                    "FROM rooms " +
                    "WHERE rooms.RoomId = (?);"
                );

                PreparedStatement occupancyStatement = InnReservations.conn.prepareStatement(
                    "SELECT SUM(DATEDIFF(reservations.CheckOut, reservations.CheckIn)) AS nights, " +
                    "SUM(reservations.Rate * DATEDIFF(reservations.CheckOut, reservations.CheckIn)) AS revenue " +
                    "FROM reservations " +
                    "WHERE reservations.Room = (?);"
                );

                PreparedStatement totalRevenue = InnReservations.conn.prepareStatement(
                    "SELECT SUM(reservations.Rate * DATEDIFF(reservations.CheckOut, reservations.CheckIn)) AS revenue " +
                    "FROM reservations;"
                );
                
                roomStatement.setString(1, room);
                occupancyStatement.setString(1, room);

                ResultSet roomResult = roomStatement.executeQuery();
                ResultSet occupancyResult = occupancyStatement.executeQuery();
                ResultSet totalRevenueResult = totalRevenue.executeQuery();

                if (roomResult.next() && occupancyResult.next() && totalRevenueResult.next()) {
                    System.out.println("\nRoomId  RoomName  Beds  BedType  MaxOcc  BasePrice  Decor  NightsOccupied  PercentOccupied  RoomRevenue  PercentRevenue");
                    String RoomId = roomResult.getString("RoomId");
                    String RoomName = roomResult.getString("RoomName");
                    int Beds = roomResult.getInt("Beds");
                    String BedType = roomResult.getString("BedType");
                    int MaxOcc = roomResult.getInt("MaxOcc");
                    int BasePrice = roomResult.getInt("BasePrice");
                    String Decor = roomResult.getString("Decor");
                    int NightsOccupied = occupancyResult.getInt("nights");
                    double PercentOccupied = NightsOccupied / 365.f;
                    int RoomRevenue = occupancyResult.getInt("revenue");
                    double PercentRevenue = (double)RoomRevenue / totalRevenueResult.getInt("revenue");
                    System.out.println(RoomId + "  " + RoomName + "  " + Beds + "  " + BedType + "  " + MaxOcc + "  " + BasePrice + "  " + 
                        Decor + "  " + NightsOccupied + "  " + PercentOccupied + "  " + RoomRevenue + "  " + PercentRevenue);
                }
                else {
                    System.out.println("Room " + room + " does not exist");
                    return;
                }

                System.out.println("\n\n");
            }
            catch (Exception e) {
                System.err.println("Error reading from database");
                System.err.println(e);
                return;
            }
        }
        else if (result.equals("r")) {
            try {
                PreparedStatement reservationStatement = InnReservations.conn.prepareStatement(
                    "SELECT Code, CheckIn, CheckOut " +
                    "FROM reservations " +
                    "WHERE reservations.Room = (?) " +
                    "ORDER BY reservations.CheckIn;"
                );

                reservationStatement.setString(1, room);

                ResultSet reservationsResult = reservationStatement.executeQuery();

                if (reservationsResult.next()) {
                    System.out.println("Code  CheckIn  CheckOut");
                    System.out.println(reservationsResult.getString("Code") + "  " + reservationsResult.getString("CheckIn") + "  " + reservationsResult.getString("CheckOut"));
                    while (reservationsResult.next()) {
                        System.out.println(reservationsResult.getString("Code") + "  " + reservationsResult.getString("CheckIn") + "  " + reservationsResult.getString("CheckOut"));
                    }
                }
                else {
                    System.out.println("No reservations for room " + room);
                    return;
                }

                System.out.println("\n");
                result = InnReservations.getReservCodeOrQ();
                if (result.equals("q")) {
                    InnReservations.clearScreen();
                    return;
                }

                reservationInfo(result);

                System.out.println("\n\n");

            }
            catch (Exception e) {
                System.err.println("Error reading from database");
                return;
            }
        }
        else {
            System.out.println("Incorrect option chosen");
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
                    "JOIN rooms ON reservations.Room = rooms.RoomId " +
                "WHERE reservations.Room = (?) AND reservations.CheckIn <= (?) AND reservations.CheckOut > (?);"
            );

            reservation.setString(1, room);
            reservation.setString(2, date1);
            reservation.setString(3, date1);

            ResultSet reservationResults = reservation.executeQuery();

            if (reservationResults.next()) {
                System.out.println("Code  Room  RoomName  CheckIn  CheckOut  Rate  LastName  FirstName  Adults  Kids");
                int Code = reservationResults.getInt("reservations.Code");
                String Room = reservationResults.getString("reservations.Room");
                String RoomName = reservationResults.getString("rooms.RoomName");
                String CheckIn = reservationResults.getString("reservations.CheckIn");
                String CheckOut = reservationResults.getString("reservations.CheckOut");
                int Rate = reservationResults.getInt("reservations.Rate");
                String LastName = reservationResults.getString("reservations.LastName");
                String FirstName = reservationResults.getString("reservations.FirstName");
                int Adults = reservationResults.getInt("reservations.Adults");
                int Kids = reservationResults.getInt("reservations.Kids");
                System.out.println(Code + "  " + Room + "  " + RoomName + "  " + CheckIn + "  " + CheckOut + "  " + Rate + "  " + LastName + "  " + FirstName + "  " + Adults + "  " + Kids);
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
                    "JOIN rooms ON reservations.Room = rooms.RoomId " +
                "WHERE reservations.Code = (?);"
            );

            res.setInt(1, Integer.parseInt(reservation));

            ResultSet reservationResults = res.executeQuery();

            if (reservationResults.next()) {
                System.out.println("Code  Room  RoomName  CheckIn  CheckOut  Rate  LastName  FirstName  Adults  Kids");
                int Code = reservationResults.getInt("reservations.Code");
                String Room = reservationResults.getString("reservations.Room");
                String RoomName = reservationResults.getString("rooms.RoomName");
                String CheckIn = reservationResults.getString("reservations.CheckIn");
                String CheckOut = reservationResults.getString("reservations.CheckOut");
                int Rate = reservationResults.getInt("reservations.Rate");
                String LastName = reservationResults.getString("reservations.LastName");
                String FirstName = reservationResults.getString("reservations.FirstName");
                int Adults = reservationResults.getInt("reservations.Adults");
                int Kids = reservationResults.getInt("reservations.Kids");
                System.out.println(Code + "  " + Room + "  " + RoomName + "  " + CheckIn + "  " + CheckOut + "  " + Rate + "  " + LastName + "  " + FirstName + "  " + Adults + "  " + Kids);
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

    private static int getCount(String room, int month) {
        try {
            PreparedStatement count = InnReservations.conn.prepareStatement(
                "SELECT COUNT(*) AS count " +
                "FROM reservations " +
                "WHERE reservations.Room = (?) AND MONTH(reservations.CheckOut) = (?);"
            );

            count.setString(1, room);
            count.setInt(2, month);

            ResultSet countResults = count.executeQuery();

            if (countResults.next()) {
                return countResults.getInt("count");
            }
            else {
                return 0;
            }
        }
        catch (Exception e) {
            System.err.println("Error reading from database");
        }

        return 0;
    }

    private static int getDaysOccupied(String room, int month) {
        try {
            PreparedStatement days = InnReservations.conn.prepareStatement(
                "SELECT SUM(DATEDIFF(reservations.CheckOut, reservations.CheckIn)) AS days " +
                "FROM reservations " +
                "WHERE reservations.Room = (?) AND MONTH(reservations.CheckOut) = (?);"
            );

            days.setString(1, room);
            days.setInt(2, month);

            ResultSet daysResult = days.executeQuery();

            if (daysResult.next()) {
                return daysResult.getInt("days");
            }
            else {
                return 0;
            }
        }
        catch (Exception e) {
            System.err.println("Error reading from database");
        }

        return 0;
    }

    private static int getRevenue(String room, int month) {
        try {
            PreparedStatement revenue = InnReservations.conn.prepareStatement(
                "SELECT SUM(reservations.Rate * DATEDIFF(reservations.CheckOut, reservations.CheckIn)) AS revenue " +
                "FROM reservations " +
                "WHERE reservations.Room = (?) AND MONTH(reservations.CheckOut) = (?);"
            );

            revenue.setString(1, room);
            revenue.setInt(2, month);

            ResultSet revenueResult = revenue.executeQuery();

            if (revenueResult.next()) {
                return revenueResult.getInt("revenue");
            }
            else {
                return 0;
            }
        }
        catch (Exception e) {
            System.err.println("Error reading from database");
        }

        return 0;
    }

}