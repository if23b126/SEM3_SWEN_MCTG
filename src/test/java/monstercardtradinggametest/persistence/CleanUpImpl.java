package monstercardtradinggametest.persistence;

import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CleanUpImpl implements CleanUp {
    private UnitOfWork unitOfWork;

    public CleanUpImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public void deleteTableUsers() {
        try(PreparedStatement drop = this.unitOfWork.prepareStatement("""
                DROP TABLE IF EXISTS public.users
            """)) {
            drop.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("Error cleaning up database", e);
        }
    }

    @Override
    public void deleteTableLogin() {
        try(PreparedStatement drop = this.unitOfWork.prepareStatement("""
                DROP TABLE IF EXISTS public.currently_logged_in
            """)) {
            drop.executeUpdate();
            this.unitOfWork.commitTransaction();
        } catch (SQLException e) {
            throw new DataAccessException("Error cleaning up database", e);
        }
    }
}
