import java.sql.*;
import java.util.*;
import java.io.*;
public class Beispiel {
    static void createTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS kunde(id INTEGER PRIMARY KEY AUTO_INCREMENT, name Varchar (255), land Varchar (255))");
        stmt.execute("CREATE TABLE IF NOT EXISTS produkt (id INTEGER PRIMARY KEY AUTO-INCREMENT, name VARCHAR (255), preis DOUBLE)");
        stmt.execute("CREATE TABLE IF NOT EXISTS bestellung(id INTEGER PRIMARY KEY AUTO_INCREMENT, bestelldatum DATE, kunde_id INTEGER, FOREIGN KEY (kunde_id) REFERENCES kunde(id), produkt_id INTEGER, FOREIGN KEY (produkt_id) REFERENCES produkt(id))");
    }
}
static void insertKunde(Connection conn, String name, String land) throws SQLExeption {
    String sql = "INSERT INTO kunde (name, land) VALUES (?, ?)";
    PreparedStatment pstmt = conn.prepareStatement(sql);
    pstmt.setString(1, name);
    pstmt.setString(2,land);
    pstmt.executeUpdate();
}
static void insertProdukt (Connection conn, String name, double preis) throws SQLExeption {
    String sql = "INSERT INTO produkt (name, preis) VALUES (?, ?)";
    PreparedStatment pstmt = conn.prepareStatement(sql);
    pstmt.SetString(1, name);
    pstmt.SetString(2, preis);
    pstmt.executeUpdate();
}
static void insertBestellung (Connection conn, String name) throws SQLExeption {
String sql = "INSERT INTO bestellung (bestelldatum, kunde_id, produkt_id) VALUES (?, ?, ?) ";
PreparedStatment pstmt = conn.preparedStatment(sql);
pstmt.SetDate(1, DATE.valueOf(bestelldatum));
pstmt.SetString(2, kunde_id);
pstmt.SetString(3, produkt_id);
pstmt.executeUpdate();

}
void LandKunde (Connection conn) throws SQLExeption {
    String sql = "SELECT land, COUNT(id) FROM kunde GROUP BY land HAVING COUNT(id) >1"; // Having wird benutzt, um die Gruppen zu filtern, die mehr als 1 Kunde haben. Es wird immer nach einer Gruppierung benutzt, also in diesem Fall nach land.
}
void alleKundenZeigen(Connection conn) throws SQLExeption {
    String sql = "SELECT * FROM kunde WHERE land = 'Deutschland'UNION SELECT * FROM kunde WHERE land = 'Österreich' "; // UNION wird benutzt, um die Ergebnisse von zwei SELECT Anfragen zu kombinieren. Es werden nur die Kunden aus Deutschland und Österreich angezeigt.
}
void teureProdukte (Connection conn) throws SQLExeption {
    String sql = "SELECT name, preis FROM produkt WHERE preis > (SELECT AVG(preis) FROM produkt) ";// Es werden nur die Produkte angezeigt, deren Preis über dem Durchschnittspreis aller Produkte liegt. Es wird eine Unterabfrage benutzt, um den Durchschnittspreis zu berechnen.
}
void main(String[] args) {
    String url = "jdbc:mysql://localhost:3306/your_database_name";  
    String user = "";
    String password = "";
    try (Connection conn = DriverManager.getConnection(url, user, password)) {
        createTable(conn);
        insertKunde(conn, "Max Mustermann", "Deutschland");
        insertProdukt(conn, "Laptop", 999.99);
        insertBestellung(conn, "2025-02-18", 1, 1);
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
