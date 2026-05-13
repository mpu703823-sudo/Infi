import java.sql.*;
import java.sql.Date;

public class BeispielFehlerfrei {

    static void createTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS kunde(id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), land VARCHAR(255)) ENGINE=InnoDB");
        stmt.execute("CREATE TABLE IF NOT EXISTS produkt(id INTEGER PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), preis DOUBLE) ENGINE=InnoDB");
        stmt.execute("CREATE TABLE IF NOT EXISTS bestellung(id INTEGER PRIMARY KEY AUTO_INCREMENT, bestelldatum DATE, kunde_id INTEGER, produkt_id INTEGER, FOREIGN KEY (kunde_id) REFERENCES kunde(id), FOREIGN KEY (produkt_id) REFERENCES produkt(id)) ENGINE=InnoDB");
    }

    static void insertKunde(Connection conn, String name, String land) throws SQLException {
        String sql = "INSERT INTO kunde (name, land) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setString(2, land);
        pstmt.executeUpdate();
    }

    static void insertProdukt(Connection conn, String name, double preis) throws SQLException {
        String sql = "INSERT INTO produkt (name, preis) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, name);
        pstmt.setDouble(2, preis);
        pstmt.executeUpdate();
    }

    static void insertBestellung(Connection conn, String bestelldatum, int kunde_id, int produkt_id) throws SQLException {
        String sql = "INSERT INTO bestellung (bestelldatum, kunde_id, produkt_id) VALUES (?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setDate(1, Date.valueOf(bestelldatum));
        pstmt.setInt(2, kunde_id);
        pstmt.setInt(3, produkt_id);
        pstmt.executeUpdate();
    }

    void landKunde(Connection conn) throws SQLException {
        String sql = "SELECT land, COUNT(id) FROM kunde GROUP BY land HAVING COUNT(id) > 1";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getString(1) + " " + rs.getInt(2));
        }
    }

    void alleKundenZeigen(Connection conn) throws SQLException {
        String sql = "SELECT * FROM kunde WHERE land = 'Deutschland' UNION SELECT * FROM kunde WHERE land = 'Österreich'";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getInt("id") + " " + rs.getString("name") + " " + rs.getString("land"));
        }
    }

    void teureProdukte(Connection conn) throws SQLException {
        String sql = "SELECT name, preis FROM produkt WHERE preis > (SELECT AVG(preis) FROM produkt)";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getString("name") + " " + rs.getDouble("preis"));
        }
    }

    void withBeispiel(Connection conn) throws SQLException {
        String sql = "WITH durchschnitt AS (SELECT AVG(preis) AS avg_preis FROM produkt) " +
                     "SELECT p.name, p.preis FROM produkt p, durchschnitt d " +
                     "WHERE p.preis > d.avg_preis";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            System.out.println(rs.getString("name") + " " + rs.getDouble("preis"));
        }
    }

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/bsp18022026";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {

            createTable(conn);

            insertKunde(conn, "Max Mustermann", "Deutschland");
            insertKunde(conn, "Anna Berger", "Österreich");
            insertKunde(conn, "Peter Müller", "Deutschland");

            insertProdukt(conn, "Laptop", 999.99);
            insertProdukt(conn, "Maus", 19.99);
            insertProdukt(conn, "Monitor", 199.99);

            insertBestellung(conn, "2025-02-18", 1, 1);

            BeispielFehlerfrei b = new BeispielFehlerfrei();

            b.alleKundenZeigen(conn);
            b.landKunde(conn);
            b.teureProdukte(conn);
            b.withBeispiel(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}