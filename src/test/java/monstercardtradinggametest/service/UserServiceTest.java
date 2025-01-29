package monstercardtradinggametest.service;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.HeaderMap;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import monstercardtradinggame.service.UserService;
import monstercardtradinggame.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    static UserService userService;

    @BeforeAll
    public static void setUp() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:userPersistence/logged_in_user.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        UserRepository userRepository = new UserRepositoryImpl(unitOfWork);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    public void loginUserInvalidCredentialsTest() {
        Request request = new Request();
        request.setBody("{\"Username\":\"christian\",\"Password\":\"wrong password\"}");
        Response response = userService.loginUser(request);
        assertEquals(401, response.getStatus());
        assertEquals("Invalid username/password provided", response.getContent());
    }

    @Test
    public void loginUserValidCredentialsTest() {
        Request request = new Request();
        request.setBody("{\"Username\":\"christian\",\"Password\":\"test\"}");
        Response response = userService.loginUser(request);
        assertEquals(200, response.getStatus());
        assertEquals("christian-mtcgToken", response.getContent());
    }

    @Test
    public void registerUserSuccessfulTest() {
        Request request = new Request();
        request.setBody("{\"Username\":\"newUser\",\"Password\":\"test\"}");
        Response response = userService.registerUser(request);
        assertEquals(201, response.getStatus());
    }

    @Test
    public void registerUserFailedTest() {
        Request request = new Request();
        request.setBody("{\"Username\":\"christian\",\"Password\":\"test\"}");
        Response response = userService.registerUser(request);
        assertEquals(409, response.getStatus());
        assertEquals("User already exists", response.getContent());
    }

    @Test
    public void getUserInfoAsAdminTest() {
        Request request = new Request();
        request.setPathname("/users/test");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer admin-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = userService.getUserInfo(request);
        assertEquals(200, response.getStatus());
        assertEquals("{\"Name\":null,\"Bio\":null,\"Image\":null}", response.getContent());
    }

    @Test
    public void getUserInfoAsUserTest() {
        Request request = new Request();
        request.setPathname("/users/test");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = userService.getUserInfo(request);
        assertEquals(200, response.getStatus());
        assertEquals("{\"Name\":null,\"Bio\":null,\"Image\":null}", response.getContent());
    }

    @Test
    public void getUserInfoAsWrongUserTest() {
        Request request = new Request();
        request.setPathname("/users/christian");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = userService.getUserInfo(request);
        assertEquals(403, response.getStatus());
        assertEquals("User is no Admin", response.getContent());
    }

    @Test
    public void getUserInfoForNonexistingUserTest() {
        Request request = new Request();
        request.setPathname("/users/nonExistingUser");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = userService.getUserInfo(request);
        assertEquals(404, response.getStatus());
        assertEquals("User not found", response.getContent());
    }

    @Test
    public void getUserInfoMissingTokenTest() {
        Request request = new Request();
        request.setPathname("/users/test");
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = userService.getUserInfo(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void setUserInfoSuccessTest() {
        Request request = new Request();
        request.setPathname("/users/test");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Name\":\"test\",\"Bio\":\"me playin...\",\"Image\":\":-)\"}");
        Response response = userService.setUserInfo(request);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void setUserInfoInvalidBodyTest() {
        Request request = new Request();
        request.setPathname("/users/test");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Name\":\"test\",\"Image\":\":-)\"}");
        Response response = userService.setUserInfo(request);
        assertEquals(409, response.getStatus());
    }

    @Test
    public void setUserInfoMissingTokenTest() {
        Request request = new Request();
        request.setPathname("/users/test");
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        request.setBody("{\"Name\":\"test\",\"Bio\":\"me playin...\",\"Image\":\":-)\"}");
        Response response = userService.setUserInfo(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());;
    }

    @Test
    public void setUserInfoTokenUserNotMatchingTest() {
        Request request = new Request();
        request.setPathname("/users/christian");
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Name\":\"test\",\"Bio\":\"me playin...\",\"Image\":\":-)\"}");
        Response response = userService.setUserInfo(request);
        assertEquals(403, response.getStatus());
        assertEquals("The Token and the User don't match", response.getContent());;
    }
}
