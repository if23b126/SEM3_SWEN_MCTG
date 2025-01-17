package monstercardtradinggametest.persistence;

import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepositoryTestImpl implements UserRespositoryTest {
    private UnitOfWork unitOfWork;

    public UserRepositoryTestImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public User getUsers() {
        User result = null;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT username, password, coins, isadmin, wins, losses, ties, elo
                FROM public.users;
            """)) {
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                result = User.builder()
                        .username(rs.getString(1))
                        .password(rs.getString(2))
                        .coins(rs.getInt(3))
                        .isAdmin(rs.getBoolean(4))
                        .wins(rs.getInt(5))
                        .losses(rs.getInt(6))
                        .ties(rs.getInt(7))
                        .elo(rs.getInt(8))
                        .build();
            }
        } catch(SQLException e) {
            throw new DataAccessException("get user count test failed", e);
        }

        return result;
    }

    @Override
    public String[] getCurrentlyLoggedInUser() {
        String[] result = new String[2];
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT username, token
                FROM public.currently_logged_in;
            """)) {
            ResultSet rs = select.executeQuery();
            if (rs.next()) {
                result[0] = rs.getString(1);
                result[1] = rs.getString(2);
            }
        } catch(SQLException e) {
            throw new DataAccessException("get user count test failed", e);
        }

        return result;
    }
}
