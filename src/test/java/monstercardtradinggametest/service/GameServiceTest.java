package monstercardtradinggametest.service;

import monstercardtradinggame.model.Card;
import monstercardtradinggame.model.Trading;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.GameRepository;
import monstercardtradinggame.persistence.repository.GameRepositoryImpl;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import monstercardtradinggametest.persistence.GameRepositoryTest;
import monstercardtradinggametest.persistence.GameRepositoryTestImpl;
import monstercardtradinggametest.persistence.UserRepositoryTestImpl;
import monstercardtradinggametest.persistence.UserRespositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    static GameRepository gameRepository;
    static UserRepository userRepository;
    static GameRepositoryTest gameRepositoryTest;
    static UserRespositoryTest userRespositoryTest;
    static UnitOfWork unitOfWork;

    @BeforeEach
    public void setUp() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:gamePersistence/all_without_trade_battle.sql'";
        unitOfWork = new UnitOfWork(jdbcUrl);
        gameRepository = new GameRepositoryImpl(unitOfWork);
    }

    @Test
    public void createPackageTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:gamePersistence/create_package.sql'";
        unitOfWork = new UnitOfWork(jdbcUrl);
        gameRepository = new GameRepositoryImpl(unitOfWork);
        gameRepositoryTest = new GameRepositoryTestImpl(unitOfWork);


        List<Card> cards = new ArrayList<>();

        cards.add(Card.builder()
                .id("644808c2-f87a-4600-b313-122b02322fd5")
                .name("WaterGoblin")
                .damage(9)
                .build());
        cards.add(Card.builder()
                .id("91a6471b-1426-43f6-ad65-6fc473e16f9f")
                .name("WaterSpell")
                .damage(21)
                .build());
        cards.add(Card.builder()
                .id("dcd93250-25a7-4dca-85da-cad2789f7198")
                .name("FireSpell")
                .damage(23)
                .build());
        cards.add(Card.builder()
                .id("4a2757d6-b1c3-47ac-b9a3-91deab093531")
                .name("Dragon")
                .damage(55)
                .build());
        cards.add(Card.builder()
                .id("4ec8b269-0dfa-4f97-809a-2c63fe2a0025")
                .name("Ork")
                .damage(56)
                .build());

        gameRepository.createPackage(cards, 1);

        int[] packages = gameRepositoryTest.getPackages();
        List<Card> outCards = gameRepositoryTest.getCards();
        List<String[]> cardsInPackages = gameRepositoryTest.getCardsInPackages();

        assertEquals(1, packages[0]);
        assertEquals(1, packages[1]);
        assertEquals(0, packages[2]);

        assertEquals(cards.get(0).getId(), outCards.get(0).getId());
        assertEquals(cards.get(0).getName(), outCards.get(0).getName());
        assertEquals(cards.get(0).getDamage(), outCards.get(0).getDamage());
        assertEquals("water", outCards.get(0).getSpecialty());
        assertEquals("monster", outCards.get(0).getType());
        assertEquals(0, outCards.get(0).getOwnedBy());

        assertEquals(cards.get(1).getId(), outCards.get(1).getId());
        assertEquals(cards.get(1).getName(), outCards.get(1).getName());
        assertEquals(cards.get(1).getDamage(), outCards.get(1).getDamage());
        assertEquals("water", outCards.get(1).getSpecialty());
        assertEquals("spell", outCards.get(1).getType());
        assertEquals(0, outCards.get(1).getOwnedBy());

        assertEquals(cards.get(2).getId(), outCards.get(2).getId());
        assertEquals(cards.get(2).getName(), outCards.get(2).getName());
        assertEquals(cards.get(2).getDamage(), outCards.get(2).getDamage());
        assertEquals("fire", outCards.get(2).getSpecialty());
        assertEquals("spell", outCards.get(2).getType());
        assertEquals(0, outCards.get(2).getOwnedBy());

        assertEquals(cards.get(3).getId(), outCards.get(3).getId());
        assertEquals(cards.get(3).getName(), outCards.get(3).getName());
        assertEquals(cards.get(3).getDamage(), outCards.get(3).getDamage());
        assertEquals("fire", outCards.get(3).getSpecialty());
        assertEquals("monster", outCards.get(3).getType());
        assertEquals(0, outCards.get(3).getOwnedBy());

        assertEquals(cards.get(4).getId(), outCards.get(4).getId());
        assertEquals(cards.get(4).getName(), outCards.get(4).getName());
        assertEquals(cards.get(4).getDamage(), outCards.get(4).getDamage());
        assertEquals("normal", outCards.get(4).getSpecialty());
        assertEquals("monster", outCards.get(4).getType());
        assertEquals(0, outCards.get(4).getOwnedBy());

        for(int i = 0; i < cardsInPackages.size(); i++) {
            assertEquals(cards.get(i).getId(), cardsInPackages.get(i)[0]);
            assertEquals(String.valueOf(1), cardsInPackages.get(i)[1]);
        }
    }

    @Test
    public void buyPackageTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:gamePersistence/buy_package.sql'";
        unitOfWork = new UnitOfWork(jdbcUrl);
        gameRepository = new GameRepositoryImpl(unitOfWork);
        gameRepositoryTest = new GameRepositoryTestImpl(unitOfWork);
        userRespositoryTest = new UserRepositoryTestImpl(unitOfWork);

        gameRepository.buyPackage(2);

        User user = userRespositoryTest.getUserFromID(2);
        int[] packages = gameRepositoryTest.getPackages();
        List<Card> cards = gameRepositoryTest.getCards();

        assertEquals(15, user.getCoins());

        assertEquals(1, packages[2]);

        for(Card card : cards) {
            assertEquals(2, card.getOwnedBy());
        }
    }

    @Test
    public void getCardsTest() {
        Collection<Card> cards = gameRepository.getCards(2);

        List<Card> indexed_cards = cards.stream().toList();

        assertEquals("644808c2-f87a-4600-b313-122b02322fd5", indexed_cards.get(0).getId());
        assertEquals("WaterGoblin", indexed_cards.get(0).getName());
        assertEquals(9, indexed_cards.get(0).getDamage());
        assertEquals("water", indexed_cards.get(0).getSpecialty());
        assertEquals("monster", indexed_cards.get(0).getType());
        assertEquals(2, indexed_cards.get(0).getOwnedBy());

        assertEquals("f3fad0f2-a1af-45df-b80d-2e48825773d9", indexed_cards.get(1).getId());
        assertEquals("Ork", indexed_cards.get(1).getName());
        assertEquals(45, indexed_cards.get(1).getDamage());
        assertEquals("normal", indexed_cards.get(1).getSpecialty());
        assertEquals("monster", indexed_cards.get(1).getType());
        assertEquals(2, indexed_cards.get(1).getOwnedBy());
    }

    @Test
    public void getDeckTest() {
        Collection<Card> cards = gameRepository.getDeck(2);
        List<Card> indexed_cards = cards.stream().toList();

        assertEquals("644808c2-f87a-4600-b313-122b02322fd5", indexed_cards.get(0).getId());
        assertEquals("WaterGoblin", indexed_cards.get(0).getName());
        assertEquals(9, indexed_cards.get(0).getDamage());
        assertEquals("water", indexed_cards.get(0).getSpecialty());
        assertEquals("monster", indexed_cards.get(0).getType());
        assertEquals(2, indexed_cards.get(0).getOwnedBy());
    }

    @Test
    public void createDeckTest() {
        gameRepository.createDeck(2, new String[] {"f3fad0f2-a1af-45df-b80d-2e48825773d9"});
        Collection<Card> cards = gameRepository.getDeck(2);
        List<Card> indexed_cards = cards.stream().toList();

        assertEquals(1, indexed_cards.size());
        assertEquals("f3fad0f2-a1af-45df-b80d-2e48825773d9", indexed_cards.get(0).getId());
        assertEquals("Ork", indexed_cards.get(0).getName());
        assertEquals(45, indexed_cards.get(0).getDamage());
        assertEquals("normal", indexed_cards.get(0).getSpecialty());
        assertEquals("monster", indexed_cards.get(0).getType());
        assertEquals(2, indexed_cards.get(0).getOwnedBy());
    }

    @Test
    public void battleTest() {
        gameRepository.battle(2);
        List<String> log = gameRepository.battle(3);

        assertEquals("You Won!", log.get(0));
        assertEquals("Player test2 used his Redemption-card and won round 1.", log.get(1));
        assertEquals("Player test used his Redemption-card and won round 2.", log.get(2));
        assertEquals("Player test2 won round 3 with Ork (Damage: 55) against WaterGoblin(Damage: 9).", log.get(3));
    }

    @Test
    public void getTradings() {
        List<Trading> tradings = gameRepository.getTradings();

        assertEquals("6cd85277-4590d-49d4-b0cf-ba0a9f1faad0", tradings.get(0).getId());
        assertEquals("f3fad0f2-a1af-45df-b80d-2e48825773d9", tradings.get(0).getCardToTrade());
        assertEquals("spell", tradings.get(0).getType());
        assertEquals(50, tradings.get(0).getMinimumDamage());
    }

    @Test
    public void createTradingTest() {
        Trading tradeOffer = Trading.builder()
                .id("6cd85277-4590d-49d4-b0cf-ba0a921faad0")
                .cardToTrade("a6fde738-c65a-4b10-b400-6fef0fdb28ba")
                .type("monster")
                .minimumDamage(60)
                .build();

        gameRepository.createTrading(tradeOffer);

        List<Trading> tradings = gameRepository.getTradings();

        assertEquals(tradeOffer.getId(), tradings.get(1).getId());
        assertEquals(tradeOffer.getCardToTrade(), tradings.get(1).getCardToTrade());
        assertEquals(tradeOffer.getType(), tradings.get(1).getType());
        assertEquals(tradeOffer.getMinimumDamage(), tradings.get(1).getMinimumDamage());
    }

    @Test
    public void acceptTradingTest() {
        userRepository = new UserRepositoryImpl(unitOfWork);

        gameRepository.acceptTrading(gameRepository.getCardFromTradingID("6cd85277-4590d-49d4-b0cf-ba0a9f1faad0"), "a6fde738-c65a-4b10-b400-6fef0fdb28ba", 2, 3);

        int userIDOfferAfterTrade = userRepository.getOwnerFromCard("f3fad0f2-a1af-45df-b80d-2e48825773d9");
        int userIDAcceptanceAfterTrade = userRepository.getOwnerFromCard("a6fde738-c65a-4b10-b400-6fef0fdb28ba");

        assertEquals(3, userIDOfferAfterTrade);
        assertEquals(2, userIDAcceptanceAfterTrade);
    }
    
    @Test
    public void deleteTradingTest() {
        gameRepository.deleteTrading("f3fad0f2-a1af-45df-b80d-2e48825773d9");

        List<Trading> tradings = gameRepository.getTradings();

        assertTrue(tradings.isEmpty());
    }

    @Test
    public void getCardFromTradingIDTest() {
        String cardID = gameRepository.getCardFromTradingID("6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");

        assertEquals("f3fad0f2-a1af-45df-b80d-2e48825773d9", cardID);
    }

    @Test
    public void checkIfTradingExistsTrueTest() {
        boolean exists = gameRepository.checkIfTradingExists("f3fad0f2-a1af-45df-b80d-2e48825773d9");

        assertTrue(exists);
    }

    @Test
    public void checkIfTradingExistsFalseTest() {
        boolean exists = gameRepository.checkIfTradingExists("a6fde738-c65a-4b10-b400-6fef0fdb28ba");

        assertFalse(exists);
    }
}
