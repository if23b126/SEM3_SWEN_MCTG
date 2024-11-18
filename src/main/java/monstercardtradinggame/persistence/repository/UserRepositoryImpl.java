package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.model.Token;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class UserRepositoryImpl implements UserRepository {
    private UnitOfWork unitOfWork;

    public UserRepositoryImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }


    @Override
    public String Login(String username, String password) {
        String result = null;
        try (PreparedStatement select=
                this.unitOfWork.prepareStatement("""
                        SELECT * FROM mctg.users
                        where username = ?
                        """);
            PreparedStatement insert =
                this.unitOfWork.prepareStatement("""
                        INSERT INTO mctg.currently_logged_in(username, token)
                        VALUES (?, ?)
                        """))
        {
            select.setString(1, username);
            ResultSet rs = select.executeQuery();
            User user = null;
            while(rs.next()) {
                user = new User(rs.getString(1), rs.getString(2));
            }

            if (user.getPassword().equals(password)) {
                Token token = new Token();
                String generatedToken = token.generateNewToken();
                insert.setString(1, username);
                insert.setString(2, generatedToken);
                insert.executeUpdate();
                result = generatedToken;
            }

            unitOfWork.commitTransaction();
        }catch (SQLException e) {
            throw new DataAccessException("Login SQL nicht erfolgreich", e);
        }
        return result;
    }

    @Override
    public Boolean Logout(String token) {
        return null;
    }

    @Override
    public Boolean Register(String username, String password) {
        return null;
    }
}
