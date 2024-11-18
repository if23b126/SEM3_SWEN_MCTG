package monstercardtradinggame.persistence.repository;


import java.util.Collection;

public interface UserRepository {

    String Login(String username, String password);
    Boolean Logout(String token);
    Boolean Register(String username, String password);

}
