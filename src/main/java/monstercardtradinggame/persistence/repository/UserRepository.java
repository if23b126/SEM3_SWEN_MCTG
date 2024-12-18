package monstercardtradinggame.persistence.repository;


public interface UserRepository {

    String login(String username, String password);
    Boolean logout(String token);
    Boolean register(String username, String password);
    Boolean checkIfUserIsLoggedIn(String token);
}
