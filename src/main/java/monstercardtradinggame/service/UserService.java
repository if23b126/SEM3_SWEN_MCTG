package monstercardtradinggame.service;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Base64;

public class UserService extends AbstractService {

    private UserRepository userRepository;
    private Base64.Encoder encoder;

    public UserService() {
        userRepository = new UserRepositoryImpl(new UnitOfWork());
        encoder = Base64.getEncoder();
    }


    /**
     * Post Request to login an already existing user, @param is the request with a JSON body, containing username and password
     * @param request
     * @return
     */
    public Response loginUser(Request request) {
        //User user = new User("MonsterSmasher", "securePW");
        User loginTry = null;
        try {
            loginTry = this.getObjectMapper().readValue(request.getBody(), User.class); // mapper from body to User
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String password = encoder.encodeToString(loginTry.getPassword().getBytes()); // encodes pw to base64
        String token = userRepository.login(loginTry.getUsername().toLowerCase(), password); // persists login in db

        if (token == null) {
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Login failed");
        } else
        {
            return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, token);
        }
    }


    /**
     * Post Request to logout a user,  @param is the request with a JSON as body, containing the token
     * @param request
     * @return new Response()
     */
    public Response logoutUser(Request request) {
        User user = null;
        try {
            user = this.getObjectMapper().readValue(request.getBody(), User.class); // mapper from body to User
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Boolean result = userRepository.logout(user.getToken());

        if (result) {
            return new Response(HttpStatus.OK);
        } else {
            return new Response(HttpStatus.UNAUTHORIZED);
        }
    }


    /**
     * Post request to register a new User, @param is the request with a JSON body, containing new username and password
     * @param request
     * @return new Response()
     */
    public Response registerUser(Request request) {
        User register = null;
        try {
            register = this.getObjectMapper().readValue(request.getBody(), User.class); // mapper from body to User
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String password = encoder.encodeToString(register.getPassword().getBytes()); // encodes pw to base64
        Boolean result = userRepository.register(register.getUsername().toLowerCase(), password); // persists user in db

        if (result) {
            return new Response(HttpStatus.CREATED);
        } else {
            return new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "User already exists");
        }
    }
}
