import java.sql.Date;
import java.sql.Time;

public class Reservation {
    private int id;
    private String customerName;
    private String contactEncrypted;
    private int tableNumber;
    private Date reservationDate;
    private Time reservationTime;

    // Constructor
    public Reservation(int id, String customerName, String contactEncrypted, int tableNumber, Date reservationDate, Time reservationTime) {
        this.id = id;
        this.customerName = customerName;
        this.contactEncrypted = contactEncrypted;
        this.tableNumber = tableNumber;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getContactEncrypted() {
        return contactEncrypted;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public Date getReservationDate() {
        return reservationDate;
    }

    public Time getReservationTime() {
        return reservationTime;
    }

    // toString method to display reservation info
    @Override
    public String toString() {
        String decryptedContact = contactEncrypted; // default to encrypted in case of error
        try {
            decryptedContact = EncryptionUtil.decrypt(contactEncrypted); // Attempt decryption
        } catch (Exception e) {
            System.out.println("Error decrypting contact: " + e.getMessage());
        }

        return String.format("ID: %d | Name: %s | Contact: %s | Table: %d | Date: %s | Time: %s",
                id, customerName, decryptedContact, tableNumber, reservationDate, reservationTime);
    }
}
