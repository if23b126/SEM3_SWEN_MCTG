package monstercardtradinggametest.service;

import httpserver.server.HeaderMap;
import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggame.persistence.repository.GameRepository;
import monstercardtradinggame.persistence.repository.GameRepositoryImpl;
import monstercardtradinggame.persistence.repository.UserRepository;
import monstercardtradinggame.persistence.repository.UserRepositoryImpl;
import monstercardtradinggame.service.GameService;
import monstercardtradinggame.service.GameServiceImpl;
import monstercardtradinggame.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameServiceTest {
    static GameService gameService;
    @BeforeEach
    public void setUp() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:gamePersistence/game_service.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        GameRepository gameRepository = new GameRepositoryImpl(unitOfWork);
        UserRepository userRepository = new UserRepositoryImpl(unitOfWork);
        gameService = new GameServiceImpl(gameRepository, userRepository);
    }

    @Test
    public void createPackageSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer admin-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]");
        Response response = gameService.createPackage(request);
        assertEquals(201, response.getStatus());
    }

    @Test
    public void createPackageCardAlreadyExistsTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer admin-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[{\"Id\":\"951e886a-0fbf-425d-8df5-af2ee4830d85\", \"Name\":\"Ork\", \"Damage\": 55.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]");
        Response response = gameService.createPackage(request);
        assertEquals(409, response.getStatus());
        assertEquals("At least one card in the packages already exists", response.getContent());
    }

    @Test
    public void createPackageWithoutAdminTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]");
        Response response = gameService.createPackage(request);
        assertEquals(403, response.getStatus());
        assertEquals("Provided user is not \"admin\"", response.getContent());
    }

    @Test
    public void createPackageNotLoggedInTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer admin2-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[{\"Id\":\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"Name\":\"WaterGoblin\", \"Damage\": 10.0}, {\"Id\":\"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", \"Name\":\"Dragon\", \"Damage\": 50.0}, {\"Id\":\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"Name\":\"WaterSpell\", \"Damage\": 20.0}, {\"Id\":\"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\", \"Name\":\"Ork\", \"Damage\": 45.0}, {\"Id\":\"dfdd758f-649c-40f9-ba3a-8657f4b3439f\", \"Name\":\"FireSpell\",    \"Damage\": 25.0}]");
        Response response = gameService.createPackage(request);
        assertEquals(401, response.getStatus());
        assertEquals("User is not logged in", response.getContent());
    }

    @Test
    public void buyPackageSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.buyPackage(request);
        assertEquals(200, response.getStatus());
        assertEquals("A package has been successfully bought", response.getContent());
    }

    @Test
    public void buyPackageNotEnoughMoneyTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer noMoney-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.buyPackage(request);
        assertEquals(403, response.getStatus());
        assertEquals("Not enough money for buying a card package", response.getContent());
    }

    @Test
    public void buyPackageNoPackageAvailableTest() {
        String jdbcUrl = "jdbc:h2:~/mctg;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=RUNSCRIPT FROM 'classpath:gamePersistence/no_package_available.sql'";
        UnitOfWork unitOfWork = new UnitOfWork(jdbcUrl);
        GameRepository gameRepository = new GameRepositoryImpl(unitOfWork);
        UserRepository userRepository = new UserRepositoryImpl(unitOfWork);
        gameService = new GameServiceImpl(gameRepository, userRepository);


        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.buyPackage(request);
        assertEquals(404, response.getStatus());
        assertEquals("No card package available for buying", response.getContent());
    }

    @Test
    public void buyPackageMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.buyPackage(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void getCardsSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getCards(request);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"Id\":\"644808c2-f87a-4600-b313-122b02322fd5\",\"Name\":\"WaterGoblin\",\"Damage\":9,\"Specialty\":\"water\",\"Type\":\"monster\",\"OwnedBy\":2},{\"Id\":\"f3fad0f2-a1af-45df-b80d-2e48825773d9\",\"Name\":\"Ork\",\"Damage\":45,\"Specialty\":\"normal\",\"Type\":\"monster\",\"OwnedBy\":2},{\"Id\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\",\"Name\":\"Ork\",\"Damage\":55,\"Specialty\":\"normal\",\"Type\":\"monster\",\"OwnedBy\":2},{\"Id\":\"88221cfe-1f84-41b9-8152-8e36c6a354de\",\"Name\":\"WaterSpell\",\"Damage\":22,\"Specialty\":\"water\",\"Type\":\"spell\",\"OwnedBy\":2}]", response.getContent());
    }

    @Test
    public void getCardsNoCardsForUserTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer admin-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getCards(request);
        assertEquals(409, response.getStatus());
        assertEquals("The request was fine, but the user doesn't have any cards", response.getContent());
    }

    @Test
    public void getCardsMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.getCards(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void getDeckJsonSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getDeck(request, false);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"Id\":\"644808c2-f87a-4600-b313-122b02322fd5\",\"Name\":\"WaterGoblin\",\"Damage\":9,\"Specialty\":\"water\",\"Type\":\"monster\",\"OwnedBy\":2}]", response.getContent());
    }

    @Test
    public void getDeckPlainSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getDeck(request, true);
        assertEquals(200, response.getStatus());
        assertEquals("Card 1\n" +
                "\tID: 644808c2-f87a-4600-b313-122b02322fd5, \n" +
                "\tName: WaterGoblin, \n" +
                "\tDamage: 9, \n" +
                "\tType: monster\n", response.getContent());
    }

    @Test
    public void getDeckNoConfiguredDeckTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer admin-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getDeck(request, false);
        assertEquals(409, response.getStatus());
        assertEquals("The request was fine, but the deck doesn't have any cards", response.getContent());
    }

    @Test
    public void getDeckMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.getDeck(request, false);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void putDeckSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[\"88221cfe-1f84-41b9-8152-8e36c6a354de\", \"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"f3fad0f2-a1af-45df-b80d-2e48825773d9\", \"644808c2-f87a-4600-b313-122b02322fd5\"]");
        Response response = gameService.putDeck(request);
        assertEquals(201, response.getStatus());
        assertEquals("The deck has been successfully configured", response.getContent());
    }

    @Test
    public void putDeckNotEnoughCardsTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"f3fad0f2-a1af-45df-b80d-2e48825773d9\", \"644808c2-f87a-4600-b313-122b02322fd5\"]");
        Response response = gameService.putDeck(request);
        assertEquals(400, response.getStatus());
        assertEquals("The provided deck did not include the required amount of cards", response.getContent());
    }

    @Test
    public void putDeckCardNotBelongingToUserTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("[\"951e886a-0fbf-425d-8df5-af2ee4830d85\", \"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"f3fad0f2-a1af-45df-b80d-2e48825773d9\", \"644808c2-f87a-4600-b313-122b02322fd5\"]");
        Response response = gameService.putDeck(request);
        assertEquals(409, response.getStatus());
        assertEquals("At least one of the provided cards does not belong to the user or is not available.", response.getContent());
    }

    @Test
    public void putDeckMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        request.setBody("[\"88221cfe-1f84-41b9-8152-8e36c6a354de\", \"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\", \"f3fad0f2-a1af-45df-b80d-2e48825773d9\", \"644808c2-f87a-4600-b313-122b02322fd5\"]");
        Response response = gameService.putDeck(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void getStatsSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getStats(request);
        assertEquals(200, response.getStatus());
        assertEquals("{\"Name\":null,\"Elo\":1000,\"Wins\":0,\"Losses\":0,\"Ties\":0}", response.getContent());
    }

    @Test
    public void getStatsMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.getStats(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void getScoreboardSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getScoreboards(request);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"Name\":null,\"Elo\":1000,\"Wins\":0,\"Losses\":0,\"Ties\":0},{\"Name\":null,\"Elo\":1000,\"Wins\":0,\"Losses\":0,\"Ties\":0},{\"Name\":null,\"Elo\":1000,\"Wins\":0,\"Losses\":0,\"Ties\":0},{\"Name\":null,\"Elo\":1000,\"Wins\":0,\"Losses\":0,\"Ties\":0},{\"Name\":null,\"Elo\":1000,\"Wins\":0,\"Losses\":0,\"Ties\":0}]", response.getContent());
    }

    @Test
    public void getScoreboardMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.getScoreboards(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void battleSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.battle(request);
        assertEquals(200, response.getStatus());
        assertEquals("Waiting for Opponent!", response.getContent());

        request = new Request();
        headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer christian-mtcgToken");
        request.setHeaderMap(headerMap);
        response = gameService.battle(request);
        assertEquals(200, response.getStatus());
        assertEquals("You Won!\nPlayer christian used his Redemption-card and won round 1.\nPlayer test used his Redemption-card and won round 2.\nPlayer christian won round 3 with Ork (Damage: 55) against WaterGoblin(Damage: 9).\n", response.getContent());
    }

    @Test
    public void battleMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.battle(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void getTradingSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        Response response = gameService.getTradings(request);
        assertEquals(200, response.getStatus());
        assertEquals("[{\"Id\":\"6cd85277-4590d-49d4-b0cf-ba0a9f1faad0\",\"CardToTrade\":\"f3fad0f2-a1af-45df-b80d-2e48825773d9\",\"Type\":\"spell\",\"MinimumDamage\":50}]", response.getContent());
    }

    @Test
    public void getTradingMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.getTradings(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void createTradingSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Id\":\"6cd85277-4590-4fd4-b0cf-ba0a921faad0\",\"CardToTrade\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\",\"Type\":\"monster\",\"MinimumDamage\":30}");
        Response response = gameService.createTrading(request);
        assertEquals(201, response.getStatus());
        assertEquals("Trading deal successfully created", response.getContent());
    }

    @Test
    public void createTradingAlreadyExistingIDTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Id\":\"6cd85277-4590d-49d4-b0cf-ba0a9f1faad0\",\"CardToTrade\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\",\"Type\":\"monster\",\"MinimumDamage\":30}");
        Response response = gameService.createTrading(request);
        assertEquals(409, response.getStatus());
        assertEquals("A deal with this deal ID already exists.", response.getContent());
    }

    @Test
    public void createTradingCardLockedInDeckTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Id\":\"6cd85277-4590-4fd4-b0cf-ba0a921faad0\",\"CardToTrade\":\"644808c2-f87a-4600-b313-122b02322fd5\",\"Type\":\"monster\",\"MinimumDamage\":30}");
        Response response = gameService.createTrading(request);
        assertEquals(400, response.getStatus());
        assertEquals("The deal contains a card that is locked in a deck.", response.getContent());
    }

    @Test
    public void createTradingNotCardOwnerTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setBody("{\"Id\":\"6cd85277-4590-4fd4-b0cf-ba0a921faad0\",\"CardToTrade\":\"951e886a-0fbf-425d-8df5-af2ee4830d85\",\"Type\":\"monster\",\"MinimumDamage\":30}");
        Response response = gameService.createTrading(request);
        assertEquals(403, response.getStatus());
        assertEquals("The deal contains a card that is not owned by the user.", response.getContent());
    }

    @Test
    public void createTradingMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        request.setBody("{\"Id\":\"6cd85277-4590-4fd4-b0cf-ba0a921faad0\",\"CardToTrade\":\"4ec8b269-0dfa-4f97-809a-2c63fe2a0025\",\"Type\":\"monster\",\"MinimumDamage\":30}");
        Response response = gameService.createTrading(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void acceptTradingSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer christian-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");
        request.setBody("\"a6fde738-c65a-4b10-b400-6fef0fdb28ba\"");
        Response response = gameService.acceptTrading(request);
        assertEquals(200, response.getStatus());
        assertEquals("Trading deal successfully executed.", response.getContent());
    }

    @Test
    public void acceptTradingOfferdCardDoesNotBelongToUserTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer christian-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");
        request.setBody("\"88221cfe-1f84-41b9-8152-8e36c6a354de\"");
        Response response = gameService.acceptTrading(request);
        assertEquals(403, response.getStatus());
        assertEquals("The offered card is not owned by the user.", response.getContent());
    }

    @Test
    public void acceptTradingDealIDNotFoundTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer christian-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590-4fd4-b0cf-ba0a921faad0");
        request.setBody("\"a6fde738-c65a-4b10-b400-6fef0fdb28ba\"");
        Response response = gameService.acceptTrading(request);
        assertEquals(404, response.getStatus());
        assertEquals("The provided deal ID was not found.", response.getContent());
    }

    @Test
    public void acceptTradingCardNotMeetingRequirementsTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer christian-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");
        request.setBody("\"a6fde738-c65a-4t10-b400-6fef0fdb28ba\"");
        Response response = gameService.acceptTrading(request);
        assertEquals(500, response.getStatus());
        assertEquals("Something went wrong", response.getContent());
    }

    @Test
    public void acceptTradingMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");
        request.setBody("\"a6fde738-c65a-4b10-b400-6fef0fdb28ba\"");
        Response response = gameService.acceptTrading(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }

    @Test
    public void deleteTradingSuccessTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");
        Response response = gameService.deleteTrading(request);
        assertEquals(200, response.getStatus());
        assertEquals("Trading deal successfully deleted.", response.getContent());
    }

    @Test
    public void deleteTradingNotOwnerOfTradeTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer christian-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590d-49d4-b0cf-ba0a9f1faad0");
        Response response = gameService.deleteTrading(request);
        assertEquals(409, response.getStatus());
        assertEquals("The deal contains a card that is not owned by the user.", response.getContent());
    }

    @Test
    public void deleteTradingTradingIDNotFoundTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        headerMap.ingest("Authorization: Bearer test-mtcgToken");
        request.setHeaderMap(headerMap);
        request.setPathname("/tradings/6cd85277-4590-4fd4-b0cf-ba0a921faad0");
        Response response = gameService.deleteTrading(request);
        assertEquals(404, response.getStatus());
        assertEquals("The provided deal ID was not found.", response.getContent());
    }

    @Test
    public void deleteTradingMissingTokenTest() {
        Request request = new Request();
        HeaderMap headerMap = new HeaderMap();
        request.setHeaderMap(headerMap);
        Response response = gameService.deleteTrading(request);
        assertEquals(401, response.getStatus());
        assertEquals("Access token is missing or invalid", response.getContent());
    }
}
