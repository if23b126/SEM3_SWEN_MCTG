package monstercardtradinggame.persistence.repository;


import monstercardtradinggame.model.Stat;
import monstercardtradinggame.model.User;

import java.util.List;

public interface UserRepository {

    String login(String username, String password);
    boolean logout(String token);
    boolean register(String username, String password);
    boolean checkIfUserIsLoggedIn(String token);
    int getUserIDFromToken(String token);
    boolean checkIfUserIsAdmin(String token);
    String getUsernameFromToken(String token);
    int getIDFromUsername(String username);
    User.UserInfo getUserData(int userID);
    boolean updateUserData(User.UserInfo user, int userID);
    String getUsernameFromID(int userID);
    boolean updateStats(int initiatorID, int opponentID, int stat);
    Stat getStats(int userID);
    List<Stat> getScoreboard();
    int getOwnerFromCard(String cardID);
    User userExists(String username);
}
