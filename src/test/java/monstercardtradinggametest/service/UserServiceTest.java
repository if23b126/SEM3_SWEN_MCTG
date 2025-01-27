package monstercardtradinggametest.service;

import monstercardtradinggame.model.Token;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import monstercardtradinggametest.persistence.*;

import org.junit.jupiter.api.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserServiceTest {
    static UserRepository userRepository;
    static UserRespositoryTest userRespositoryTest;
    static Base64.Encoder encoder;

    @BeforeAll
    static void beforeAll() {
        encoder = Base64.getEncoder();
    }


    @Test
    public void createUserTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:register_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);
        userRespositoryTest = new UserRepositoryTestImpl(unitOfWork);

        String username = "test";
        String password = encoder.encodeToString("test".getBytes());
        System.out.println(password);
        userRepository.register(username, password);

        User user = userRespositoryTest.getUsers();

        assertEquals("test", user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(20, user.getCoins());
        assertFalse(user.isAdmin());
        assertEquals(0, user.getWins());
        assertEquals(0, user.getLosses());
        assertEquals(0, user.getTies());
        assertEquals(1000, user.getElo());
    }

    @Test
    public void testTokenCreation() {
        String username = "test";
        Token tokenObj = new Token();
        String token = tokenObj.generateNewToken(username);

        assertEquals("test-mtcgToken", token);
    }

    @Test
    public void loginUserTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:login_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);
        userRespositoryTest = new UserRepositoryTestImpl(unitOfWork);

        String username = "test";
        String password = encoder.encodeToString("test".getBytes());
        userRepository.login(username, password);

        String[] result = userRespositoryTest.getCurrentlyLoggedInUser();
        assertEquals("test", result[0]);
        assertEquals("test-mtcgToken", result[1]);
    }
}