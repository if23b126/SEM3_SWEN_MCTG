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
        if (request.getMethod() == Method.POST && request.getPathname().equals("/packages")) {
            return this.gameService.createPackage(request);
        } else if (request.getMethod() == Method.POST && request.getPathname().equals("/transactions/packages")) {
            return this.gameService.buyPackage(request);
        } else if(request.getMethod() == Method.GET && request.getPathname().equals("/cards")) {
            return this.gameService.getCards(request);
        } else if (request.getMethod() == Method.GET && request.getPathname().equals("/deck")) {
            if(request.getParams() != null) {
                return this.gameService.getDeck(request, true);
            } else {
                return this.gameService.getDeck(request, false);
            }
        } else if(request.getMethod() == Method.PUT && request.getPathname().equals("/deck")) {
            return this.gameService.putDeck(request);
        } else if(request.getMethod() == Method.GET && request.getPathname().equals("/stats")) {
            return this.gameService.getStats(request);
        } else if(request.getMethod() == Method.GET && request.getPathname().equals("/scoreboards")) {
            return this.gameService.getScoreboards(request);
        } else {
            return new Response(HttpStatus.BAD_REQUEST,
                    ContentType.JSON,
                    "[]");
        }
    }
}