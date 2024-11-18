package monstercardtradinggame.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DatabaseManager {
    INSTANCE;

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/root",
                    "mctg",
                    "mctg");
        } catch (SQLException e) {
            throw new DataAccessException("Datenbankverbindung nicht erfolgreich", e);
        }
    }
}
