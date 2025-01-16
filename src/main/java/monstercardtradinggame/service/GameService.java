package monstercardtradinggame.service;

import com.fasterxml.jackson.core.type.TypeReference;
import httpserver.http.ContentType;
import httpserver.http.HttpStatus;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.Card;
import monstercardtradinggame.model.Stat;
import monstercardtradinggame.model.Trading;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.GameRepository;
import monstercardtradinggame.persistence.repository.GameRepositoryImpl;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Collection;
import java.util.List;

public class GameService extends AbstractService {

    private GameRepository gameRepository;
    private UserRepository userRepository;

    public GameService() {
        this.gameRepository = new GameRepositoryImpl(UnitOfWork.getInstance());
        this.userRepository = new UserRepositoryImpl(UnitOfWork.getInstance());
    }

    /**
     * /packages
     * @param request Request sent from server
     * @return new Response()
     */
    public Response createPackage(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Collection<Card> cards;
            try {
                cards = this.getObjectMapper().readValue(request.getBody(), new TypeReference<Collection<Card>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if(userRepository.checkIfUserIsAdmin(token)) {
                switch(gameRepository.createPackage(cards, userRepository.getUserIDFromToken(token))) {
                    case 0:
                        response = new Response(HttpStatus.CREATED);
                        break;
                    case 1:
                        response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "At least one card in the packages already exists");
                        break;
                    default:
                        response = new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.PLAIN_TEXT, "Something went wrong");
                }
            } else {
                response = new Response(HttpStatus.FORBIDDEN, ContentType.PLAIN_TEXT, "Provided user is not \"admin\"");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "User is not logged in");
        }
        return response;
    }

    public Response buyPackage(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            switch(gameRepository.buyPackage(userRepository.getUserIDFromToken(token))) {
                case -1:
                    response = new Response(HttpStatus.FORBIDDEN, ContentType.PLAIN_TEXT, "Not enough money for buying a card package");
                    break;
                case 1:
                    response = new Response(HttpStatus.NOT_FOUND, ContentType.PLAIN_TEXT, "No card package available for buying");
                    break;
                default:
                    response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "A package has been successfully bought");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response getCards(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Collection<Card> cards = gameRepository.getCards(userRepository.getUserIDFromToken(token));
            if(cards.isEmpty()) {
                String json = null;
                try {
                    json = this.getObjectMapper().writeValueAsString(cards);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                response = new Response(HttpStatus.OK, ContentType.JSON, json);
            } else {
                response = new Response(HttpStatus.NO_CONTENT, ContentType.PLAIN_TEXT, "The request was fine, but the user doesn't have any cards");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response getDeck(Request request, Boolean asPlainString) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Collection<Card> cards = gameRepository.getDeck(userRepository.getUserIDFromToken(token));
            if(cards.isEmpty()) {
                if (asPlainString) {
                    String plain = "";
                    int counter = 1;

                    for (Card card : cards) {
                        plain += "Card " + counter++ + "\n" + card.toString() + "\n";
                    }

                    response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, plain);
                } else {
                    String json = null;
                    try {
                        json = this.getObjectMapper().writeValueAsString(cards);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    response = new Response(HttpStatus.OK, ContentType.JSON, json);
                }
            } else {
                response = new Response(HttpStatus.NO_CONTENT, ContentType.PLAIN_TEXT, "The request was fine, but the deck doesn't have any cards");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response putDeck(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
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
                response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "The provided deck did not include the required amount of cards");
            }
            else if(gameRepository.createDeck(userRepository.getUserIDFromToken(token), cards)) {
                response = new Response(HttpStatus.CREATED, ContentType.PLAIN_TEXT, "The deck has been successfully configured");
            } else {
                response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "At least one of the provided cards does not belong to the user or is not available.");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response getStats(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            int userID = userRepository.getUserIDFromToken(token);
            Stat stats = userRepository.getStats(userID);

            String json = null;
            try{
                json = this.getObjectMapper().writeValueAsString(stats);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            response = new Response(HttpStatus.OK, ContentType.JSON, json);
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response getScoreboards(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            List<Stat> stats = userRepository.getScoreboard();

            String json = null;
            try{
                json = this.getObjectMapper().writeValueAsString(stats);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            response = new Response(HttpStatus.OK, ContentType.JSON, json);
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response battle(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            int userID = userRepository.getUserIDFromToken(token);
            List<String> gameLog = gameRepository.battle(userID);
            if(gameLog.isEmpty()){
                response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "Waiting for Opponent!");
            } else {
                String logString = "";
                for(String line : gameLog) {
                    logString += line + "\n";
                }
                response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, logString);
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response getTradings(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            List<Trading> tradings = gameRepository.getTradings();

            String json = null;
            try{
                json = this.getObjectMapper().writeValueAsString(tradings);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            response = new Response(HttpStatus.OK, ContentType.JSON, json);
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response createTrading(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            Trading trading;
            try {
                trading = this.getObjectMapper().readValue(request.getBody(), Trading.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            if(userRepository.getOwnerFromCard(trading.getCardToTrade()) == userRepository.getUserIDFromToken(token)) {
                if (!gameRepository.checkIfTradingExists(trading.getId())) {
                    response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "A deal with this deal ID already exists.");
                } else if (gameRepository.createTrading(trading)) {
                    response = new Response(HttpStatus.CREATED, ContentType.PLAIN_TEXT, "Trading deal successfully created");
                } else {
                    response = new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.PLAIN_TEXT, "Something went wrong");
                }
            } else {
                response = new Response(HttpStatus.FORBIDDEN, ContentType.PLAIN_TEXT, "The deal contains a card that is not owned by the user or locked in the deck.");
            }

        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response acceptTrading(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            String offer = gameRepository.getCardFromTradingID(request.getPathname().substring(10));
            String acceptance = request.getBody().replace("\"", "");

            if (userRepository.getUserIDFromToken(token) != userRepository.getOwnerFromCard(acceptance)) {
                response = new Response(HttpStatus.FORBIDDEN, ContentType.PLAIN_TEXT, "The offered card is not owned by the user.");
            } else if (!gameRepository.checkIfTradingExists(offer)) {
                response = new Response(HttpStatus.NOT_FOUND, ContentType.PLAIN_TEXT, "The provided deal ID was not found.");
            } else if(gameRepository.acceptTrading(offer, acceptance, userRepository.getOwnerFromCard(offer), userRepository.getOwnerFromCard(acceptance))) {
                response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "Trading deal successfully executed.");
            } else {
                response = new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.PLAIN_TEXT, "Something went wrong");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }

    public Response deleteTrading(Request request) {
        String token = null;
        if(request.getHeaderMap().getHeader("Authorization") != null) {
            token = request.getHeaderMap().getHeader("Authorization").substring(7);
        }
        Response response;
        if(userRepository.checkIfUserIsLoggedIn(token)) {
            String trading = gameRepository.getCardFromTradingID(request.getPathname().substring(10));

            if (userRepository.getUserIDFromToken(token) != userRepository.getOwnerFromCard(trading)) {
                response = new Response(HttpStatus.CONFLICT, ContentType.PLAIN_TEXT, "The deal contains a card that is not owned by the user.");
            } else if (!gameRepository.checkIfTradingExists(trading)) {
                response = new Response(HttpStatus.NOT_FOUND, ContentType.PLAIN_TEXT, "The provided deal ID was not found.");
            }else if(gameRepository.deleteTrading(trading)) {
                response = new Response(HttpStatus.OK, ContentType.PLAIN_TEXT, "Trading deal successfully deleted.");
            } else {
                response = new Response(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.PLAIN_TEXT, "Something went wrong");
            }
        } else {
            response = new Response(HttpStatus.UNAUTHORIZED, ContentType.PLAIN_TEXT, "Access token is missing or invalid");
        }

        return response;
    }
}