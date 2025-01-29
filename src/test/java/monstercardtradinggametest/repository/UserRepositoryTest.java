package monstercardtradinggametest.repository;

import monstercardtradinggame.model.Stat;
import monstercardtradinggame.model.Token;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import monstercardtradinggametest.persistence.*;

import org.junit.jupiter.api.*;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {
    static UserRepository userRepository;
    static UserRespositoryTest userRespositoryTest;
    static Base64.Encoder encoder;

    @BeforeAll
    static void beforeAll() {
        encoder = Base64.getEncoder();
    }


    @Test
    public void createUserTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/register_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);
        userRespositoryTest = new UserRepositoryTestImpl(unitOfWork);

        String username = "test";
        String password = encoder.encodeToString("test".getBytes());
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
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/login_user.sql'";
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

    //check for user status and get id/username from token/id
    @Test
    public void checkIfUserIsLoggedInTrueTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        Boolean loggedIn = userRepository.checkIfUserIsLoggedIn("test-mtcgToken");

        assertTrue(loggedIn);
    }

    @Test
    public void checkIfUserIsLoggedInFalseTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        Boolean loggedIn = userRepository.checkIfUserIsLoggedIn("christian-mtcgToken");

        assertFalse(loggedIn);
    }

    @Test
    public void getUserIDFromTokenTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        int userID = userRepository.getUserIDFromToken("test-mtcgToken");

        assertEquals(1, userID);
    }

    @Test
    public void checkIfUserIsAdminTrueTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        boolean admin = userRepository.checkIfUserIsAdmin("admin-mtcgToken");

        assertTrue(admin);
    }

    @Test
    public void checkIfUserIsAdminFalseTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        boolean admin = userRepository.checkIfUserIsAdmin("test-mtcgToken");

        assertFalse(admin);
    }

    @Test
    public void getUsernameFromTokenTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        String username = userRepository.getUsernameFromToken("test-mtcgToken");

        assertEquals("test", username);
    }

    @Test
    public void getUserIDFromUsernameTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        int userID = userRepository.getIDFromUsername("test");

        assertEquals(1, userID);
    }

    @Test
    public void getUsernameFromIDTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        String username = userRepository.getUsernameFromID(1);

        assertEquals("test", username);
    }

    @Test
    public void getUserStatsTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/get_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        Stat stats = userRepository.getStats(1);

        assertEquals("testName1", stats.getName());
        assertEquals(5, stats.getWins());
        assertEquals(10, stats.getLosses());
        assertEquals(3, stats.getTies());
        assertEquals(897, stats.getElo());
    }

    // the following tests are for elo calculation
    @Test
    public void updateUserStatsForUserOneWinningTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/update_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        userRepository.updateStats(1, 2, 1);
        Stat test1 = userRepository.getStats(1);
        Stat test2 = userRepository.getStats(2);

        //assert everything right for test1
        assertEquals("test1", test1.getName());
        assertEquals(1, test1.getWins());
        assertEquals(0, test1.getLosses());
        assertEquals(0, test1.getTies());
        assertEquals(1006, test1.getElo());

        //assert everything right for test2
        assertEquals("test2", test2.getName());
        assertEquals(0, test2.getWins());
        assertEquals(1, test2.getLosses());
        assertEquals(0, test2.getTies());
        assertEquals(994, test2.getElo());
    }

    @Test
    public void updateUserStatsForUserTwoWinningTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/update_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        userRepository.updateStats(1, 2, 0);
        Stat test1 = userRepository.getStats(1);
        Stat test2 = userRepository.getStats(2);

        //assert everything right for test1
        assertEquals("test1", test1.getName());
        assertEquals(0, test1.getWins());
        assertEquals(1, test1.getLosses());
        assertEquals(0, test1.getTies());
        assertEquals(994, test1.getElo());

        //assert everything right for test2
        assertEquals("test2", test2.getName());
        assertEquals(1, test2.getWins());
        assertEquals(0, test2.getLosses());
        assertEquals(0, test2.getTies());
        assertEquals(1006, test2.getElo());
    }

    @Test
    public void updateUserStatsForTieTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/update_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        userRepository.updateStats(1, 2, 2);
        Stat test1 = userRepository.getStats(1);
        Stat test2 = userRepository.getStats(2);

        //assert everything right for test1
        assertEquals("test1", test1.getName());
        assertEquals(0, test1.getWins());
        assertEquals(0, test1.getLosses());
        assertEquals(1, test1.getTies());
        assertEquals(1000, test1.getElo());

        //assert everything right for test2
        assertEquals("test2", test2.getName());
        assertEquals(0, test2.getWins());
        assertEquals(0, test2.getLosses());
        assertEquals(1, test2.getTies());
        assertEquals(1000, test2.getElo());
    }

    @Test
    public void getUserDataTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/get_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        User.UserInfo userInfo = userRepository.getUserData(1);

        assertEquals("testName1", userInfo.getName());
        assertEquals("me playin", userInfo.getBio());
        assertEquals(":-)", userInfo.getImage());
    }

    @Test
    public void getScoreboardTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/get_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        List<Stat> stats = userRepository.getScoreboard();

        assertEquals("testName2", stats.get(0).getName());
        assertEquals(30, stats.get(0).getWins());
        assertEquals(5, stats.get(0).getLosses());
        assertEquals(1, stats.get(0).getTies());
        assertEquals(1900, stats.get(0).getElo());

        assertEquals("testName1", stats.get(1).getName());
        assertEquals(5, stats.get(1).getWins());
        assertEquals(10, stats.get(1).getLosses());
        assertEquals(3, stats.get(1).getTies());
        assertEquals(897, stats.get(1).getElo());
    }

    @Test
    public void updateUserDataTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/get_user_stats.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        User.UserInfo userInfo = User.UserInfo.builder()
                .name("newTestName")
                .bio("I am the destroyer")
                .image("xD")
                .build();

        userRepository.updateUserData(userInfo, 1);
        User.UserInfo test1 = userRepository.getUserData(1);

        assertEquals("newTestName", test1.getName());
        assertEquals("I am the destroyer", test1.getBio());
        assertEquals("xD", test1.getImage());
    }

    @Test
    public void getOwnerFromCardTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:gamePersistence/all_without_trade_battle.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        userRepository = new UserRepositoryImpl(unitOfWork);

        int userID = userRepository.getOwnerFromCard("f3fad0f2-a1af-45df-b80d-2e48825773d9");

        assertEquals(2, userID);
    }
}