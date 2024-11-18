package monstercardtradinggame.controller;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.RestController;
import monstercardtradinggame.service.UserService;

public class UserController implements RestController {
    private UserService userService;

    public UserController(){
        this.userService = new UserService();
    }

    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST && request.getPathname().equals("/user/login")) {
            return this.userService.loginUser(request);
        } else if (request.getMethod() == Method.POST && request.getPathname().equals("/user/logout")) {
            return this.userService.logoutUser(request);
        } else if (request.getMethod() == Method.POST && request.getPathname().equals("/user/register")) {
            return this.userService.registerUser(request);
        }

        return new Response(HttpStatus.BAD_REQUEST,
                            ContentType.JSON,
                            "[]");
    }
}
