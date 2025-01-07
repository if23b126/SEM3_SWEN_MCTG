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
import java.util.Collection;

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

        try(PreparedStatement select_coins = this.unitOfWork.prepareStatement("""
                SELECT coins from public.users
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
            """)) {

        } catch (SQLException e) {
            throw new DataAccessException("buyPackage SQL nicht erfolgreich", e);
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
