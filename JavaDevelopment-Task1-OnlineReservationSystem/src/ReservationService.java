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

    // 1. Fetches all trains formatted as "12951 - Mumbai Rajdhani Express"
    public static List<String> getAllTrains() {
        List<String> trains = new ArrayList<>();
        String sql = "SELECT train_number, train_name FROM trains ORDER BY train_number";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                trains.add(rs.getString("train_number") + " - " + rs.getString("train_name"));
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
                    String[] stops = rs.getString("route_stops").split(",");
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

    // 3. Fetches valid coach classes for the selected train
    public static String[] getTrainClasses(String trainNumber) {
        String sql = "SELECT available_classes FROM trains WHERE train_number = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, trainNumber.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String[] classes = rs.getString("available_classes").split(",");
                    for (int i = 0; i < classes.length; i++) {
                        classes[i] = classes[i].trim();
                    }
                    return classes;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error loading train classes: " + e.getMessage());
        }
        return new String[]{"SL - Sleeper", "3A - AC 3 Tier"};
    }

    // 4. Calculates realistic ticket fare in INR based on stops, coach class, and train tier
    public static int calculateFare(String trainNumber, int boardingIndex, int destinationIndex, String classType) {
        int hops = Math.max(1, destinationIndex - boardingIndex);
        String code = (classType != null && classType.contains(" - "))
            ? classType.split(" - ", 2)[0].trim()
            : "SL";

        int ratePerStop;
        int minFare;
        switch (code) {
            case "2S" -> { ratePerStop = 65;  minFare = 95; }
            case "SL" -> { ratePerStop = 140; minFare = 220; }
            case "CC" -> { ratePerStop = 260; minFare = 420; }
            case "3A" -> { ratePerStop = 340; minFare = 550; }
            case "EC" -> { ratePerStop = 490; minFare = 780; }
            case "2A" -> { ratePerStop = 510; minFare = 820; }
            case "1A" -> { ratePerStop = 850; minFare = 1350; }
            default   -> { ratePerStop = 200; minFare = 300; }
        }

        boolean isPremiumTrain = trainNumber.startsWith("20")
            || trainNumber.startsWith("22")
            || trainNumber.startsWith("1295")
            || trainNumber.startsWith("1230")
            || trainNumber.startsWith("1200");

        double rawFare = Math.max(minFare, hops * ratePerStop) * (isPremiumTrain ? 1.25 : 1.0);
        return (int) (Math.round(rawFare / 5.0) * 5);
    }

    // 5. Validates inputs, generates a unique 10-digit PNR, and saves ticket + fare to SQLite
    public static Reservation bookTicket(String passengerName, String trainNumber,
                                         String trainName, String classType,
                                         String journeyDate, String source,
                                         String destination, int fare) throws Exception {

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
            (pnr, passenger_name, train_number, train_name, class_type, journey_date, source, destination, fare)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            pstmt.setInt(9, fare);
            pstmt.executeUpdate();
        }

        return new Reservation(
            pnr, passengerName.trim(), trainNumber.trim(), trainName.trim(),
            classType.trim(), journeyDate.trim(), source.trim(), destination.trim(), fare
        );
    }

    // 6. Fetches a single ticket by PNR number
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
                        rs.getString("destination"),
                        rs.getInt("fare")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("PNR search error: " + e.getMessage());
        }
        return null;
    }

    // 7. Fetches all active reservations (most recent first)
    public static List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY rowid DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new Reservation(
                    rs.getString("pnr"),
                    rs.getString("passenger_name"),
                    rs.getString("train_number"),
                    rs.getString("train_name"),
                    rs.getString("class_type"),
                    rs.getString("journey_date"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    rs.getInt("fare")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error loading all reservations: " + e.getMessage());
        }
        return list;
    }

    // 8. Deletes a confirmed cancellation from SQLite
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
}