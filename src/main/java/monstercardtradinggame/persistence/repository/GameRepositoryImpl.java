package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.model.Card;
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


    public GameRepositoryImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public Boolean createPackage(Collection<Card> cards, int userID) {
        Boolean response = false;

        try(PreparedStatement insert_package = this.unitOfWork.prepareStatement("""
                INSERT INTO public.packages
                    (created_by)
                VALUES(?);
            """, true);
            PreparedStatement insert_cards = this.unitOfWork.prepareStatement("""
                INSERT INTO public.cards
                    (id, name, damage, type)
                VALUES(?, ?, ?, ?);
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

                    insert_relations.setString(1, card.getId());
                    insert_relations.setInt(2, package_id);

                    insert_cards.executeUpdate();
                    insert_relations.executeUpdate();
                }

                response = true;

                this.unitOfWork.commitTransaction();
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
                SELECT * FROM public.cards
                WHERE owned_by=?
            """)) {
            select.setInt(1, userID);
            ResultSet result = select.executeQuery();
            while(result.next()) {
                cards.add(Card.builder()
                        .id(result.getString(1))
                        .name(result.getString(2))
                        .damage(result.getInt(3))
                        .type(result.getString(4))
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
                SELECT c.* FROM public.cards_in_decks cid
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
                        .type(result.getString(4))
                        .build());
                }
            }
        } catch(SQLException e) {
            throw new DataAccessException("getDeck SQL nicht erfolgreich", e);
        }

        return cards;
    }

    @Override
    public Boolean createDeck(int userID, String[] cards) {
        Boolean result = false;

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
    public int battle(int userID) {
        int result = -1;
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

    private Boolean checkIfPackageAvailable(){
        Boolean response = false;
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

    private Boolean checkIfCardExist(Collection<Card> cards) {
        Boolean response = false;
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

    private Boolean checkCardOwnership(int userID, String[] cards) {
        Boolean result = true;
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

    private Boolean checkIfDeckExist(int userID) {
        Boolean response = false;
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

    private int executeBattle(int initiator, int opponent) {
        int winner = 0;

        List<Card> initiatorCards = (List<Card>)getDeck(initiator);
        List<Card> opponentCards = (List<Card>)getDeck(opponent);

        for(int i = 0; i < 100; i++) {
            Random rand = new Random();
            Card initiatorCard = initiatorCards.get(rand.nextInt(initiatorCards.size()));
            Card opponentCard = opponentCards.get(rand.nextInt(opponentCards.size()));

            if(initiatorCard.getType().equals(opponentCard.getType())) {
                if(initiatorCard.getDamage() < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage() > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            } else if(initiatorCard.getType().equals("water") && opponentCard.getType().equals("fire")) {
                if(initiatorCard.getDamage()*2 < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage()*2 > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            } else if(initiatorCard.getType().equals("fire") && opponentCard.getType().equals("water")) {
                if(initiatorCard.getDamage()/2 < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage()/2 > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            }else if(initiatorCard.getType().equals("fire") && opponentCard.getType().equals("normal")) {
                if(initiatorCard.getDamage()*2 < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage()*2 > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            } else if(initiatorCard.getType().equals("normal") && opponentCard.getType().equals("fire")) {
                if(initiatorCard.getDamage()/2 < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage()/2 > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            } else if(initiatorCard.getType().equals("normal") && opponentCard.getType().equals("water")) {
                if(initiatorCard.getDamage()*2 < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage()*2 > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            } else if(initiatorCard.getType().equals("water") && opponentCard.getType().equals("normal")) {
                if(initiatorCard.getDamage()/2 < opponentCard.getDamage()) {
                    initiatorCards.remove(initiatorCard);
                    opponentCards.add(initiatorCard);
                } else if(initiatorCard.getDamage()/2 > opponentCard.getDamage()) {
                    opponentCards.remove(opponentCard);
                    initiatorCards.add(opponentCard);
                }
            }

            if(initiatorCards.isEmpty()){
                winner = 1;
                break;
            }
            if(opponentCards.isEmpty()){
                winner = 2;
                break;
            }
        }

        return winner;
    }
}
