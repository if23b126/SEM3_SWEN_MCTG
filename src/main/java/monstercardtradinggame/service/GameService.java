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

import java.util.Collection;

public class GameService extends AbstractService {

    private GameRepository gameRepository;
    private UserRepository userRepository;

    public GameService() {
        this.gameRepository = new GameRepositoryImpl(new UnitOfWork());
        this.userRepository = new UserRepositoryImpl(new UnitOfWork());
    }

    /**
     * /packages
     * @param request Request sent from server
     * @return new Response()
     */
    public Response createPackage(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization").substring(7);
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Collection<Card> cards;
            try {
                cards = this.getObjectMapper().readValue(request.getBody(), new TypeReference<Collection<Card>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if(userRepository.checkIfUserIsAdmin(token)) {
                if(gameRepository.createPackage(cards, userRepository.getUserIDFromToken(token))) {
                    response = new Response(HttpStatus.CREATED);
                } else {
                    response = new Response(HttpStatus.CONFLICT);
                }
            } else {
                response = new Response(HttpStatus.FORBIDDEN);
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED);
        }
        return response;
    }

    public Response buyPackage(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization").substring(7);
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            switch(gameRepository.buyPackage(userRepository.getUserIDFromToken(token))) {
                case -1:
                    response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "Not enough money");
                    break;
                case 1:
                    response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "No packages available");
                default:
                    response = new Response(HttpStatus.CREATED);
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED);
        }

        return response;
    }

    public Response getCards(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization").substring(7);
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Collection<Card> cards = gameRepository.getCards(userRepository.getUserIDFromToken(token));
            String json = null;
            try{
                json = this.getObjectMapper().writeValueAsString(cards);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            response = new Response(HttpStatus.OK, ContentType.JSON, json);
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED);
        }

        return response;
    }

    public Response getDeck(Request request, Boolean asPlainString) {
        String token = request.getHeaderMap().getHeader("Authorization").substring(7);
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Collection<Card> cards = gameRepository.getDeck(userRepository.getUserIDFromToken(token));
            if (asPlainString) {
                String plain = "";
                int counter = 1;

                for(Card card : cards) {
                    plain += "Card " + counter++ + "\n" + card.toString() + "\n";
                }

                response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, plain);
            } else {
                String json = null;
                try{
                    json = this.getObjectMapper().writeValueAsString(cards);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                response = new Response(HttpStatus.OK, ContentType.JSON, json);
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED);
        }

        return response;
    }

    public Response putDeck(Request request) {
        String token = request.getHeaderMap().getHeader("Authorization").substring(7);
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            String[] cards;
            cards = request.getBody().replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace("\n", "")
                    .replace(" ", "")
                    .split(",");

            if(cards.length != 4) {
                response = new Response(HttpStatus.CONFLICT);
            }
            else if(gameRepository.createDeck(userRepository.getUserIDFromToken(token), cards)) {
                response = new Response(HttpStatus.CREATED);
            } else {
                response = new Response(HttpStatus.CONFLICT);
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED);
        }

        return response;
    }

    public Response getStats(Request request) {
        return new Response(HttpStatus.OK);
    }

    public Response getScoreboards(Request request) {
        return new Response(HttpStatus.OK);
    }
}