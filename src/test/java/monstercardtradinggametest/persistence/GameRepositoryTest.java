package monstercardtradinggametest.persistence;

import monstercardtradinggame.model.Card;

import java.util.List;

public interface GameRepositoryTest {
    List<Card> getCards();
    int[] getPackages();
    List<String[]> getCardsInPackages();
}
