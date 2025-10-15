import java.sql.*;

public class SQLiteAllInOne {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:test.db";

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Treiber nicht gefunden!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(url)) {
            conn.setAutoCommit(false);
            System.out.println("=== SQLITE ===");

            createTable(conn);
            insertData(conn);
            selectAll(conn);
            selectWhereAge(conn, 25);
            updateSalaryByName(conn, "Paul", 30000);
            updateSalaryWithAndOr(conn);
            selectWhereAddressLike(conn, "%Strasse%");
            deleteWhereAgeLessThan(conn, 23);
            selectAll(conn);

            conn.commit();
            System.out.println("=== Operationen abgeschlossen ===");

        } catch (SQLException e) {
            System.err.println("SQL-Fehler: " + e.getMessage());
        }
    }

    // Tabelle erstellen
    private static void createTable(Connection conn) throws SQLException {
        String createTable = """
            CREATE TABLE IF NOT EXISTS COMPANY (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                NAME TEXT NOT NULL,
                AGE INT NOT NULL,
                ADDRESS CHAR(50),
                SALARY REAL
            );
            """;
        try (Statement st = conn.createStatement()) {
            st.execute(createTable);
        }
    }

    // Daten einfügen
    private static void insertData(Connection conn) throws SQLException {
        String insertSQL = "INSERT INTO COMPANY (NAME, AGE, ADDRESS, SALARY) VALUES (?, ?, ?, ?);";
        String[][] daten = {
            {"Paul", "32", "Berlin", "20000"},
            {"Allen", "25", "München", "15000"},
            {"Teddy", "23", "Hamburg", "20000"},
            {"Mark", "25", "Berlin Strasse 1", "65000"},
            {"David", "27", "Bergstrasse 3", "85000"},
            {"Kim", "22", "Köln", "45000"}
        };
        try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
            for (String[] d : daten) {
                ps.setString(1, d[0]);
                ps.setInt(2, Integer.parseInt(d[1]));
                ps.setString(3, d[2]);
                ps.setDouble(4, Double.parseDouble(d[3]));
                try {
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    // Datensatz evtl. schon vorhanden → ignorieren
                }
            }
        }
    }

    // Alle Datensätze auslesen
    private static void selectAll(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM COMPANY ORDER BY ID;")) {
            printResultSet(rs);
        }
    }

    // Datensätze mit WHERE AGE > X auslesen
    private static void selectWhereAge(Connection conn, int age) throws SQLException {
        String sql = "SELECT * FROM COMPANY WHERE AGE > ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, age);
            try (ResultSet rs = ps.executeQuery()) {
                printResultSet(rs);
            }
        }
    }

    // UPDATE Gehalt nach Name
    private static void updateSalaryByName(Connection conn, String name, double salary) throws SQLException {
        String sql = "UPDATE COMPANY SET SALARY = ? WHERE NAME = ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, salary);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    // UPDATE mit AND / OR
    private static void updateSalaryWithAndOr(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("UPDATE COMPANY SET SALARY = SALARY + 5000 WHERE AGE < 26 AND ADDRESS LIKE '%Berlin%';");
            st.executeUpdate("UPDATE COMPANY SET SALARY = SALARY + 2000 WHERE NAME = 'Allen' OR NAME = 'Kim';");
        }
    }

    // SELECT WHERE ADDRESS LIKE
    private static void selectWhereAddressLike(Connection conn, String pattern) throws SQLException {
        String sql = "SELECT * FROM COMPANY WHERE ADDRESS LIKE ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                printResultSet(rs);
            }
        }
    }

    // DELETE WHERE AGE < X
    private static void deleteWhereAgeLessThan(Connection conn, int age) throws SQLException {
        String sql = "DELETE FROM COMPANY WHERE AGE < ?;";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, age);
            ps.executeUpdate();
        }
    }

    // ResultSet ausgeben
    private static void printResultSet(ResultSet rs) throws SQLException {
        while (rs.next()) {
            int id = rs.getInt("ID");
            String name = rs.getString("NAME");
            int age = rs.getInt("AGE");
            String address = rs.getString("ADDRESS");
            double salary = rs.getDouble("SALARY");
            System.out.printf("ID=%d | NAME=%s | AGE=%d | ADDRESS=%s | SALARY=%.2f%n",
                    id, name, age, address, salary);
        }
    }
}
