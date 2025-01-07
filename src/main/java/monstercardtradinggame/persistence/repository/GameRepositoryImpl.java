package monstercardtradinggame.persistence.repository;

import httpserver.server.Request;
import httpserver.server.Response;
import monstercardtradinggame.model.Card;
import monstercardtradinggame.model.User;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;

import javax.swing.plaf.nimbus.State;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
                    (id, name, damage)
                VALUES(?, ?, ?);
            """);
            PreparedStatement insert_relations = this.unitOfWork.prepareStatement("""
                INSERT INTO public.cards_in_packages
                    (card_id, package_id)
                VALUES(?, ?);
            """)) {

            if(!checkIfCardExisted(cards)) {

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

                    insert_relations.setString(1, card.getId());
                    insert_relations.setInt(2, package_id);

                    insert_cards.executeUpdate();
                    insert_relations.executeUpdate();
                }

                response = true;

                this.unitOfWork.commitTransaction();
            }

        } catch (SQLException e){
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
                cards.add(Card.builder()
                        .id(result.getString(1))
                        .name(result.getString(2))
                        .damage(result.getInt(3))
                        .type(result.getString(4))
                        .build());
            }
        } catch(SQLException e) {
            throw new DataAccessException("getDeck SQL nicht erfolgreich", e);
        }

        return cards;
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

    private Boolean checkIfCardExisted (Collection<Card> cards) {
        Boolean response = false;
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT count(*) FROM public.cards
                WHERE id=?
            """)) {
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
        } catch(SQLException e){
            throw new DataAccessException("select cards not successful");
        }

        return response;
    }
}
