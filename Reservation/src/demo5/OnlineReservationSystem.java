package demo5;

import java.sql.*;   // ← MUST be present
import java.util.*;
// Model class for Reservation
class Reservation {
    String pnr;
    String name;
    String trainNumber;
    String trainName;
    String classType;
    String dateOfJourney;
    String fromPlace;
    String toPlace;

    public Reservation(String pnr, String name, String trainNumber, String trainName,
                       String classType, String dateOfJourney, String fromPlace, String toPlace) {
        this.pnr = pnr;
        this.name = name;
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.classType = classType;
        this.dateOfJourney = dateOfJourney;
        this.fromPlace = fromPlace;
        this.toPlace = toPlace;
    }
}

public class OnlineReservationSystem {

    private static final String VALID_USER = "admin";
    private static final String VALID_PASS = "1234";

    // MySQL connection details (change if needed)
    private static final String URL = "jdbc:mysql://localhost:3306/reservation_db";
    private static final String USER = "root";
    private static final String PASS = "root";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        if (!login(sc)) {
            System.out.println("Invalid login. Exiting...");
            return;
        }

        while (true) {
            System.out.println("\n===== Online Reservation System =====");
            System.out.println("1. Make Reservation");
            System.out.println("2. Cancel Reservation");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> makeReservation(sc);
                case 2 -> cancelReservation(sc);
                case 3 -> {
                    System.out.println("Thank you for using the system.");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static boolean login(Scanner sc) {
        System.out.println("===== Login =====");
        System.out.print("Enter User ID: ");
        String user = sc.nextLine();

        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        return VALID_USER.equals(user) && VALID_PASS.equals(pass);
    }

    private static void makeReservation(Scanner sc) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

            System.out.println("\n===== Reservation Form =====");

            System.out.print("Enter Passenger Name: ");
            String name = sc.nextLine();

            System.out.print("Enter Train Number (101/102/103): ");
            String trainNumber = sc.nextLine();

            String trainName = getTrainName(trainNumber);
            if (trainName.equals("Invalid")) {
                System.out.println("Invalid train number.");
                return;
            }

            System.out.println("Train Name: " + trainName);

            System.out.print("Enter Class Type: ");
            String classType = sc.nextLine();

            System.out.print("Enter Date of Journey: ");
            String date = sc.nextLine();

            System.out.print("From: ");
            String from = sc.nextLine();

            System.out.print("To: ");
            String to = sc.nextLine();

            String pnr = generatePNR();

            String query = "INSERT INTO reservations VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);

            ps.setString(1, pnr);
            ps.setString(2, name);
            ps.setString(3, trainNumber);
            ps.setString(4, trainName);
            ps.setString(5, classType);
            ps.setString(6, date);
            ps.setString(7, from);
            ps.setString(8, to);

            ps.executeUpdate();

            System.out.println("Reservation Successful!");
            System.out.println("Your PNR Number: " + pnr);

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    private static void cancelReservation(Scanner sc) {
        try (Connection con = DriverManager.getConnection(URL, USER, PASS)) {

            System.out.println("\n===== Cancellation Form =====");
            System.out.print("Enter PNR Number: ");
            String pnr = sc.nextLine();

            String selectQuery = "SELECT * FROM reservations WHERE pnr = ?";
            PreparedStatement ps = con.prepareStatement(selectQuery);
            ps.setString(1, pnr);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("PNR not found.");
                return;
            }

            System.out.println("Passenger Name: " + rs.getString("name"));
            System.out.println("Train: " + rs.getString("trainName"));
            System.out.println("From: " + rs.getString("fromPlace") + " To: " + rs.getString("toPlace"));
            System.out.println("Date: " + rs.getString("dateOfJourney"));

            System.out.print("Confirm cancellation? (yes/no): ");
            String confirm = sc.nextLine();

            if (confirm.equalsIgnoreCase("yes")) {
                String deleteQuery = "DELETE FROM reservations WHERE pnr = ?";
                PreparedStatement del = con.prepareStatement(deleteQuery);
                del.setString(1, pnr);
                del.executeUpdate();

                System.out.println("Reservation Cancelled Successfully.");
            } else {
                System.out.println("Cancellation Aborted.");
            }

        } catch (Exception e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    private static String generatePNR() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private static String getTrainName(String trainNumber) {
        return switch (trainNumber) {
            case "101" -> "Express One";
            case "102" -> "Superfast Two";
            case "103" -> "Mail Three";
            default -> "Invalid";
        };
    }
}
