package httpserver.server;

public interface RestController {
    Response handleRequest(Request request);
}
