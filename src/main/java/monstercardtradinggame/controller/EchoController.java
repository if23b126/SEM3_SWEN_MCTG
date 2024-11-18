package monstercardtradinggame.controller;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import httpserver.server.RestController;

public class EchoController implements RestController {
    @Override
    public Response handleRequest(Request request) {
        return new Response(HttpStatus.OK,
                            ContentType.PLAIN_TEXT,
                            "Echo-" + request.getBody());
    }
}
