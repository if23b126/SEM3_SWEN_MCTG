package monstercardtradinggame.persistence.repository;

import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.Card;

import java.util.Collection;

public interface GameRepository {
    Boolean createPackage(Collection<Card> cards, int userID);
    int buyPackage(int userID);
    Collection<Card> getCards(int userID);
    Collection<Card> getDeck(int userID);
    Boolean createDeck(int userID, String[] cards);
}
