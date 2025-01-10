package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.model.Stat;
import monstercardtradinggame.model.Token;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public int getUserIDFromToken(String token) {
        int result = -1;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
            SELECT id
            FROM public.currently_logged_in cli
            LEFT JOIN public.users u on cli.username = u.username AND
            token=?
        """)){

            select.setString(1, token);
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                if(rs.getInt(1) != 0) {
                    result = rs.getInt(1);
                }
            }

        }catch (SQLException e){
            throw new DataAccessException("Get username From Token SQL nicht erfolgreich", e);
        }

        return result;
    }


    @Override
    public Boolean checkIfUserIsAdmin(String token) {
        Boolean result = false;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
            SELECT isAdmin
            FROM public.currently_logged_in cli
            LEFT JOIN public.users u on cli.username = u.username AND
            token=?
        """)){

            select.setString(1, token);
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                if(rs.getBoolean(1)) {
                    result = rs.getBoolean(1);
                }
            }

        }catch (SQLException e){
            throw new DataAccessException("Get username From Token SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public String getUsernameFromToken(String token) {
        String result = null;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT username
                FROM public.currently_logged_in
                WHERE token=?
            """)) {
            select.setString(1, token);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                result = rs.getString(1);
            }
        } catch (SQLException e){
            throw new DataAccessException("Get username From Token SQL nicht erfolgreich", e);
        }
        return result;
    }

    @Override
    public int getIDFromUsername(String username) {
        int result = -1;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT id FROM public.users
                WHERE username=?
            """)) {
            select.setString(1, username);
            ResultSet rs = select.executeQuery();
            rs.next();
            result = rs.getInt(1);
        } catch(SQLException e){
            throw new DataAccessException("Get username From Token SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public User.UserInfo getUserData(int userID) {
        User.UserInfo result = null;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT name, bio, image
                FROM public.users
                WHERE id=?
            """)) {
            select.setInt(1, userID);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                result = User.UserInfo.builder()
                        .name(rs.getString(1))
                        .bio(rs.getString(2))
                        .image(rs.getString(3))
                        .build();
            }
        } catch(SQLException e){
            throw new DataAccessException("Get userdata From User SQL nicht erfolgreich", e);
        }
        return result;
    }

    @Override
    public Boolean updateUserData(User.UserInfo user, int userID) {
        Boolean result = false;
        try(PreparedStatement update = this.unitOfWork.prepareStatement("""
                UPDATE public.users
                SET name=?, bio=?, image=?
                WHERE id=?
            """)) {
            update.setString(1, user.getName());
            update.setString(2, user.getBio());
            update.setString(3, user.getImage());
            update.setInt(4, userID);

            update.executeUpdate();
            result = true;

            this.unitOfWork.commitTransaction();
        } catch(SQLException e){
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("Update UserData From User SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public String getUsernameFromID(int userID) {
        String result = null;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT username
                FROM public.users
                WHERE id=?
            """)) {
            select.setInt(1, userID);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                if(rs.getString(1) != null) {
                    result = rs.getString(1);
                }
            }
        } catch(SQLException e){
            throw new DataAccessException("Get username From User SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public Boolean updateStats(int initiatorID, int opponentID, int stat) {
        Boolean result = false;
        try(PreparedStatement update = this.unitOfWork.prepareStatement("""
                UPDATE public.users
                SET wins=?, losses=?, ties=?, elo=?
                WHERE id=?;
            """)) {

            Stat initiator = getStats(initiatorID);
            Stat opponent = getStats(opponentID);

            int[] newElos = stat != 2 ? calculateElo(initiator.getElo(), opponent.getElo(), stat) : new int[]{initiator.getElo(), opponent.getElo()};

            if(stat == 1) {
                update.setInt(1, initiator.getWins() + 1);
                update.setInt(2, initiator.getLosses());
                update.setInt(3, initiator.getTies());
                update.setInt(4, newElos[0]);
                update.setInt(5, initiatorID);
                update.executeUpdate();

                update.setInt(1, opponent.getWins());
                update.setInt(2, opponent.getLosses() + 1);
                update.setInt(3, opponent.getTies());
                update.setInt(4, newElos[1]);
                update.setInt(5, opponentID);
                update.executeUpdate();
            } else if (stat == 0) {
                update.setInt(1, initiator.getWins());
                update.setInt(2, initiator.getLosses() + 1);
                update.setInt(3, initiator.getTies());
                update.setInt(4, newElos[0]);
                update.setInt(5, initiatorID);
                update.executeUpdate();

                update.setInt(1, opponent.getWins() + 1);
                update.setInt(2, opponent.getLosses());
                update.setInt(3, opponent.getTies());
                update.setInt(4, newElos[1]);
                update.setInt(5, opponentID);
                update.executeUpdate();
            } else if (stat == 2) {
                update.setInt(1, initiator.getWins());
                update.setInt(2, initiator.getLosses());
                update.setInt(3, initiator.getTies() + 1);
                update.setInt(4, newElos[0]);
                update.setInt(5, initiatorID);
                update.executeUpdate();

                update.setInt(1, opponent.getWins());
                update.setInt(2, opponent.getLosses());
                update.setInt(3, opponent.getTies() + 1);
                update.setInt(4, newElos[1]);
                update.setInt(5, opponentID);
                update.executeUpdate();
            }

            result = true;
            this.unitOfWork.commitTransaction();

        } catch(SQLException e){
            throw new DataAccessException("Update Stats From User SQL nicht erfolgreich", e);
        }

        return result;
    }


    @Override
    public Stat getStats(int userID) {
        Stat result = null;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT name, wins, losses, ties, elo FROM public.users
                WHERE id=?
            """)) {
            select.setInt(1, userID);
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                result = Stat.builder()
                        .name(rs.getString(1))
                        .wins(rs.getInt(2))
                        .losses(rs.getInt(3))
                        .ties(rs.getInt(4))
                        .elo(rs.getInt(5))
                        .build();
            }
        } catch(SQLException e){
            throw new DataAccessException("Get Stats From User SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public List<Stat> getScoreboard() {
        List<Stat> result = new ArrayList<>();
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT name, wins, losses, ties, elo
                FROM public.users
                ORDER BY elo DESC
            """)) {
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                result.add(Stat.builder()
                        .name(rs.getString(1))
                        .wins(rs.getInt(2))
                        .losses(rs.getInt(3))
                        .ties(rs.getInt(4))
                        .elo(rs.getInt(5))
                        .build());
            }
        } catch(SQLException e){
            throw new DataAccessException("Get Stats From User SQL nicht erfolgreich", e);
        }

        return result;
    }

    private int[] calculateElo(int initiator, int opponent, int stat) {
        int[] result = new int[2];
        double initiatorProbability = 1.0 / (1 + Math.pow(10, (initiator - opponent) / 400.0));
        double opponentProbability = 1.0 / (1 + Math.pow(10, (opponent - initiator) / 400.0));

        result[0] = (int)(initiator + 12 * (stat - initiatorProbability));
        result[1] = (int)(opponent + 12 * ((1 - stat) - opponentProbability));

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
                SELECT username, password FROM public.users
                        where username = ?
                """)){
            select.setString(1, username);
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                user = User.builder()
                        .username(rs.getString(1))
                        .password(rs.getString(2))
                        .build();
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
