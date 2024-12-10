package monstercardtradinggame.controller;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.http.Method;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.RestController;
import monstercardtradinggame.service.GameService;

public class GameController implements RestController {
    private GameService gameService;

    public GameController(){
        this.gameService = new GameService();
    }


    /**
     * maps the different request to the methods they need to be processed, determines this by the path of the URL
     * @param request
     * @return new Response()
     */
    @Override
    public Response handleRequest(Request request) {
        return null;
    }
}