package monstercardtradinggame.persistence.repository;


import monstercardtradinggame.model.User;

public interface UserRepository {

    String login(String username, String password);
    Boolean logout(String token);
    Boolean register(String username, String password);
    Boolean checkIfUserIsLoggedIn(String token);
    int getUserIDFromToken(String token);
    Boolean checkIfUserIsAdmin(String token);
    String getUsernameFromToken(String token);
    int getIDFromUsername(String username);
    User.UserInfo getUserData(int userID);
    Boolean updateUserData(User.UserInfo user, int userID);
    String getUsernameFromID(int userID);
    Boolean updateStats(int userID, int stat);
}
