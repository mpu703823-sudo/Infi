import java.sql.*;

public class FKDemoMySQLPrepared {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/demo_db";
    private static final String USER = "root";      
    private static final String PASS = ""; 
    private static final String KUNDEN_TABELLE = "kunden_even";
    private static final String BESTELLUNGEN_TABELLE = "bestellungen_odd";

    public static void main(String[] args) {
        System.out.println("=== FKDemo mit MySQL  ===\n");
        
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("FEHLER: MySQL Treiber nicht gefunden.");
            return;
        }

        if (!ohneCascade()) System.err.println("\nTeil 1 fehlgeschlagen");
        System.out.println("\n" + "=".repeat(30) + "\n");
        if (!mitCascade()) System.err.println("\nTeil 2 fehlgeschlagen");
    }
    
    static boolean ohneCascade() {
        System.out.println("OHNE CASCADE:");
        try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
            c.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            c.createStatement().execute("DROP TABLE IF EXISTS " + BESTELLUNGEN_TABELLE);
            c.createStatement().execute("DROP TABLE IF EXISTS " + KUNDEN_TABELLE);
            c.createStatement().execute("CREATE TABLE " + KUNDEN_TABELLE + " (id INT PRIMARY KEY, name VARCHAR(255))");
            c.createStatement().execute("CREATE TABLE " + BESTELLUNGEN_TABELLE + " (id INT PRIMARY KEY, kid INT, FOREIGN KEY (kid) REFERENCES " + KUNDEN_TABELLE + "(id))");
            
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + KUNDEN_TABELLE + " VALUES (?, ?)")) {
                ps.setInt(1, 2);
                ps.setString(2, "Max");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + BESTELLUNGEN_TABELLE + " VALUES (?, ?)")) {
                ps.setInt(1, 101);
                ps.setInt(2, 2);
                ps.executeUpdate();
            }
            
            System.out.println("Vorher:");
            zeige(c);
            
            System.out.println("\nVersuche Kunde zu LÖSCHEN...");
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + KUNDEN_TABELLE + " WHERE id = ?")) {
                ps.setInt(1, 2);
                ps.executeUpdate();
                System.out.println(" Gelöscht");
            } catch (SQLException e) {
                System.out.println(" FEHLER (Erwartet): " + e.getMessage());
            }
            
            System.out.println("\nVersuche Kunden-ID zu ÄNDERN...");
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + KUNDEN_TABELLE + " SET id = ? WHERE id = ?")) {
                ps.setInt(1, 98);
                ps.setInt(2, 2);
                ps.executeUpdate();
                System.out.println(" ID geändert");
            } catch (SQLException e) {
                System.out.println(" FEHLER (Erwartet): " + e.getMessage());
            }
            return true;
        } catch (SQLException e) {
            System.err.println("SQL-Fehler in Teil 1: " + e.getMessage());
            return false;
        }
    }
    
    static boolean mitCascade() {
        System.out.println("MIT CASCADE:");
        try (Connection c = DriverManager.getConnection(DB_URL, USER, PASS)) {
            c.createStatement().execute("SET FOREIGN_KEY_CHECKS = 1");
            c.createStatement().execute("DROP TABLE IF EXISTS " + BESTELLUNGEN_TABELLE);
            c.createStatement().execute("DROP TABLE IF EXISTS " + KUNDEN_TABELLE);
            c.createStatement().execute("CREATE TABLE " + KUNDEN_TABELLE + " (id INT PRIMARY KEY, name VARCHAR(255))");
            c.createStatement().execute("CREATE TABLE " + BESTELLUNGEN_TABELLE + " (id INT PRIMARY KEY, kid INT, FOREIGN KEY (kid) REFERENCES " + KUNDEN_TABELLE + "(id) ON DELETE CASCADE ON UPDATE CASCADE)");
            
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + KUNDEN_TABELLE + " VALUES (?, ?)")) {
                ps.setInt(1, 4);
                ps.setString(2, "Anna");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO " + BESTELLUNGEN_TABELLE + " VALUES (?, ?)")) {
                ps.setInt(1, 203);
                ps.setInt(2, 4);
                ps.executeUpdate();
            }
            
            System.out.println("Vorher:");
            zeige(c);
            
            System.out.println("\nÄndere Kunden-ID 4 → 100...");
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + KUNDEN_TABELLE + " SET id = ? WHERE id = ?")) {
                ps.setInt(1, 100);
                ps.setInt(2, 4);
                ps.executeUpdate();
            }
            System.out.println(" ID geändert (Bestellung auch!)\n");
            zeige(c);
            
            System.out.println("\nLösche Kunde 100...");
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + KUNDEN_TABELLE + " WHERE id = ?")) {
                ps.setInt(1, 100);
                ps.executeUpdate();
            }
            System.out.println(" Gelöscht (Bestellung auch!)\n");
            
            System.out.println("Nachher:");
            zeige(c);
            
            return true;
        } catch (SQLException e) {
            System.err.println("SQL-Fehler in Teil 2: " + e.getMessage());
            return false;
        }
    }
    
    static boolean zeige(Connection c) {
        try {
            try (PreparedStatement ps = c.prepareStatement("SELECT id, name FROM " + KUNDEN_TABELLE)) {
                ResultSet rs = ps.executeQuery();
                System.out.print("Kunden: ");
                while (rs.next()) System.out.print("ID=" + rs.getInt("id") + " (" + rs.getString("name") + ") ");
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id, kid FROM " + BESTELLUNGEN_TABELLE)) {
                ResultSet rs = ps.executeQuery();
                System.out.print("\nBestellungen: ");
                while (rs.next()) System.out.print("ID=" + rs.getInt("id") + " KID=" + rs.getInt("kid") + " ");
            }
            System.out.println();
            return true;
        } catch (SQLException e) {
            System.err.println("Fehler beim Anzeigen der Tabellen: " + e.getMessage());
            return false;
        }
    }
}
