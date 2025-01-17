package monstercardtradinggame.service;

import httpserver.server.Request;
import httpserver.server.Response;

public interface UserService {
    Response loginUser(Request request);
    Response logoutUser(Request request);
    Response registerUser(Request request);
    Response getUserInfo(Request request);
    Response setUserInfo(Request request);
}
