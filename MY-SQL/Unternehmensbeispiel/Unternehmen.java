import java.sql.*;
import java.util.*;
import java.io.*;

public class Unternehmen {

    static List<String> importDateien = new ArrayList<>();
    static List<String> exportDateien = new ArrayList<>();

    public static void main(String[] args) {
        try {
            Database.init();
            KundenService ks = new KundenService();
            ArtikelService as = new ArtikelService();
            BestellService bs = new BestellService();

            Scanner sc = new Scanner(System.in);
            boolean running = true;

            while (running) {
                System.out.println("\n--- MENÜ ---");
                System.out.println("1 Kunde anlegen");
                System.out.println("2 Artikel anlegen");
                System.out.println("3 Artikel bestellen");
                System.out.println("4 Kunden anzeigen");
                System.out.println("5 Artikel anzeigen");
                System.out.println("6 Bestellungen eines Kunden anzeigen");
                System.out.println("7 CSV Kunden importieren");
                System.out.println("8 CSV Artikel importieren");
                System.out.println("9 JSON Kunden exportieren");
                System.out.println("10 JSON Bestellungen exportieren");
                System.out.println("11 Importierte Dateien verwalten");
                System.out.println("12 Exportierte Dateien verwalten");
                System.out.println("0 Exit");
                System.out.print("> ");

                String input = sc.nextLine();
                if (input.equalsIgnoreCase("exit") || input.equals("0")) break;

                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (Exception e) {
                    continue;
                }

                switch (choice) {
                    case 1 -> {
                        System.out.print("Name: ");
                        String n = read(sc);
                        System.out.print("Email: ");
                        String e = read(sc);
                        ks.addKunde(n, e);
                        Thread.sleep(1500);
                    }
                    case 2 -> {
                        System.out.print("Bezeichnung: ");
                        String b = read(sc);
                        System.out.print("Preis: ");
                        double p = Double.parseDouble(read(sc));
                        as.addArtikel(b, p);
                        Thread.sleep(1500);
                    }
                    case 3 -> {
                        System.out.print("Kunden-ID: ");
                        int k = Integer.parseInt(read(sc));
                        System.out.print("Artikel-ID: ");
                        int a = Integer.parseInt(read(sc));
                        System.out.print("Anzahl: ");
                        int an = Integer.parseInt(read(sc));
                        bs.bestelleArtikel(k, a, an);
                        Thread.sleep(1500);
                    }
                    case 4 -> {
                        ks.showKunden();
                        Thread.sleep(1500);
                    }
                    case 5 -> {
                        as.showArtikel();
                        Thread.sleep(1500);
                    }
                    case 6 -> {
                        System.out.print("Kunden-ID: ");
                        int kid = Integer.parseInt(read(sc));
                        bs.showBestellungenVonKunde(kid);
                        Thread.sleep(1500);
                    }
                    case 7 -> {
                        System.out.print("CSV-Dateiname: ");
                        CSVImportService.importKunden(read(sc));
                    }
                    case 8 -> {
                        System.out.print("CSV-Dateiname: ");
                        CSVImportService.importArtikel(read(sc));
                    }
                    case 9 -> {
                        System.out.print("JSON-Dateiname: ");
                        JSONExportService.exportKunden(read(sc));
                    }
                    case 10 -> {
                        System.out.print("Kunden-ID: ");
                        int kid = Integer.parseInt(read(sc));
                        System.out.print("JSON-Dateiname: ");
                        JSONExportService.exportBestellungenVonKunde(kid, read(sc));
                    }
                    case 11 -> DateiVerwaltung.verwalten(importDateien, true);
                    case 12 -> DateiVerwaltung.verwalten(exportDateien, false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Database.close();
        }
    }

    static String read(Scanner sc) {
        String s = sc.nextLine();
        if (s.equalsIgnoreCase("exit")) throw new RuntimeException("EXIT");
        return s;
    }

    static class DateiVerwaltung {
        static void verwalten(List<String> list, boolean isImport) throws Exception {
            Scanner sc = new Scanner(System.in);
            if (list.isEmpty()) {
                System.out.println("Keine Dateien vorhanden");
                Thread.sleep(1500);
                return;
            }

            while (true) {
                System.out.println("exit = zurück");
                for (int i = 0; i < list.size(); i++)
                    System.out.println((i + 1) + ". " + list.get(i));

                String in = sc.nextLine();
                if (in.equalsIgnoreCase("exit")) return;

                int idx;
                try {
                    idx = Integer.parseInt(in) - 1;
                } catch (Exception e) {
                    continue;
                }

                if (idx < 0 || idx >= list.size()) continue;

                String file = list.get(idx);

                System.out.println("1 Löschen");
                System.out.println("2 Aktualisieren");
                System.out.println("3 Exit");

                String c = sc.nextLine();
                if (c.equalsIgnoreCase("exit") || c.equals("3")) return;

                if (c.equals("1")) {
                    new File(file).delete();
                    list.remove(idx);
                    System.out.println("Datei erfolgreich gelöscht");
                } else if (c.equals("2")) {
                    if (isImport) {
                        if (file.endsWith(".csv")) CSVImportService.importKunden(file);
                    } else {
                        JSONExportService.exportKunden(file);
                    }
                    System.out.println("Datei erfolgreich aktualisiert");
                }
                Thread.sleep(1500);
                return;
            }
        }
    }

    static class Database {
        private static Connection conn;

        static void init() throws Exception {
            Properties cfg = new Properties();
            cfg.load(new FileInputStream("C:/HTL/3AHWII/Infi/sqlite-tools-win-x64-3500400/Infi-Aufgaben/db_config.properties"));
            conn = DriverManager.getConnection(cfg.getProperty("db.url"), cfg.getProperty("db.user"), cfg.getProperty("db.pass"));
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE TABLE IF NOT EXISTS kunden (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(255), email VARCHAR(255) UNIQUE)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS artikel (id INT AUTO_INCREMENT PRIMARY KEY, bezeichnung VARCHAR(255), preis DOUBLE, angelegt_am DATE)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS bestellungen (id INT AUTO_INCREMENT PRIMARY KEY, kundenID INT, artikelID INT, anzahl INT, bestellt_am DATETIME)");
        }

        static Connection get() { return conn; }

        static void close() {
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    static class KundenService {
        int addKunde(String name, String email) throws SQLException {
            try (PreparedStatement ps = Database.get().prepareStatement("INSERT INTO kunden(name,email) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.executeUpdate();
            } catch (Exception ignored) {}
            return 0;
        }

        void showKunden() throws SQLException {
            ResultSet rs = Database.get().createStatement().executeQuery("SELECT * FROM kunden");
            while (rs.next())
                System.out.println(rs.getInt("id") + " : " + rs.getString("name") + " : " + rs.getString("email"));
        }
    }

    static class ArtikelService {
        void addArtikel(String bez, double preis) throws SQLException {
            PreparedStatement ps = Database.get().prepareStatement("INSERT INTO artikel(bezeichnung,preis,angelegt_am) VALUES (?,?,?)");
            ps.setString(1, bez);
            ps.setDouble(2, preis);
            ps.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            ps.executeUpdate();
        }

        void showArtikel() throws SQLException {
            ResultSet rs = Database.get().createStatement().executeQuery("SELECT * FROM artikel");
            while (rs.next())
                System.out.println(rs.getInt("id") + " : " + rs.getString("bezeichnung") + " : " + rs.getDouble("preis"));
        }
    }

    static class BestellService {
        void bestelleArtikel(int k, int a, int an) throws SQLException {
            PreparedStatement ps = Database.get().prepareStatement("INSERT INTO bestellungen(kundenID,artikelID,anzahl,bestellt_am) VALUES (?,?,?,?)");
            ps.setInt(1, k);
            ps.setInt(2, a);
            ps.setInt(3, an);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        }

        void showBestellungenVonKunde(int kid) throws SQLException {
            PreparedStatement ps = Database.get().prepareStatement("SELECT a.bezeichnung,b.anzahl,a.preis FROM bestellungen b JOIN artikel a ON a.id=b.artikelID WHERE b.kundenID=?");
            ps.setInt(1, kid);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                System.out.println(rs.getString("bezeichnung") + " x" + rs.getInt("anzahl"));
        }
    }

    static class CSVImportService {
        static void importKunden(String pfad) throws Exception {
            File f = new File(pfad);
            if (!f.exists()) {
                System.out.println("Es konnte keine Datei gefunden werden !");
                Thread.sleep(1500);
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            br.readLine();
            KundenService ks = new KundenService();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                ks.addKunde(p[0], p[1]);
            }
            br.close();
            importDateien.add(pfad);
            System.out.println("Kunde erfolgreich importiert");
            Thread.sleep(1500);
        }

        static void importArtikel(String pfad) throws Exception {
            File f = new File(pfad);
            if (!f.exists()) {
                System.out.println("Es konnte keine Datei gefunden werden !");
                Thread.sleep(1500);
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(f));
            br.readLine();
            ArtikelService as = new ArtikelService();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                as.addArtikel(p[0], Double.parseDouble(p[1]));
            }
            br.close();
            importDateien.add(pfad);
            System.out.println("Artikel erfolgreich importiert");
            Thread.sleep(1500);
        }
    }

    static class JSONExportService {
        static void exportKunden(String pfad) throws Exception {
            StringBuilder json = new StringBuilder("[\n");
            ResultSet rs = Database.get().createStatement().executeQuery("SELECT * FROM kunden");
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",\n");
                first = false;
                json.append("  {\"id\": ").append(rs.getInt("id"))
                        .append(", \"name\": \"").append(rs.getString("name"))
                        .append("\", \"email\": \"").append(rs.getString("email")).append("\"}");
            }
            json.append("\n]");
            FileWriter fw = new FileWriter(pfad);
            fw.write(json.toString());
            fw.close();
            exportDateien.add(pfad);
            System.out.println("Export erfolgreich");
            Thread.sleep(1500);
        }

        static void exportBestellungenVonKunde(int kid, String pfad) throws Exception {
            StringBuilder json = new StringBuilder("[\n");
            PreparedStatement ps = Database.get().prepareStatement("SELECT a.bezeichnung,b.anzahl,a.preis FROM bestellungen b JOIN artikel a ON a.id=b.artikelID WHERE b.kundenID=?");
            ps.setInt(1, kid);
            ResultSet rs = ps.executeQuery();
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",\n");
                first = false;
                json.append("  {\"artikel\": \"").append(rs.getString("bezeichnung"))
                        .append("\", \"anzahl\": ").append(rs.getInt("anzahl"))
                        .append(", \"preis\": ").append(rs.getDouble("preis")).append("}");
            }
            json.append("\n]");
            FileWriter fw = new FileWriter(pfad);
            fw.write(json.toString());
            fw.close();
            exportDateien.add(pfad);
            System.out.println("Export erfolgreich");
            Thread.sleep(1500);
        }
    }
}
