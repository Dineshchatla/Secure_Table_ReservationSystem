import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Scanner sc = new Scanner(System.in);
    private static User currentUser = null;

    public static void main(String[] args) {
        try {
            System.out.println("Welcome to Secure Table Reservation System (Basic)");

            while (true) {
                if (currentUser == null) {
                    showLoginMenu();
                } else {
                    if ("admin".equalsIgnoreCase(currentUser.getRole())) {
                        showAdminMenu();
                    } else {
                        showStaffMenu();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showLoginMenu() throws Exception {
        System.out.println("\n1) Login\n2) Exit");
        System.out.print("Choose: ");
        String c = sc.nextLine().trim();
        if (c.equals("1")) login();
        else if (c.equals("2")) { System.out.println("Goodbye"); System.exit(0); }
    }

    private static void login() throws Exception {
        System.out.print("Username: "); String username = sc.nextLine().trim();
        System.out.print("Password: "); String password = sc.nextLine().trim();
        String hashed = SecurityUtil.sha256(password);

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, username, role FROM users WHERE username = ? AND password_hash = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, hashed);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentUser = new User(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                        logAction(conn, currentUser.getUsername(), "LOGIN");
                        System.out.println("Login successful. Role: " + currentUser.getRole());
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                }
            }
        }
    }

    private static void showAdminMenu() throws Exception {
        System.out.println("\nAdmin Menu:\n1) Add Reservation\n2) View Reservations\n3) Delete Reservation\n4) Add Staff User\n5) Logout");
        System.out.print("Choose: "); String c = sc.nextLine().trim();
        switch (c) {
            case "1": addReservation(); break;
            case "2": viewReservations(); break;
            case "3": deleteReservation(); break;
            case "4": addStaffUser(); break;
            case "5": logout(); break;
            default: System.out.println("Invalid"); break;
        }
    }

    private static void showStaffMenu() throws Exception {
        System.out.println("\nStaff Menu:\n1) Add Reservation\n2) View Reservations\n3) Logout");
        System.out.print("Choose: "); String c = sc.nextLine().trim();
        switch (c) {
            case "1": addReservation(); break;
            case "2": viewReservations(); break;
            case "3": logout(); break;
            default: System.out.println("Invalid"); break;
        }
    }

    private static void addReservation() throws Exception {
        System.out.print("Customer Name: "); String name = sc.nextLine().trim();
        System.out.print("Contact Number: "); String contact = sc.nextLine().trim();
        System.out.print("Table Number (int): "); int table = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Reservation Date (YYYY-MM-DD): "); String date = sc.nextLine().trim();
        System.out.print("Reservation Time (HH:MM:SS): "); String time = sc.nextLine().trim();

        String encrypted = EncryptionUtil.encrypt(contact);

        try (Connection conn = DBConnection.getConnection()) {
            // Check double booking
            String check = "SELECT COUNT(*) FROM reservations WHERE table_number = ? AND reservation_date = ? AND reservation_time = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, table);
                ps.setDate(2, Date.valueOf(date));
                ps.setTime(3, Time.valueOf(time));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int cnt = rs.getInt(1);
                    if (cnt > 0) {
                        System.out.println("Table already booked at that time.");
                        return;
                    }
                }
            }

            String insert = "INSERT INTO reservations (customer_name, contact_number, table_number, reservation_date, reservation_time) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, name);
                ps.setString(2, encrypted);
                ps.setInt(3, table);
                ps.setDate(4, Date.valueOf(date));
                ps.setTime(5, Time.valueOf(time));
                ps.executeUpdate();
            }
            logAction(conn, currentUser.getUsername(), "ADD_RESERVATION: " + name + " table:" + table + " date:" + date + " time:" + time);
            System.out.println("Reservation added.");
        }
    }

    private static void viewReservations() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            String q = "SELECT id, customer_name, contact_number, table_number, reservation_date, reservation_time FROM reservations ORDER BY reservation_date, reservation_time";
            try (PreparedStatement ps = conn.prepareStatement(q); ResultSet rs = ps.executeQuery()) {
                System.out.println("\nReservations:");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("customer_name"); String contactEnc = rs.getString("contact_number");
                    int table = rs.getInt("table_number");
                    java.sql.Date d = rs.getDate("reservation_date"); java.sql.Time t = rs.getTime("reservation_time");
                    String contactDec = "(decryption-error)";
                    try { contactDec = EncryptionUtil.decrypt(contactEnc); } catch (Exception e) { /* ignore */ }
                    System.out.println(String.format("ID:%d | %s | %s | Table:%d | %s %s", id, name, contactDec, table, d.toString(), t.toString()));
                }
            }
            logAction(conn, currentUser.getUsername(), "VIEW_RESERVATIONS");
        }
    }

    private static void deleteReservation() throws Exception {
        System.out.print("Reservation ID to delete: "); int id = Integer.parseInt(sc.nextLine().trim());
        try (Connection conn = DBConnection.getConnection()) {
            String del = "DELETE FROM reservations WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(del)) {
                ps.setInt(1, id);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    System.out.println("Deleted."); logAction(conn, currentUser.getUsername(), "DELETE_RESERVATION_ID:" + id);
                } else {
                    System.out.println("No reservation found with that ID.");
                }
            }
        }
    }

    private static void addStaffUser() throws Exception {
        System.out.print("New staff username: "); String username = sc.nextLine().trim();
        System.out.print("Password for staff: "); String pw = sc.nextLine().trim();
        String hashed = SecurityUtil.sha256(pw);
        try (Connection conn = DBConnection.getConnection()) {
            String insert = "INSERT INTO users (username, password_hash, role) VALUES (?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, username);
                ps.setString(2, hashed);
                ps.setString(3, "staff");
                ps.executeUpdate();
            }
            logAction(conn, currentUser.getUsername(), "ADD_STAFF_USER:" + username);
            System.out.println("Staff user added.");
        }
    }

    private static void logout() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            logAction(conn, currentUser.getUsername(), "LOGOUT");
        }
        currentUser = null;
        System.out.println("Logged out.");
    }

    private static void logAction(Connection conn, String username, String action) throws SQLException {
        String sql = "INSERT INTO audit_logs (username, action) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, action);
            ps.executeUpdate();
        }
    }
}
