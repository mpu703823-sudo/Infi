import java.sql.*;
import java.util.*;

public class RandomTableAppMySQL {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/demo_db";
    private static final String USER = "root";
    private static final String PASS = "";
    private static final String TABLE_NAME = "random_table_mysql";
    
    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new RandomTableAppMySQL().runLoop();
    }

    private void runLoop() {
        System.out.println("Willkommen! Geben Sie die Anzahl der Zeilen ein oder 'Q' zum Beenden.");

        while (true) {
            Connection conn = null;
            try {
                
                Integer count = readRowCountInteractive();
                if (count == null) break;

                conn = connect();
                createTable(conn);
                clearTable(conn);

                insertRandomRows(conn, count);
                printTable(conn);
                printStatisticsDiagram(conn);

                System.out.println("\n--- Analyse abgeschlossen ---\n");

            } catch (ClassNotFoundException e) {
                System.out.println("FEHLER: MySQL Treiber nicht gefunden.");
                break;
            } catch (SQLException e) {
                System.out.println("SQL-Fehler: " + e.getMessage());
                break;
            } catch (Exception e) {
                System.out.println("Unerwarteter Fehler: " + e.getMessage());
                e.printStackTrace();
                break;
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                        System.out.println("Datenbankverbindung geschlossen.");
                    } catch (SQLException e) {
                        System.out.println("Fehler beim Schließen der Verbindung: " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("Programm beendet.");
    }

    private Connection connect() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        System.out.println("Verbindung zur Datenbank hergestellt.");
        return conn;
    }

    private void createTable(Connection conn) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS %s (
                id INTEGER PRIMARY KEY AUTO_INCREMENT,
                value INTEGER NOT NULL,
                value2 INTEGER NOT NULL
            );
        """.formatted(TABLE_NAME);
        
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
        System.out.println("Tabelle geprüft oder erstellt.");
    }

    private void clearTable(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE " + TABLE_NAME); 
        }
        System.out.println("Tabelle geleert.");
    }

    private Integer readRowCountInteractive() {
        int attempts = 0;
        while (true) {
            System.out.print("Anzahl der Zeilen (oder Q zum Beenden): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Q")) return null;

            input = input.replace(',', '.');

            try {
                double d = Double.parseDouble(input);
                int n = (int) Math.round(d);
                if (n <= 0) throw new IllegalArgumentException();
                return n;
            } catch (NumberFormatException e) {
                attempts++;
                System.out.println("Ungültige Zahl. Sie haben einen weiteren Versuch.");
            } catch (IllegalArgumentException e) {
                attempts++;
                System.out.println("Ungültige Zahl (muss > 0). Sie haben einen weiteren Versuch.");
            }

            if (attempts >= 2) {
                System.out.println("Zuviele Fehlversuche. Geben Sie eine gültige Zahl ein oder 'Q' zum Beenden.");
                attempts = 0;
            }
        }
    }

    private void insertRandomRows(Connection conn, int n) throws SQLException {
        Random rand = new Random();
        String sql = "INSERT INTO " + TABLE_NAME + " (value, value2) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < n; i++) {
                int value = rand.nextInt(10) + 1;
                int value2 = value % 2;
                ps.setInt(1, value);
                ps.setInt(2, value2);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println(n + " Zeilen eingefügt.");
    }

    private void printTable(Connection conn) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " LIMIT 20";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("\n=== Inhalt der Tabelle (erste 20 Zeilen) ===");
            System.out.printf("%-5s %-7s %-7s%n", "ID", "Value", "Value2");
            System.out.println("---------------------------");
            while (rs.next()) {
                System.out.printf("%-5d %-7d %-7d%n",
                        rs.getInt("id"),
                        rs.getInt("value"),
                        rs.getInt("value2"));
            }
        }
    }

    private void printStatisticsDiagram(Connection conn) throws SQLException {
        String sqlEven = "SELECT COUNT(*) AS c FROM " + TABLE_NAME + " WHERE value2 = 0";
        String sqlOdd = "SELECT COUNT(*) AS c FROM " + TABLE_NAME + " WHERE value2 = 1";

        int even, odd;

        try (Statement stmt = conn.createStatement()) {
            ResultSet rsEven = stmt.executeQuery(sqlEven);
            even = rsEven.next() ? rsEven.getInt("c") : 0;
            
            ResultSet rsOdd = stmt.executeQuery(sqlOdd);
            odd = rsOdd.next() ? rsOdd.getInt("c") : 0;
        }

        int maxCount = Math.max(even, odd);
        int height = Math.min(maxCount, 10); 
        int scale = maxCount > 10 ? (int) Math.ceil((double) maxCount / 10) : 1;

        System.out.println("\n=== Statistik (Diagramm) ===");

        for (int i = height; i >= 1; i--) {
            int level = i * scale;
            String yLabel = String.format("%3d |", level);
            String evenMark = (even >= level) ? "  #" : "   ";
            String oddMark = (odd >= level) ? "  #" : "   ";
            System.out.println(yLabel + evenMark + oddMark);
        }

        System.out.println("    +-----------");
        System.out.println("      G   U");
        System.out.println("      G = Gerade, U = Ungerade");
        System.out.printf("Gerade: %d, Ungerade: %d%n", even, odd);
    }
}