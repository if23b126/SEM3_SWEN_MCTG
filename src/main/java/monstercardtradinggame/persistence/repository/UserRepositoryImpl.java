package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.model.Token;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepositoryImpl implements UserRepository {
    private UnitOfWork unitOfWork;

    public UserRepositoryImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }


    /**
     * login method to persist in db, checks if user is existent, if so inserts it into currently_logged_in table
     * @param username
     * @param password
     * @return null if failed, token-string if successful
     */
    @Override
    public String login(String username, String password) {
        String result = null;
        try (PreparedStatement insert =
                this.unitOfWork.prepareStatement("""
                        INSERT INTO public.currently_logged_in(username, token)
                        VALUES (?, ?)
                        """))
        {

            User user = userExists(username);
            if (user.getPassword().equals(password) && !userLoggedIn(username)) {
                Token token = new Token();
                String generatedToken = token.generateNewToken(username);
                insert.setString(1, username);
                insert.setString(2, generatedToken);
                insert.executeUpdate();
                result = generatedToken;
                unitOfWork.commitTransaction(); // persists changes from local to db
            }
        }catch (SQLException e) {
            this.unitOfWork.rollbackTransaction(); // if for some reason, the query fails after executeUpdate() this rolls back to state before commit
            throw new DataAccessException("Login SQL nicht erfolgreich", e);
        }
        return result;
    }


    /**
     * logs out a user, is passed a token, if token exists in currently_logged_in table, user gets logged out
     * @param token
     * @return true is successful, false if failed
     */
    @Override
    public Boolean logout(String token) {
        Boolean result = false;
        try (PreparedStatement delete = this.unitOfWork.prepareStatement("""
                DELETE FROM public.currently_logged_in 
                where token = ?
                """))
        {
            delete.setString(1, token);
            delete.executeUpdate();
            this.unitOfWork.commitTransaction(); // persists changes from local to db
            result = true;
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction(); // if for some reason, the query fails after executeUpdate() this rolls back to state before commit
            throw new DataAccessException("Logout SQL nicht erfolgreich", e);
        }

        return result;
    }


    /**
     * registers a new user, checks if username is already existent, if so return null, if username is not already in db,
     * user will be inserted into users table
     * @param username
     * @param password
     * @return true is successful, false if failed
     */
    @Override
    public Boolean register(String username, String password) {
        Boolean result = false;
        try (PreparedStatement insert = this.unitOfWork.prepareStatement("""
                INSERT into public.users (username, password)
                values (?, ?)
                """))
        {
            if(userExists(username) == null) {
                insert.setString(1, username);
                insert.setString(2, password);
                insert.executeUpdate();
                this.unitOfWork.commitTransaction(); // persists changes from local to db
                result = true;
            }
        } catch(SQLException e){
            this.unitOfWork.rollbackTransaction(); // if for some reason, the query fails after executeUpdate() this rolls back to state before commit
            throw new DataAccessException("Register SQL nicht erfolgreich", e);
        }

        return result;
    }


    @Override
    public Boolean checkIfUserIsLoggedIn(String token) {
        Boolean result = false;
        try (PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT * FROM public.currently_logged_in 
                where token = ?
                """))
        {
            select.setString(1, token);
            ResultSet rs = select.executeQuery();
            result = rs.next();
        } catch(SQLException e){
            this.unitOfWork.rollbackTransaction(); // if for some reason, the query fails after executeUpdate() this rolls back to state before commit
            throw new DataAccessException("Register SQL nicht erfolgreich", e);
        }

        return result;
    }


    /**
     * checks if user exists, selects user from users table and queries for the username
     * @param username
     * @return User if found, null if not found
     */
    public User userExists(String username) {
        User user = null;
        try (PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT * FROM public.users
                        where username = ?
                """)){
            select.setString(1, username);
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                user = new User(rs.getString(1), rs.getString(2));
            }
        } catch(SQLException e) {
            throw new DataAccessException("User exists SQL nicht erfolgreich", e);
        }
        return user;
    }


    /**
     * checks in db if a user is logged in, select from currently_logged_in table to determine
     * @param username
     * @return true if user is logged in, false if not
     */
    public Boolean userLoggedIn(String username) {
        try (PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT * FROM public.currently_logged_in
                        where username = ?
                """)){
            select.setString(1, username);
            ResultSet rs = select.executeQuery();
            return rs.next();

        } catch(SQLException e) {
            throw new DataAccessException("userLoggedIn SQL nicht erfolgreich", e);
        }
    }
}
