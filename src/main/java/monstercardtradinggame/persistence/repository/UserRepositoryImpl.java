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
    public String login(String username, String password) {
        String result = null;
        try (PreparedStatement insert =
                this.unitOfWork.prepareStatement("""
                        INSERT INTO mctg.currently_logged_in(username, token)
                        VALUES (?, ?)
                        """))
        {

            User user = userExists(username);
            if (user.getPassword().equals(password) && tokenExists(username)) {
                Token token = new Token();
                String generatedToken = token.generateNewToken(username);
                insert.setString(1, username);
                insert.setString(2, generatedToken);
                insert.executeUpdate();
                result = generatedToken;
                unitOfWork.commitTransaction();
            }
        }catch (SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Login SQL nicht erfolgreich", e);
        }
        return result;
    }

    @Override
    public Boolean logout(String token) {
        Boolean result = false;
        try (PreparedStatement delete = this.unitOfWork.prepareStatement("""
                DELETE FROM mctg.currently_logged_in 
                where token = ?
                """))
        {
            delete.setString(1, token);
            delete.executeUpdate();
            this.unitOfWork.commitTransaction();
            result = true;
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Logout SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public Boolean register(String username, String password) {
        Boolean result = false;
        try (PreparedStatement insert = this.unitOfWork.prepareStatement("""
                INSERT into mctg.users (username, password)
                values (?, ?)
                """))
        {
            if(userExists(username) == null) {
                insert.setString(1, username);
                insert.setString(2, password);
                insert.executeUpdate();
                this.unitOfWork.commitTransaction();
                result = true;
            }
        } catch(SQLException e){
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Register SQL nicht erfolgreich", e);
        }

        return result;
    }


    public User userExists(String username) {
        try (PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT * FROM mctg.users
                        where username = ?
                """)){
            select.setString(1, username);
            ResultSet rs = select.executeQuery();
            User user = null;
            while(rs.next()) {
                user = new User(rs.getString(1), rs.getString(2));
            }

            return user;
        } catch(SQLException e) {
            throw new DataAccessException("User exists SQL nicht erfolgreich", e);
        }
    }

    public Boolean tokenExists(String token) {
        try (PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT * FROM mctg.currently_logged_in
                        where token = ?
                """)){
            select.setString(1, token);
            ResultSet rs = select.executeQuery();
            return rs.next();

        } catch(SQLException e) {
            throw new DataAccessException("Token exists SQL nicht erfolgreich", e);
        }
    }
}
