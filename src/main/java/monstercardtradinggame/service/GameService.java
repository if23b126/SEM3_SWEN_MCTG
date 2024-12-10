package monstercardtradinggame.service;

import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.GameRepository;
import monstercardtradinggame.persistence.repository.GameRepositoryImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Base64;

public class GameService extends AbstractService {

    //private GameRepository userRepository;

    public GameService() {
        //userRepository = new GameRepositoryImpl(new UnitOfWork());
    }
}