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

public class UserServiceImpl extends AbstractService implements UserService {

    private UserRepository userRepository;
    private Base64.Encoder encoder;

    public UserServiceImpl() {
        userRepository = new UserRepositoryImpl(UnitOfWork.getInstance());
        encoder = Base64.getEncoder();
    }


    /**
     * Post Request to login an already existing user, @param is the request with a JSON body, containing username and password
     * @param request
     * @return
     */
    @Override
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
            return new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Invalid username/password provided");
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
    @Override
    public Response logoutUser(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            User user = null;
            try {
                user = this.getObjectMapper().readValue(request.getBody(), User.class); // mapper from body to User
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            Boolean result = userRepository.logout(token);

            if (result) {
                response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "User successfully logged out");
            } else {
                response = new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.PLAIN_TEXT, "Something went wrong");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }


    /**
     * Post request to register a new User, @param is the request with a JSON body, containing new username and password
     * @param request
     * @return new Response()
     */
    @Override
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


    @Override
    public Response getUserInfo(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            String[] username = request.getPathname().split("/");
            User.UserInfo user = null;
            String json = null;
            if(username[username.length - 1].equals(userRepository.getUsernameFromToken(token)) || userRepository.checkIfUserIsAdmin(token)) {
                user = userRepository.getUserData(userRepository.getIDFromUsername(username[username.length - 1]));

                try{
                    json = this.getObjectMapper().writeValueAsString(user);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                response = new Response(HttpStatus.OK, ContentType.JSON, json);
            } else if(userRepository.checkIfUserIsAdmin(token)) {
                response = new Response(HttpStatus.FORBIDDEN, ContentType.PLAIN_TEXT, "User is no Admin");
            } else {
                response = new Response(HttpStatus.NOT_FOUND, ContentType.PLAIN_TEXT, "User not found");
            }

        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    @Override
    public Response setUserInfo(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            String[] username = request.getPathname().split("/");
            if(username[username.length - 1].equals(userRepository.getUsernameFromToken(token))) {
                User.UserInfo userInfo = null;
                try {
                    userInfo = this.getObjectMapper().readValue(request.getBody(), User.UserInfo.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                if(userRepository.updateUserData(userInfo, userRepository.getUserIDFromToken(token))) {
                    response = new Response(HttpStatus.OK);
                } else {
                    response = new Response(HttpStatus.CONFLICT);
                }
            } else {
                response = new Response(HttpStatus.FORBIDDEN, ContentType.PLAIN_TEXT, "The Token and the User don't match");
            }

        } else {
            response = new Response(HttpStatus.UNAUTHORIZED);
        }

        return response;
    }
}
