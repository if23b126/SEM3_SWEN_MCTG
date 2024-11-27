import httpserver.server.Server;
import httpserver.utils.Router;
import monstercardtradinggame.controller.EchoController;
import monstercardtradinggame.controller.UserController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server(10001, configureRouter());
        try{
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates a new Route for the different paths of the URL
     * @return new Router()
     */
    private static Router configureRouter() {
        Router router = new Router();
        router.addService("/echo", new EchoController());
        router.addService("/users", new UserController());
        router.addService("/sessions", new UserController());

        return router;
    }
}
