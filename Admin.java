import java.sql.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.*;
import java.math.*;

public class Admin {

    // Program loop for admin subsystem
    public static void adminLoop() {
        boolean exit = false;
        Scanner input = new Scanner(System.in);

        while (!exit) {
            displayAdmin();

            String[] tokens = input.nextLine().toLowerCase().split(" ");
            char option = tokens[0].charAt(0);

            switch(option) {
                case 'v':   viewTable(tokens);
                    break;
                case 'c':   clearTables();
                    break;
                case 'l':   loadDatabase();
                    break;
                case 'r':   removeDatabase();
                    break;
                case 'b':   exit = true;
                    break;
            }
        }

        InnReservations.clearScreen();
    }

    // Admin UI display
    private static void displayAdmin() {

    // Display UI
    System.out.println("Welcome, Admin.\n\n"
       + "Current Status: " + getStatus() + "\n"
       + "Reservations: " + getReservations() + "\n"
       + "Rooms: " + getRooms() + "\n\n"
       + "Choose an option:\n"
       + "- (V)iew [table name] - Displays table contents\n"
       + "- (C)lear - Deletes all table contents\n"
       + "- (L)oad - Loads all table contents\n"
       + "- (R)emove - Removes tables\n"
       + "- (B)ack - Goes back to main menu\n");

    }

    private static String getStatus() {
        try {
            DatabaseMetaData dbm = InnReservations.conn.getMetaData();
            ResultSet rooms = dbm.getTables(null, null, "rooms", null);
            ResultSet reservations = dbm.getTables(null, null, "reservations", null);

            if (rooms.next()) {
                // Rooms exist
                String roomsCount = "SELECT COUNT(*) FROM rooms;";
                PreparedStatement roomsCountStatement = InnReservations.conn.prepareStatement(roomsCount);

                ResultSet roomsCountResult = roomsCountStatement.executeQuery();
                roomsCountResult.next();
                if (roomsCountResult.getInt(1) == 0) {
                    return "empty";
                }
            }
            else {
                return "no database";
            }

            if (reservations.next()) {
                // Reservations exist
                String reservationsCount = "SELECT COUNT(*) FROM rooms;";
                PreparedStatement reservationCountStatement = InnReservations.conn.prepareStatement(reservationsCount);

                ResultSet reservationsCountResult = reservationCountStatement.executeQuery();
                reservationsCountResult.next();
                if (reservationsCountResult.getInt(1) == 0) {
                    return "empty";
                }
            }
            else {
                return "no database";
            }

            return "full";
        }
        catch (Exception e) {
            return "no database";
        }
    }

    private static int getReservations() {
        try {
            DatabaseMetaData dbm = InnReservations.conn.getMetaData();
            ResultSet reservations = dbm.getTables(null, null, "reservations", null);

            if (reservations.next()) {
                // Reservations exists
                String reservationsCount = "SELECT COUNT(*) FROM reservations;";
                PreparedStatement reservationsCounStatement = InnReservations.conn.prepareStatement(reservationsCount);

                ResultSet reservationsCountResult = reservationsCounStatement.executeQuery();
                reservationsCountResult.next();
                return reservationsCountResult.getInt(1);
            }
            return 0;
        }
        catch (Exception e) {
            return 0;
        }
    }

    private static int getRooms() {
        try {
            DatabaseMetaData dbm = InnReservations.conn.getMetaData();
            ResultSet rooms = dbm.getTables(null, null, "rooms", null);

            if (rooms.next()) {
                // rooms exists
                String roomsCount = "SELECT COUNT(*) FROM rooms;";
                PreparedStatement roomsCounStatement = InnReservations.conn.prepareStatement(roomsCount);

                ResultSet roomsCountResult = roomsCounStatement.executeQuery();
                roomsCountResult.next();
                return roomsCountResult.getInt(1);
            }
            return 0;
        }
        catch (Exception e) {
            return 0;
        }
    }

    private static void viewTable(String[] tokens) {
        InnReservations.clearScreen();

        if (tokens.length == 1) {
            System.out.println("No table selected.\n");
        }
        else if (tokens[1].equals("rooms")) {
            try {
                PreparedStatement ps = InnReservations.conn.prepareStatement("SELECT * FROM rooms;");
                ResultSet rs = ps.executeQuery();
                System.out.println("Viewing rooms\n\n");
                System.out.println("RoomId  RoomName  Beds  BedType  MaxOcc  BasePrice  Decor");

                while (rs.next()) {
                    String RoomId = rs.getString("RoomId");
                    String RoomName = rs.getString("RoomName");
                    int Beds = rs.getInt("Beds");
                    String BedType = rs.getString("BedType");
                    int MaxOcc = rs.getInt("MaxOcc");
                    int BasePrice = rs.getInt("BasePrice");
                    String Decor = rs.getString("Decor");

                    System.out.println(RoomId + "  " + RoomName + "  " + Beds + "  " + BedType + "  " + MaxOcc + "  " + BasePrice + "  " + Decor);
                }
                System.out.println("\n");
            }
            catch (Exception e) {
                System.err.println("Error reading from table");
            }
        }
        else if (tokens[1].equals("reservations")) {
            try {
                PreparedStatement ps = InnReservations.conn.prepareStatement("SELECT * FROM reservations;");
                ResultSet rs = ps.executeQuery();
                System.out.println("Viewing reservations\n\n");
                System.out.println("Code  Room  CheckIn  CheckOut  Rate  LastName  FirstName  Adults  Kids");

                while (rs.next()) {
                    int Code = rs.getInt("Code");
                    String Room = rs.getString("Room");
                    String CheckIn = rs.getString("CheckIn");
                    String CheckOut = rs.getString("CheckOut");
                    int Rate = rs.getInt("Rate");
                    String LastName = rs.getString("LastName");
                    String FirstName = rs.getString("FirstName");
                    int Adults = rs.getInt("Adults");
                    int Kids = rs.getInt("Kids");

                    System.out.println(Code + "  " + Room + "  " + CheckIn + "  " + CheckOut + "  " + Rate + "  " + LastName + "  " + FirstName + "  " + Adults + "  " + Kids);
                }
                System.out.println("\n");
            }
            catch (Exception e) {
                System.err.println("Error reading from table");
            }
        }
        else {
            System.out.println("Table \'" + tokens[1] + "\' is not a valid table.");
        }

    }

    private static void clearTables() {
        InnReservations.clearScreen();
        System.out.println("Attempting to clear tables...\n");
        try {
            PreparedStatement clearRooms = InnReservations.conn.prepareStatement("DELETE FROM rooms;");
            PreparedStatement clearReservations = InnReservations.conn.prepareStatement("DELETE FROM reservations;");

            clearRooms.executeUpdate();
            clearReservations.executeUpdate();
            System.out.println("Tables cleared sucessfully.\n\n");
        }
        catch (Exception e) {
            System.err.println("Error clearing tables");
        }
    }

    private static void loadDatabase() {
        InnReservations.clearScreen();

        String status = getStatus();
        if (status.equals("full")) {
            System.out.println("Database full. Not reloading database.\n\n");
        }
        else if (status.equals("empty")) {
            System.out.println("Database empty. Reloading database...\n");
            try {
                PreparedStatement reloadRooms = InnReservations.conn.prepareStatement("INSERT INTO rooms SELECT * FROM INN.rooms;");
                PreparedStatement reloadReservations = InnReservations.conn.prepareStatement("INSERT INTO reservations SELECT * FROM INN.reservations;");

                reloadRooms.executeUpdate();
                reloadReservations.executeUpdate();

                System.out.println("Database reloaded sucessfully.\n\n");
            }
            catch (Exception e) {
                System.err.println("Error reloading database");
            }
        }
        else {
            System.out.println("Database does not exist. Loading new database...\n");
            try {
                PreparedStatement createRooms = InnReservations.conn.prepareStatement("CREATE TABLE rooms AS SELECT * FROM INN.rooms;");
                PreparedStatement createReservations = InnReservations.conn.prepareStatement("CREATE TABLE reservations AS SELECT * FROM INN.reservations;");

                createRooms.execute();
                createReservations.execute();

                System.out.println("Database loaded sucessfully.\n\n");
            }
            catch (Exception e) {
                System.err.println("Error loading database");
            }
        }
    }

    private static void removeDatabase() {
        InnReservations.clearScreen();

        System.out.println("Attempting to remove database...\n");
        try {
            PreparedStatement dropRooms = InnReservations.conn.prepareStatement("DROP TABLE rooms;");
            PreparedStatement dropReservations = InnReservations.conn.prepareStatement("DROP TABLE reservations;");

            dropRooms.execute();
            dropReservations.execute();

            System.out.println("Database removed successfully.\n\n");
        }
        catch (Exception e) {
            System.err.println("Error removing database");
        }
    }
}