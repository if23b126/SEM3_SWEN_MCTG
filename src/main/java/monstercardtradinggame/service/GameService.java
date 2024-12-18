package monstercardtradinggame.service;

import com.fasterxml.jackson.core.type.TypeReference;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.Card;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.GameRepository;
import monstercardtradinggame.persistence.repository.GameRepositoryImpl;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Base64;
import java.util.Collection;

public class GameService extends AbstractService {

    private GameRepository gameRepository;
    private UserRepository userRepository;

    public GameService() {
        this.gameRepository = new GameRepositoryImpl(new UnitOfWork());
        this.userRepository = new UserRepositoryImpl(new UnitOfWork());
    }

    public Response createPackage(Request request) {
        if(userRepository.checkIfUserIsLoggedIn(request.getHeaderMap().getHeader("Authorization").substring(7))) {
            Collection<Card> cards;
            try {
                cards = this.getObjectMapper().readValue(request.getBody(), new TypeReference<Collection<Card>>() {}); // mapper from body to User
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return new Response(HttpStatus.OK);
    }

    public Response buyPackage(Request request) {
        return new Response(HttpStatus.OK);
    }

    public Response getCards(Request request) {
        return new Response(HttpStatus.OK);
    }

    public Response getDeck(Request request, Boolean asPlainString) {
        if (asPlainString) {
            return new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "plain");
        }
        return new Response(HttpStatus.OK, ContentType.JSON, "json");
    }

    public Response putDeck(Request request) {
        return new Response(HttpStatus.OK);
    }

    public Response getStats(Request request) {
        return new Response(HttpStatus.OK);
    }

    public Response getScoreboards(Request request) {
        return new Response(HttpStatus.OK);
    }
}