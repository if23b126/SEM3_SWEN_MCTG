package monstercardtradinggametest.persistence;

import monstercardtradinggame.model.Stat;
import monstercardtradinggame.model.User;

public interface UserRespositoryTest {
    User getUsers();
    String[] getCurrentlyLoggedInUser();
}
