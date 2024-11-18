package monstercardtradinggame.service;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.User;
/*import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.WeatherRepository;
import monstercardtradinggame.persistence.repository.WeatherRepositoryImpl;*/
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.Collections;

public class UserService extends AbstractService {

    public UserService() {}

    // POST /user/login
    public Response loginUser(Request request) {
        User user = new User("MonsterSmasher", "securePW");
        User loginTry = null;
        try {
            loginTry = this.getObjectMapper().readValue(request.getBody(), User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (user.getUsername().equals(loginTry.getUsername()) && user.getPassword().equals(loginTry.getPassword())) {
            return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "login successful");
        }

        return new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "login failed");
    }

    // POST /user/logout
    public Response logoutUser(Request request) {
        return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "logout successful");
    }

    // POST /user/register
    public Response registerUser(Request request) {
        User register = null;
        try {
            register = this.getObjectMapper().readValue(request.getBody(), User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new Response(HttpStatus.CREATED, ContentType.PLAIN_TEXT, "User Created");
    }
}
