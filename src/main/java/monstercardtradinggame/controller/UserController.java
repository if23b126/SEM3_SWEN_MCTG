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


    /**
     * maps the different request to the methods they need to be processed, determines this by the path of the URL
     * @param request
     * @return new Response()
     */
    @Override
    public Response handleRequest(Request request) {
        if (request.getMethod() == Method.POST && request.getPathname().equals("/sessions")) {
            return this.userService.loginUser(request);
        } else if (request.getMethod() == Method.POST && request.getPathname().equals("/users/logout")) {
            return this.userService.logoutUser(request);
        } else if (request.getMethod() == Method.POST && request.getPathname().equals("/users")) {
            return this.userService.registerUser(request);
        } else if (request.getMethod() == Method.GET && request.getPathname().equals("/users")) {
            return this.userService.getUserInfo(request);
        } else if (request.getMethod() == Method.PUT && request.getPathname().equals("/users")) {
            return this.userService.setUserInfo(request);
        } else {
            return new Response(HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "[]");
        }
    }
}
