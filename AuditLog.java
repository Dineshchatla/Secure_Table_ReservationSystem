public class AuditLog {
    private int id;
    private String username;
    private String action;
    private java.sql.Timestamp timestamp;
    public AuditLog(int id, String username, String action, java.sql.Timestamp timestamp) {
        this.id = id; this.username = username; this.action = action; this.timestamp = timestamp;
    }
}
