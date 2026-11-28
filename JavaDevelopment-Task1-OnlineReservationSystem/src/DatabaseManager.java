import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:data/reservations.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL
            );
            """;

        // Refresh trains table so updated routes and Vande Bharat trains load cleanly
        String dropOldTrainsTable = "DROP TABLE IF EXISTS trains;";

        String createTrainsTable = """
            CREATE TABLE trains (
                train_number TEXT PRIMARY KEY,
                train_name TEXT NOT NULL,
                route_stops TEXT NOT NULL
            );
            """;

        String createReservationsTable = """
            CREATE TABLE IF NOT EXISTS reservations (
                pnr TEXT PRIMARY KEY,
                passenger_name TEXT NOT NULL,
                train_number TEXT NOT NULL,
                train_name TEXT NOT NULL,
                class_type TEXT NOT NULL,
                journey_date TEXT NOT NULL,
                source TEXT NOT NULL,
                destination TEXT NOT NULL
            );
            """;

        String seedUsers = """
            INSERT OR IGNORE INTO users (username, password, full_name) VALUES
            ('admin', 'admin123', 'Reservation Clerk'),
            ('ayush', 'pass123', 'Ayush Aher');
            """;

        String seedTrains = """
            INSERT INTO trains (train_number, train_name, route_stops) VALUES
            ('12951', 'Mumbai Rajdhani Express', 'Mumbai Central,Borivali,Surat,Vadodara Junction,Ratlam Junction,Kota Junction,New Delhi'),
            ('20901', 'Mumbai - Gandhinagar Vande Bharat', 'Mumbai Central,Borivali,Vapi,Surat,Vadodara Junction,Ahmedabad Junction,Gandhinagar Capital'),
            ('22223', 'CSMT - Sainagar Shirdi Vande Bharat', 'CSMT Mumbai,Dadar,Thane,Kalyan Junction,Nashik Road,Sainagar Shirdi'),
            ('22225', 'CSMT - Solapur Vande Bharat', 'CSMT Mumbai,Dadar,Kalyan Junction,Pune Junction,Kurduvadi,Solapur'),
            ('22436', 'New Delhi - Varanasi Vande Bharat', 'New Delhi,Kanpur Central,Prayagraj Junction,Varanasi Junction'),
            ('20607', 'Chennai - Mysuru Vande Bharat', 'MGR Chennai Central,Katpadi Junction,KSR Bengaluru,Mysuru Junction'),
            ('12123', 'Deccan Queen Express', 'CSMT Mumbai,Dadar,Kalyan Junction,Lonavala,Shivajinagar,Pune Junction'),
            ('11007', 'Deccan Express', 'CSMT Mumbai,Dadar,Thane,Kalyan Junction,Karjat,Lonavala,Talegaon,Shivajinagar,Pune Junction'),
            ('12009', 'Mumbai - Ahmedabad Shatabdi', 'Mumbai Central,Borivali,Vapi,Surat,Bharuch Junction,Vadodara Junction,Ahmedabad Junction'),
            ('12627', 'Karnataka Express', 'KSR Bengaluru,Dharmavaram,Solapur,Daund Junction,Manmad Junction,Bhusaval Junction,Itarsi Junction,Bhopal Junction,VGL Jhansi,Agra Cantt,New Delhi'),
            ('12301', 'Howrah Rajdhani Express', 'Howrah Junction,Asansol Junction,Dhanbad Junction,Gaya Junction,Prayagraj Junction,Kanpur Central,New Delhi'),
            ('12860', 'Gitanjali Express', 'Howrah Junction,Kharagpur Junction,Tatanagar Junction,Bilaspur Junction,Raipur Junction,Nagpur Junction,Bhusaval Junction,Nashik Road,Kalyan Junction,CSMT Mumbai');
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(dropOldTrainsTable);
            stmt.execute(createTrainsTable);
            stmt.execute(createReservationsTable);
            stmt.execute(seedUsers);
            stmt.execute(seedTrains);

            System.out.println("Database updated: 12 trains with intermediate stations ready.");
        } catch (SQLException e) {
            System.out.println("Database setup error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
    }
}