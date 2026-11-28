import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ReservationService {

    // 1. Fetches all trains formatted as "12951 - Mumbai Rajdhani Express" for the dropdown
    public static List<String> getAllTrains() {
        List<String> trains = new ArrayList<>();
        String sql = "SELECT train_number, train_name FROM trains ORDER BY train_number";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String entry = rs.getString("train_number") + " - " + rs.getString("train_name");
                trains.add(entry);
            }
        } catch (SQLException e) {
            System.out.println("Error loading trains: " + e.getMessage());
        }
        return trains;
    }

    // 2. Fetches all ordered stations for a selected train number
    public static String[] getRouteStops(String trainNumber) {
        String sql = "SELECT route_stops FROM trains WHERE train_number = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trainNumber.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String rawStops = rs.getString("route_stops");
                    String[] stops = rawStops.split(",");
                    for (int i = 0; i < stops.length; i++) {
                        stops[i] = stops[i].trim();
                    }
                    return stops;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading route stops: " + e.getMessage());
        }
        return new String[0];
    }

    // 3. Validates inputs, generates a unique 10-digit PNR, and saves to SQLite
    public static Reservation bookTicket(String passengerName, String trainNumber,
                                         String trainName, String classType,
                                         String journeyDate, String source,
                                         String destination) throws Exception {

        if (passengerName.isBlank() || trainNumber.isBlank() || trainName.isBlank()
                || journeyDate.isBlank() || source.isBlank() || destination.isBlank()) {
            throw new IllegalArgumentException("All fields are required. Please fill in every box.");
        }

        if (!trainNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Train number must contain digits only.");
        }

        try {
            LocalDate.parse(journeyDate.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date. Use YYYY-MM-DD format.");
        }

        if (source.trim().equalsIgnoreCase(destination.trim())) {
            throw new IllegalArgumentException("Boarding and destination stations cannot be the same.");
        }

        String pnr = generateUniquePnr();
        String sql = """
            INSERT INTO reservations
            (pnr, passenger_name, train_number, train_name, class_type, journey_date, source, destination)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr);
            pstmt.setString(2, passengerName.trim());
            pstmt.setString(3, trainNumber.trim());
            pstmt.setString(4, trainName.trim());
            pstmt.setString(5, classType.trim());
            pstmt.setString(6, journeyDate.trim());
            pstmt.setString(7, source.trim());
            pstmt.setString(8, destination.trim());
            pstmt.executeUpdate();
        }

        return new Reservation(
            pnr, passengerName.trim(), trainNumber.trim(), trainName.trim(),
            classType.trim(), journeyDate.trim(), source.trim(), destination.trim()
        );
    }

    // 4. Fetches a ticket by PNR number
    public static Reservation findByPnr(String pnr) {
        String sql = "SELECT * FROM reservations WHERE pnr = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Reservation(
                        rs.getString("pnr"),
                        rs.getString("passenger_name"),
                        rs.getString("train_number"),
                        rs.getString("train_name"),
                        rs.getString("class_type"),
                        rs.getString("journey_date"),
                        rs.getString("source"),
                        rs.getString("destination")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("PNR search error: " + e.getMessage());
        }
        return null;
    }

    // 5. Deletes a confirmed cancellation from SQLite
    public static boolean cancelByPnr(String pnr) {
        String sql = "DELETE FROM reservations WHERE pnr = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Cancellation error: " + e.getMessage());
            return false;
        }
    }

    private static String generateUniquePnr() {
        while (true) {
            long number = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
            String candidate = String.valueOf(number);
            if (findByPnr(candidate) == null) {
                return candidate;
            }
        }
    }

    // Quick test to verify train list and intermediate route stops
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        List<String> allTrains = getAllTrains();
        System.out.println("Total trains loaded: " + allTrains.size());
        System.out.println("First train in list: " + allTrains.get(0));

        String[] stops = getRouteStops("12951");
        System.out.println("Stops for 12951: " + String.join(" -> ", stops));
    }
}