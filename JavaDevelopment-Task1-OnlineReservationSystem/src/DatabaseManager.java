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

        String dropOldTrainsTable = "DROP TABLE IF EXISTS trains;";

        String createTrainsTable = """
            CREATE TABLE trains (
                train_number TEXT PRIMARY KEY,
                train_name TEXT NOT NULL,
                route_stops TEXT NOT NULL,
                available_classes TEXT NOT NULL
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
                destination TEXT NOT NULL,
                fare INTEGER NOT NULL DEFAULT 0
            );
            """;

        String seedUsers = """
            INSERT OR IGNORE INTO users (username, password, full_name) VALUES
            ('admin', 'admin123', 'Reservation Clerk'),
            ('user1', 'pass123', 'User1'),
            ('user2', 'pass456', 'User2');
            """;

        String seedTrains = """
            INSERT INTO trains (train_number, train_name, route_stops, available_classes) VALUES
            ('10103', 'Mandovi Express', 'CSMT Mumbai,Dadar,Thane,Panvel,Mangaon,Khed,Chiplun,Ratnagiri,Kankavli,Kudal,Sawantwadi Road,Thivim,Karmali,Madgaon Junction', '2S - Second Sitting,SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('11007', 'Deccan Express', 'CSMT Mumbai,Dadar,Thane,Kalyan Junction,Karjat,Lonavala,Talegaon,Shivajinagar,Pune Junction', '2S - Second Sitting,CC - Chair Car,3A - AC 3 Tier'),
            ('11301', 'Udyan Express', 'CSMT Mumbai,Dadar,Kalyan Junction,Lonavala,Pune Junction,Daund Junction,Solapur,Kalaburagi,Wadi,Raichur,Guntakal Junction,Dharmavaram,KSR Bengaluru', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('12009', 'Mumbai - Ahmedabad Shatabdi', 'Mumbai Central,Borivali,Vapi,Surat,Bharuch Junction,Vadodara Junction,Ahmedabad Junction', 'CC - Chair Car,EC - Executive Chair Car'),
            ('12051', 'CSMT - Madgaon Jan Shatabdi', 'CSMT Mumbai,Dadar,Thane,Panvel,Chiplun,Ratnagiri,Kankavli,Kudal,Thivim,Madgaon Junction', '2S - Second Sitting,CC - Chair Car'),
            ('12123', 'Deccan Queen Express', 'CSMT Mumbai,Dadar,Kalyan Junction,Lonavala,Shivajinagar,Pune Junction', '2S - Second Sitting,CC - Chair Car,EC - Executive Chair Car'),
            ('12127', 'Mumbai - Pune Intercity Express', 'CSMT Mumbai,Dadar,Thane,Kalyan Junction,Lonavala,Shivajinagar,Pune Junction', '2S - Second Sitting,CC - Chair Car'),
            ('12133', 'CSMT - Mangaluru Express', 'CSMT Mumbai,Thane,Panvel,Ratnagiri,Kankavli,Madgaon Junction,Karwar,Kumta,Bhatkal,Udupi,Surathkal,Mangaluru Junction', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier'),
            ('12137', 'Punjab Mail', 'CSMT Mumbai,Dadar,Kalyan Junction,Nashik Road,Manmad Junction,Bhusaval Junction,Itarsi Junction,Bhopal Junction,VGL Jhansi,Gwalior,Agra Cantt,New Delhi,Rohtak Junction,Bathinda Junction,Firozpur Cantt', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('12267', 'Mumbai Central - Hapa Duronto', 'Mumbai Central,Ahmedabad Junction,Surendranagar,Rajkot Junction,Jamnagar,Hapa', '3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('12301', 'Howrah Rajdhani Express', 'Howrah Junction,Asansol Junction,Dhanbad Junction,Gaya Junction,Prayagraj Junction,Kanpur Central,New Delhi', '1A - First AC,2A - AC 2 Tier,3A - AC 3 Tier'),
            ('12619', 'Matsyagandha Express', 'Lokmanya Tilak Terminus,Thane,Panvel,Chiplun,Ratnagiri,Kankavli,Kudal,Madgaon Junction,Karwar,Kumta,Bhatkal,Udupi,Surathkal,Mangaluru Central', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier'),
            ('12627', 'Karnataka Express', 'KSR Bengaluru,Dharmavaram,Solapur,Daund Junction,Manmad Junction,Bhusaval Junction,Itarsi Junction,Bhopal Junction,VGL Jhansi,Agra Cantt,New Delhi', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('12701', 'Hussainsagar Express', 'CSMT Mumbai,Dadar,Kalyan Junction,Lonavala,Pune Junction,Daund Junction,Solapur,Kalaburagi,Wadi,Tandur,Vikarabad Junction,Begumpet,Hyderabad Deccan', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('12860', 'Gitanjali Express', 'Howrah Junction,Kharagpur Junction,Tatanagar Junction,Bilaspur Junction,Raipur Junction,Nagpur Junction,Bhusaval Junction,Nashik Road,Kalyan Junction,CSMT Mumbai', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier'),
            ('12903', 'Golden Temple Mail', 'Mumbai Central,Borivali,Surat,Vadodara Junction,Ratlam Junction,Kota Junction,Mathura Junction,Hazrat Nizamuddin,Ghaziabad,Meerut City,Ambala Cantt,Ludhiana Junction,Jalandhar City,Amritsar Junction', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('12951', 'Mumbai Rajdhani Express', 'Mumbai Central,Borivali,Surat,Vadodara Junction,Ratlam Junction,Kota Junction,New Delhi', '1A - First AC,2A - AC 2 Tier,3A - AC 3 Tier'),
            ('12953', 'August Kranti Rajdhani', 'Mumbai Central,Borivali,Vapi,Valsad,Surat,Bharuch Junction,Vadodara Junction,Ratlam Junction,Kota Junction,Sawai Madhopur,Mathura Junction,Hazrat Nizamuddin', '1A - First AC,2A - AC 2 Tier,3A - AC 3 Tier'),
            ('16345', 'Netravati Express', 'Lokmanya Tilak Terminus,Thane,Panvel,Chiplun,Ratnagiri,Kudal,Thivim,Madgaon Junction,Karwar,Udupi,Mangaluru Junction,Kannur,Kozhikode,Shoranur Junction,Ernakulam Junction,Alappuzha,Kollam Junction,Thiruvananthapuram Central', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier'),
            ('20111', 'Konkan Kanya Express', 'CSMT Mumbai,Dadar,Thane,Panvel,Khed,Chiplun,Sangameshwar Road,Ratnagiri,Vaibhavwadi Road,Kankavli,Sindhudurg,Kudal,Sawantwadi Road,Thivim,Karmali,Madgaon Junction', 'SL - Sleeper,3A - AC 3 Tier,2A - AC 2 Tier,1A - First AC'),
            ('20607', 'Chennai - Mysuru Vande Bharat', 'MGR Chennai Central,Katpadi Junction,KSR Bengaluru,Mysuru Junction', 'CC - Chair Car,EC - Executive Chair Car'),
            ('20833', 'Visakhapatnam - Secunderabad Vande Bharat', 'Visakhapatnam,Samalkot Junction,Rajahmundry,Vijayawada Junction,Khammam,Warangal,Secunderabad Junction', 'CC - Chair Car,EC - Executive Chair Car'),
            ('20901', 'Mumbai - Gandhinagar Vande Bharat', 'Mumbai Central,Borivali,Vapi,Surat,Vadodara Junction,Ahmedabad Junction,Gandhinagar Capital', 'CC - Chair Car,EC - Executive Chair Car'),
            ('22223', 'CSMT - Sainagar Shirdi Vande Bharat', 'CSMT Mumbai,Dadar,Thane,Kalyan Junction,Nashik Road,Sainagar Shirdi', 'CC - Chair Car,EC - Executive Chair Car'),
            ('22225', 'CSMT - Solapur Vande Bharat', 'CSMT Mumbai,Dadar,Kalyan Junction,Pune Junction,Kurduvadi,Solapur', 'CC - Chair Car,EC - Executive Chair Car'),
            ('22227', 'CSMT - Jalna Vande Bharat', 'CSMT Mumbai,Dadar,Thane,Kalyan Junction,Nashik Road,Manmad Junction,Aurangabad,Jalna', 'CC - Chair Car,EC - Executive Chair Car'),
            ('22229', 'CSMT - Madgaon Vande Bharat', 'CSMT Mumbai,Dadar,Thane,Panvel,Khed,Ratnagiri,Kankavli,Thivim,Madgaon Junction', 'CC - Chair Car,EC - Executive Chair Car'),
            ('22436', 'New Delhi - Varanasi Vande Bharat', 'New Delhi,Kanpur Central,Prayagraj Junction,Varanasi Junction', 'CC - Chair Car,EC - Executive Chair Car');
            """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(dropOldTrainsTable);
            stmt.execute(createTrainsTable);
            stmt.execute(createReservationsTable);

            try {
                stmt.execute("ALTER TABLE reservations ADD COLUMN fare INTEGER NOT NULL DEFAULT 0;");
            } catch (SQLException ignored) {}

            stmt.execute("DELETE FROM users WHERE username = 'ayush';");
            stmt.execute(seedUsers);
            stmt.execute(seedTrains);

            System.out.println("Database updated: 28 trains with intermediate stations ready.");
        } catch (SQLException e) {
            System.out.println("Database setup error: " + e.getMessage());
        }
    }
}