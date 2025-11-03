    # Secure Table Reservation System (Basic)


    ## Overview

A simple console-based Java project demonstrating:
- SHA-256 password hashing
- AES-GCM encryption for customer contact
- Role-based access (admin/staff)
- PreparedStatements to avoid SQL injection
- Audit logging of actions

## Requirements
- Java 24
- MySQL (server running, create user/root with password)
- MySQL JDBC Driver (add to classpath)

## Setup
1. Start MySQL server.
2. Edit `DBConnection.java` and set the correct MySQL username/password (USER and PASS).
3. Run the SQL script `restaurant_db.sql` to create the database and default admin user:
   ```bash
   mysql -u root -p < restaurant_db.sql
   ```
   Default admin credentials: `admin` / `admin123`.
4. Compile the Java files (ensure MySQL JDBC driver is on classpath). Example using terminal:
   ```bash
   javac -cp .:mysql-connector-java.jar *.java
   java -cp .:mysql-connector-java.jar Main
   ```
   (On Windows replace `:` with `;` in classpath)

## Usage
- Login as `admin` and manage reservations or add staff users.
- Staff users can add/view reservations.
- Contact numbers are stored encrypted in the DB and decrypted when viewing.

## Notes
- This is a basic educational example. In production, never hard-code keys or DB credentials; use secure vaults.
- AES key in `EncryptionUtil` is for demo only.

