package monstercardtradinggame.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UnitOfWork implements AutoCloseable {

    private Connection connection;
    public UnitOfWork() {
        this.connection = DatabaseManager.INSTANCE.getConnection();
        try {
            this.connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Autocommit nicht deaktivierbar", e);
        }
    }


    /**
     * commits the current local state to db to persist it
     */
    public void commitTransaction() {
        if (this.connection != null) {
            try {
                this.connection.commit();
            } catch (SQLException e) {
                throw new DataAccessException("Commit der Transaktion nicht erfolgreich", e);
            }
        }
    }


    /**
     * closes connection on Program exit
     */
    public void finishWork() {
        if (this.connection != null) {
            try {
                this.connection.close();
                this.connection = null;
            } catch (SQLException e) {
                throw new DataAccessException("Schließen der Connection nicht erfolgreich", e);
            }
        }
    }


    /**
     * creates a new Prepared Statement, is needed for SQL-Queries where you need specific data
     * @param sql
     * @return prepared statement for manipulation
     */
    public PreparedStatement prepareStatement(String sql) {
        if (this.connection != null) {
            try {
                return this.connection.prepareStatement(sql);
            } catch (SQLException e) {
                throw new DataAccessException("Erstellen eines PreparedStatements nicht erfolgreich", e);
            }
        }
        throw new DataAccessException("UnitOfWork hat keine aktive Connection zur Verfügung");
    }


    /**
     * if for some reason, the query fails after changes had been written local, this rolls back to the last commit
     */
    public void rollbackTransaction() {

        if (this.connection != null) {
            try {
                this.connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException("Rollback nicht erfolgreich", e);
            }
        }
    }


    @Override
    public void close() throws Exception {
        this.finishWork();
    }
}
