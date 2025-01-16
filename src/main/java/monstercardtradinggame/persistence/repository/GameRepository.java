package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.model.Card;
import monstercardtradinggame.model.Trading;

import java.util.Collection;
import java.util.List;

public interface GameRepository {
    int createPackage(Collection<Card> cards, int userID);
    int buyPackage(int userID);
    Collection<Card> getCards(int userID);
    Collection<Card> getDeck(int userID);
    Boolean createDeck(int userID, String[] cards);
    List<String> battle(int userID);
    List<Trading> getTradings();
    Boolean createTrading(Trading trading);
    Boolean acceptTrading(String offer, String acceptance, int offer_userID, int acceptance_userID);
    Boolean deleteTrading(String trading);
    String getCardFromTradingID(String tradingID);
    Boolean checkIfTradingExists(String tradingID);
}
