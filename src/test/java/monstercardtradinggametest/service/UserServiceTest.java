package monstercardtradinggametest.service;

import monstercardtradinggame.model.Token;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import monstercardtradinggametest.persistence.*;

import org.junit.jupiter.api.*;
import org.postgresql.core.ConnectionFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );
    static UserRepository userRepository;
    static UserRespositoryTest userRespositoryTest;
    static PrepareUser prepareUser;
    static CleanUp cleanUp;
    static Base64.Encoder encoder;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        encoder = Base64.getEncoder();
    }

    @AfterAll
    static void tearDown() {
        cleanUp.deleteTableUsers();
        cleanUp.deleteTableLogin();
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
            connection.setAutoCommit(false);
        } catch(SQLException e) {
            throw new DataAccessException("Datanbankverbindung fehlgeschlagen", e);
        }
        UnitOfWork unitOfWork = new UnitOfWork(connection);
        userRepository = new UserRepositoryImpl(unitOfWork);
        userRespositoryTest = new UserRepositoryTestImpl(unitOfWork);
        prepareUser = new PrepareUserImpl(unitOfWork);
        cleanUp = new CleanUpImpl(unitOfWork);
    }

    @Test
    public void createUserTest() {
        prepareUser.prepareTableForCreateUserTest();

        String username = "test";
        String password = encoder.encodeToString("test".getBytes());
        userRepository.register(username, password);

        User user = userRespositoryTest.getUsers();

        assertEquals("test", user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(20, user.getCoins());
        assertEquals(false, user.isAdmin());
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
        prepareUser.prepareTableForCreateUserTest();
        prepareUser.prepareTableForLoginUserTest();

        String username = "test";
        String password = encoder.encodeToString("test".getBytes());
        userRepository.login(username, password);

        String[] result = userRespositoryTest.getCurrentlyLoggedInUser();
        assertEquals("test", result[0]);
        assertEquals("test-mtcgToken", result[1]);
    }
}