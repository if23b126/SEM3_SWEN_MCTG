package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.model.Card;
import monstercardtradinggame.model.Trading;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class GameRepositoryImpl implements GameRepository {
    private UnitOfWork unitOfWork;
    private UserRepository userRepository;


    public GameRepositoryImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
        userRepository = new UserRepositoryImpl(unitOfWork);
    }

    @Override
    public int createPackage(Collection<Card> cards, int userID) {
        int response = -1;

        try(PreparedStatement insert_package = this.unitOfWork.prepareStatement("""
                INSERT INTO public.packages
                    (created_by)
                VALUES(?);
            """, true);
            PreparedStatement insert_cards = this.unitOfWork.prepareStatement("""
                INSERT INTO public.cards
                    (id, name, damage, specialty, type)
                VALUES(?, ?, ?, ?, ?);
            """);
            PreparedStatement insert_relations = this.unitOfWork.prepareStatement("""
                INSERT INTO public.cards_in_packages
                    (card_id, package_id)
                VALUES(?, ?);
            """)) {

            if(!checkIfCardExist(cards)) {

                insert_package.setInt(1, userID);
                int affectedRows = insert_package.executeUpdate();

                if (affectedRows == 0) {
                    throw new DataAccessException("creating package failed");
                }
                int package_id = -1;

                try (ResultSet generatedKeys = insert_package.getGeneratedKeys()) {
                    generatedKeys.next();
                    package_id = generatedKeys.getInt(1);
                } catch (SQLException e) {
                    this.unitOfWork.rollbackTransaction();
                    throw new DataAccessException("aquiring package_id failed");
                }


                for (Card card : cards) {
                    insert_cards.setString(1, card.getId());
                    insert_cards.setString(2, card.getName());
                    insert_cards.setInt(3, card.getDamage());
                    if(card.getName().startsWith("Water")){
                        insert_cards.setString(4, "water");
                    } else if (card.getName().startsWith("Fire") || card.getName().startsWith("Dragon")){
                        insert_cards.setString(4, "fire");
                    } else {
                        insert_cards.setString(4, "normal");
                    }
                    if(card.getName().toLowerCase().contains("spell")){
                        insert_cards.setString(5, "spell");
                    } else {
                        insert_cards.setString(5, "monster");
                    }

                    insert_relations.setString(1, card.getId());
                    insert_relations.setInt(2, package_id);

                    insert_cards.executeUpdate();
                    insert_relations.executeUpdate();
                }

                response = 0;

                this.unitOfWork.commitTransaction();
            } else {
                response = 1;
            }

        } catch (SQLException e){
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("create Package SQL nicht erfolgreich", e);
        }

        return response;
    }

    @Override
    public int buyPackage(int userID) {
        int response = -1;

        try(PreparedStatement select_packages = this.unitOfWork.prepareStatement("""
                SELECT id FROM public.packages
                WHERE is_bought=false
            """);
            PreparedStatement select_coins = this.unitOfWork.prepareStatement("""
                SELECT coins FROM public.users
                WHERE id=?
            """);
            PreparedStatement update_users = this.unitOfWork.prepareStatement("""
                UPDATE public.users
                SET coins=?
                WHERE id=?
            """);
            PreparedStatement update_package = this.unitOfWork.prepareStatement("""
                UPDATE public.packages
                SET is_bought=true
                WHERE id=?;
            """);
            PreparedStatement select_cards = this.unitOfWork.prepareStatement("""
                SELECT card_id
                FROM public.cards_in_packages
                WHERE package_id=?
            """);
            PreparedStatement update_cards = this.unitOfWork.prepareStatement("""
                UPDATE public.cards
                SET owned_by=?
                WHERE id=?
            """)) {

            select_coins.setInt(1, userID);
            ResultSet coins_result = select_coins.executeQuery();
            int coins = -1;
            while (coins_result.next()) {
                coins = coins_result.getInt(1);
            }

            if(coins < 5) {
                response = -1;
            } else if(!checkIfPackageAvailable()){
                    response = 1;
            } else if(checkIfPackageAvailable()) {
                ResultSet package_result = select_packages.executeQuery();
                List<Integer> package_ids = new ArrayList<>();
                while (package_result.next()) {
                    package_ids.add(package_result.getInt(1));
                }
                Random rand = new Random();
                int package_id = package_ids.get(rand.ints(0, package_ids.size()).findFirst().getAsInt());

                update_users.setInt(1, coins - 5);
                update_users.setInt(2, userID);
                update_users.executeUpdate();

                update_package.setInt(1, package_id);
                update_package.executeUpdate();

                Collection<String> cards = new ArrayList<>();
                select_cards.setInt(1, package_id);
                ResultSet result_cards = select_cards.executeQuery();
                while (result_cards.next()) {
                    cards.add(result_cards.getString(1));
                }

                update_cards.setInt(1, userID);
                for (String card_id : cards) {
                    update_cards.setString(2, card_id);
                    update_cards.executeUpdate();
                }

                this.unitOfWork.commitTransaction();

                response = 0;
            }

        } catch (SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("buyPackage SQL nicht erfolgreich", e);
        }

        return response;
    }

    @Override
    public Collection<Card> getCards(int userID) {
        Collection<Card> cards = new ArrayList<>();
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT id, "name", damage, specialty, "type", owned_by FROM public.cards
                WHERE owned_by=?
            """)) {
            select.setInt(1, userID);
            ResultSet result = select.executeQuery();
            while(result.next()) {
                cards.add(Card.builder()
                        .id(result.getString(1))
                        .name(result.getString(2))
                        .damage(result.getInt(3))
                        .specialty(result.getString(4))
                        .type(result.getString(5))
                        .ownedBy(result.getInt(6))
                        .build());
            }
        } catch(SQLException e) {
            throw new DataAccessException("getCards SQL nicht erfolgreich", e);
        }

        return cards;
    }

    @Override
    public Collection<Card> getDeck(int userID) {
        Collection<Card> cards = new ArrayList<>();
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT c.id, c."name", c.damage, c.specialty, c."type", c.owned_by FROM public.cards_in_decks cid
                LEFT JOIN public.cards c
                ON cid.card_id=c.id AND c.owned_by=?
            """)) {
            select.setInt(1, userID);
            ResultSet result = select.executeQuery();
            while(result.next()) {
                if(result.getInt(3) != 0) {
                    cards.add(Card.builder()
                        .id(result.getString(1))
                        .name(result.getString(2))
                        .damage(result.getInt(3))
                        .specialty(result.getString(4))
                        .type(result.getString(5))
                        .ownedBy(result.getInt(6))
                        .build());
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException("getDeck SQL nicht erfolgreich", e);
        }

        return cards;
    }

    @Override
    public boolean createDeck(int userID, String[] cards) {
        boolean result = false;

        try(PreparedStatement delete_relation = this.unitOfWork.prepareStatement("""
                DELETE FROM public.cards_in_decks
                WHERE deck_id=(SELECT id FROM public.decks d WHERE d.user_id=?)
            """);
            PreparedStatement delete_deck = this.unitOfWork.prepareStatement("""
                DELETE FROM public.decks
                WHERE user_id=?
            """);
            PreparedStatement insert_deck = this.unitOfWork.prepareStatement("""
                INSERT INTO public.decks
                (user_id)
                VALUES(?)
            """, true);
            PreparedStatement insert_relation = this.unitOfWork.prepareStatement("""
                INSERT INTO public.cards_in_decks
                (deck_id, card_id)
                VALUES(?, ?)
            """)) {

            if(checkCardOwnership(userID, cards)) {

                if (checkIfDeckExist(userID)) {
                    delete_relation.setInt(1, userID);
                    delete_relation.executeUpdate();

                    delete_deck.setInt(1, userID);
                    delete_deck.executeUpdate();
                }

                insert_deck.setInt(1, userID);
                insert_deck.executeUpdate();

                int deck_id = -1;

                try (ResultSet generatedKeys = insert_deck.getGeneratedKeys()) {
                    generatedKeys.next();
                    deck_id = generatedKeys.getInt(1);
                } catch (SQLException e) {
                    this.unitOfWork.rollbackTransaction();
                    throw new DataAccessException("aquiring deck_id failed");
                }

                insert_relation.setInt(1, deck_id);
                for (String card : cards) {
                    insert_relation.setString(2, card);
                    insert_relation.executeUpdate();
                }

                this.unitOfWork.commitTransaction();
                result = true;
            }

        } catch (SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("createDeck SQL nicht erfolgreich", e);
        }

        return result;
    }

    @Override
    public List<String> battle(int userID) {
        List<String> result = new ArrayList<>();
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT user_id FROM public.ready_to_battle
            """);
            PreparedStatement insert = this.unitOfWork.prepareStatement("""
                INSERT INTO public.ready_to_battle
                (user_id)
                VALUES(?);
            """);
            PreparedStatement delete = this.unitOfWork.prepareStatement("""
                DELETE FROM public.ready_to_battle
                WHERE user_id=?;
            """)) {
            List<Integer> playersReadyForBattle = new ArrayList<>();
            ResultSet rs = select.executeQuery();

            while(rs.next()) {
                playersReadyForBattle.add(rs.getInt(1));
            }

            if(playersReadyForBattle.isEmpty()) {
                insert.setInt(1, userID);
                insert.executeUpdate();
            } else {
                Random rand = new Random();
                int opponentID = playersReadyForBattle.get(rand.ints(0, playersReadyForBattle.size()).findFirst().getAsInt());
                result = executeBattle(userID, opponentID);

                delete.setInt(1, opponentID);
                delete.executeUpdate();
                delete.setInt(1, userID);
                delete.executeUpdate();
            }

            this.unitOfWork.commitTransaction();
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("battle SQL nicht erfolgreich", e);
        }

        return result;
    }

    private boolean checkIfPackageAvailable(){
        boolean response = false;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT count(*) FROM public.packages
                WHERE is_bought=false
            """)) {
            ResultSet rs = select.executeQuery();
            int result = -1;
            while(rs.next()) {
                result = rs.getInt(1);
            }

            response = result != 0;

        } catch (SQLException e){
            throw new DataAccessException("checkIfPackageAvailable SQL nicht erfolgreich", e);
        }

        return response;
    }

    @Override
    public List<Trading> getTradings() {
        List<Trading> tradings = new ArrayList<>();

        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT id, card_id, "type", minimumdamage
                FROM public.trading
                WHERE isactive=true
            """)) {
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                tradings.add(Trading.builder()
                        .id(rs.getString(1))
                        .cardToTrade(rs.getString(2))
                        .type(rs.getString(3))
                        .minimumDamage(rs.getInt(4))
                        .build());
            }
        } catch(SQLException e) {
            throw new DataAccessException("getTradings SQL nicht erfolgreich", e);
        }

        return tradings;
    }

    @Override
    public boolean createTrading(Trading trading) {
        boolean result = false;

        try(PreparedStatement insert = this.unitOfWork.prepareStatement("""
                INSERT INTO public.trading
                (id, card_id, type, minimumdamage)
                VALUES(?, ?, ?, ?);
            """)) {
            if(!(checkIfCardIsInDeck(trading.getCardToTrade()))) {
                insert.setString(1, trading.getId());
                insert.setString(2, trading.getCardToTrade());
                insert.setString(3, trading.getType());
                insert.setInt(4, trading.getMinimumDamage());
                insert.executeUpdate();

                result = true;
                this.unitOfWork.commitTransaction();
            }
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("createTrading SQL nicht erfolgreich", e);
        }
        
        return result;
    }

    @Override
    public boolean checkIfTradingExists(String cardID) {
        boolean response = false;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT count(*)
                FROM public.trading
                WHERE card_id=?
            """)) {
            select.setString(1, cardID);
            ResultSet rs = select.executeQuery();
            if(rs.next()) {
                response = rs.getInt(1) != 0;
            }
        } catch(SQLException e) {
            throw new DataAccessException("checkIfTradingExists SQL nicht erfolgreich", e);
        }

        return response;
    }

    @Override
    public boolean acceptTrading(String offer, String acceptance, int offer_userID, int acceptance_userID) {
        boolean result = false;

        try(PreparedStatement update_trading = this.unitOfWork.prepareStatement("""
                UPDATE public.trading
                SET isactive=false
                WHERE card_id=?
            """);
            PreparedStatement update_cards = this.unitOfWork.prepareStatement("""
                UPDATE public.cards
                SET owned_by=?
                WHERE id=?
            """);
            PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT type, damage
                FROM public.cards
                WHERE id=?
            """)) {

            Trading tradeOffer = getTrading(offer);
            Card acceptanceCard = getCardFromId(acceptance);

            if(offer_userID != acceptance_userID &&
                    acceptanceCard.getDamage() >= tradeOffer.getMinimumDamage() &&
                    acceptanceCard.getType().equals(tradeOffer.getType()) &&
                    !(checkIfCardIsInDeck(acceptanceCard.getId()))) {
                update_trading.setString(1, offer);
                update_trading.executeUpdate();

                update_cards.setInt(1, offer_userID);
                update_cards.setString(2, acceptance);
                update_cards.executeUpdate();

                update_cards.setInt(1, acceptance_userID);
                update_cards.setString(2, tradeOffer.getCardToTrade());
                update_cards.executeUpdate();

                this.unitOfWork.commitTransaction();
                result = true;
            } else {
                result = false;
            }

        } catch(SQLException e){
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("acceptTrading SQL nicht erfolgreich", e);
        }

        return result;
    }

    private boolean checkIfCardIsInDeck(String cardId) {
        boolean response = false;

        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT count(*)
                FROM public.cards_in_decks
                WHERE card_id=?
            """)) {
            select.setString(1, cardId);
            ResultSet rs = select.executeQuery();
            if(rs.next()) {
                response = rs.getInt(1) != 0;
            }
        } catch(SQLException e) {
            throw new DataAccessException("checkIfCardIsInDeckSQL nicht erfolgreich", e);
        }

        return response;
    }

    private Card getCardFromId(String cardId) {
        Card card = null;

        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT name, damage, type
                FROM public.cards
                WHERE id=?
            """)) {
            select.setString(1, cardId);
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                card = Card.builder()
                        .id(cardId)
                        .name(rs.getString(1))
                        .damage(rs.getInt(2))
                        .type(rs.getString(3))
                        .build();
            }
        } catch(SQLException e) {
            throw new DataAccessException("getCardFromId SQL nicht erfolgreich", e);
        }

        return card;
    }

    private Trading getTrading(String tradingId) {
        Trading response = null;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT id, card_id, type, minimumdamage
                FROM public.trading
                WHERE card_id=?
            """)){
            select.setString(1, tradingId);
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                response = Trading.builder()
                        .id(rs.getString(1))
                        .cardToTrade(rs.getString(2))
                        .type(rs.getString(3))
                        .minimumDamage(rs.getInt(4))
                        .build();
            }
        } catch(SQLException e) {
            throw new DataAccessException("getTrading SQL nicht erfolgreich", e);
        }

        return response;
    }

    @Override
    public boolean deleteTrading(String trading) {
        boolean result = false;

        try(PreparedStatement update = this.unitOfWork.prepareStatement("""
                UPDATE public.trading
                SET isactive=false
                WHERE card_id=?
            """)) {
            update.setString(1, trading);
            update.executeUpdate();

            result = true;
            this.unitOfWork.commitTransaction();
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("deleteTrading SQL nicht erfolgreich", e);
        }

        return result;
    }

    private boolean checkIfCardExist(Collection<Card> cards) {
        boolean response = false;
        try(PreparedStatement select_count = this.unitOfWork.prepareStatement("""
                SELECT count(*) FROM public.cards
            """);
            PreparedStatement select = this.unitOfWork.prepareStatement("""
                    SELECT count(*) FROM public.cards
                    WHERE id=?
                """)) {
            ResultSet count_result = select_count.executeQuery();
            count_result.next();
            int count = count_result.getInt(1);
            if(count != 0) {
                for (Card card : cards) {
                    int result = -1;
                    select.setString(1, card.getId());
                    ResultSet rs = select.executeQuery();
                    while (rs.next()) {
                        result = rs.getInt(1);
                    }
                    if (result != 0) {
                        response = true;
                    }
                }
            }
        } catch(SQLException e){
            throw new DataAccessException("select cards not successful");
        }

        return response;
    }

    @Override
    public String getCardFromTradingID(String tradingID) {
        String result = null;

        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT card_id
                FROM public.trading
                WHERE id=?
            """)) {
            select.setString(1, tradingID);
            ResultSet rs = select.executeQuery();
            if(rs.next()) {
                result = rs.getString(1);
            }
        } catch(SQLException e) {
            throw new DataAccessException("getCardFromTradingID SQL nicht erfolgreich", e);
        }

        return result;
    }

    private boolean checkCardOwnership(int userID, String[] cards) {
        boolean result = true;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT owned_by FROM public.cards
                WHERE id=?
            """)) {
            for (String card : cards) {
                select.setString(1, card);
                ResultSet rs = select.executeQuery();
                rs.next();
                int cardOwner = rs.getInt(1);
                if(cardOwner != userID) {
                    result = false;
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException("checkCardOwnership SQL nicht erfolgreich", e);
        }

        return result;
    }

    private boolean checkIfDeckExist(int userID) {
        boolean response = false;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT count(*) FROM public.decks
                WHERE user_id=?
            """)) {
            select.setInt(1, userID);
            ResultSet deck_result = select.executeQuery();
            int result = -1;
            while (deck_result.next()) {
                result = deck_result.getInt(1);
            }
            response = result != 0;
        } catch (SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("checkIfDeckExist SQL nicht erfolgreich", e);
        }

        return response;
    }

    private List<String> executeBattle(int initiator, int opponent) {
        List<String> gameLog = new ArrayList<>();

        List<Card> initiatorCards = (List<Card>)getDeck(initiator);
        List<Card> opponentCards = (List<Card>)getDeck(opponent);
        boolean initiatorRedemption = false;
        boolean opponentRedemption = false;

        for(int i = 0; i < 100; i++) {
            Random rand = new Random();
            int initiatorIndex = rand.nextInt(initiatorCards.size());
            int opponentIndex = rand.nextInt(opponentCards.size());
            Card initiatorCard = initiatorCards.get(initiatorIndex);
            Card opponentCard = opponentCards.get(opponentIndex);

            if(initiatorCards.size() == 1 && !initiatorRedemption) {
                gameLog.add("Player " + userRepository.getUsernameFromID(initiator) + " used his Redemption-card and won round " + (i+1) + ".");
                initiatorRedemption = true;
            } else if(opponentCards.size() == 1 && !opponentRedemption){
                gameLog.add("Player " + userRepository.getUsernameFromID(opponent) + " used his Redemption-card and won round " + (i+1) + ".");
                opponentRedemption = true;
            } else if(calculateDamage(initiatorCard, opponentCard) < calculateDamage(opponentCard, initiatorCard)) {
                initiatorCards.remove(initiatorCard);
                opponentCards.add(initiatorCard);
                gameLog.add("Player " + userRepository.getUsernameFromID(opponent) + " won round " + (i+1) + " with " + opponentCard.getName() + " (Damage: " + calculateDamage(opponentCard, initiatorCard) + ") against " + initiatorCard.getName() + " (Damage: " + calculateDamage(initiatorCard, opponentCard) + ").");
            } else if(calculateDamage(initiatorCard, opponentCard) > calculateDamage(opponentCard, initiatorCard)) {
                opponentCards.remove(opponentCard);
                initiatorCards.add(opponentCard);
                gameLog.add("Player " + userRepository.getUsernameFromID(initiator) + " won round " + (i+1) + " with " + initiatorCard.getName() + " (Damage: " + calculateDamage(initiatorCard, opponentCard) + ") against " + opponentCard.getName() + "(Damage: " + calculateDamage(opponentCard, initiatorCard) + ").");
            } else {
                gameLog.add("Round " + i + " has been a tie.");
            }

            if(initiatorCards.isEmpty() || opponentCards.isEmpty()) {
                break;
            }
        }
        if(opponentCards.isEmpty()){
            gameLog.addFirst("You Won!");
            userRepository.updateStats(initiator, opponent, 1);
        } else if(initiatorCards.isEmpty()){
            gameLog.addFirst("You Lost!");
            userRepository.updateStats(initiator, opponent, 0);
        } else {
            gameLog.addFirst("It's a tie");
            userRepository.updateStats(initiator, opponent, 2);
        }
        return gameLog;
    }

    private int calculateDamage(Card initiatorCard, Card opponentCard) {
        int result = -1;
        if(initiatorCard.getName().toLowerCase().contains("spell") || opponentCard.getName().toLowerCase().contains("spell")) {
            if (initiatorCard.getName().equalsIgnoreCase("waterspell") && opponentCard.getName().equalsIgnoreCase("knight")) {
                result = Integer.MAX_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("knight") && opponentCard.getName().equalsIgnoreCase("waterspell")) {
                result = Integer.MIN_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("kraken") && opponentCard.getName().toLowerCase().contains("spell")) {
                result = Integer.MAX_VALUE;
            } else if (opponentCard.getName().toLowerCase().contains("spell") && opponentCard.getName().equalsIgnoreCase("kraken")) {
                result = Integer.MIN_VALUE;
            } else if (initiatorCard.getType().equalsIgnoreCase(opponentCard.getType())) {
                result = 0;
            } else if (initiatorCard.getType().equalsIgnoreCase("water") && opponentCard.getType().equalsIgnoreCase("fire")) {
                result = initiatorCard.getDamage() * 2;
            } else if (initiatorCard.getType().equalsIgnoreCase("fire") && opponentCard.getType().equalsIgnoreCase("water")) {
                result = initiatorCard.getDamage() / 2;
            } else if (initiatorCard.getType().equalsIgnoreCase("fire") && opponentCard.getType().equalsIgnoreCase("normal")) {
                result = initiatorCard.getDamage() * 2;
            } else if (initiatorCard.getType().equalsIgnoreCase("normal") && opponentCard.getType().equalsIgnoreCase("fire")) {
                result = initiatorCard.getDamage() / 2;
            } else if (initiatorCard.getType().equalsIgnoreCase("normal") && opponentCard.getType().equalsIgnoreCase("water")) {
                result = initiatorCard.getDamage() * 2;
            } else if (initiatorCard.getType().equalsIgnoreCase("water") && opponentCard.getType().equalsIgnoreCase("normal")) {
                result = initiatorCard.getDamage() / 2;
            }
        } else {
            if (initiatorCard.getName().equalsIgnoreCase("watergoblin") && opponentCard.getName().equalsIgnoreCase("dragon")) {
                result = Integer.MAX_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("dragon") && opponentCard.getName().equalsIgnoreCase("watergoblin")) {
                result = Integer.MIN_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("wizard") && opponentCard.getName().equalsIgnoreCase("ork")) {
                result = Integer.MAX_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("ork") && opponentCard.getName().equalsIgnoreCase("wizard")) {
                result = Integer.MIN_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("fireelve") && opponentCard.getName().equalsIgnoreCase("dragon")) {
                result = Integer.MAX_VALUE;
            } else if (initiatorCard.getName().equalsIgnoreCase("dragon") && opponentCard.getName().equalsIgnoreCase("fireelve")) {
                result = Integer.MIN_VALUE;
            } else {
                result = initiatorCard.getDamage();
            }
        }

        return result;
    }
}
