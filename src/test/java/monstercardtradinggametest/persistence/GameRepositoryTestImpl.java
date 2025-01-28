package monstercardtradinggametest.persistence;

import monstercardtradinggame.model.Card;
import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;
import monstercardtradinggametest.persistence.GameRepositoryTest;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameRepositoryTestImpl implements GameRepositoryTest {
    UnitOfWork unitOfWork;

    public GameRepositoryTestImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public List<Card> getCards() {
        List<Card> result = new ArrayList<>();
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT id, "name", damage, specialty, "type", owned_by
                FROM public.cards ORDER BY damage asc;
            """)) {
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                result.add(Card.builder()
                        .id(rs.getString(1))
                        .name(rs.getString(2))
                        .damage(rs.getInt(3))
                        .specialty(rs.getString(4))
                        .type(rs.getString(5))
                        .ownedBy(rs.getInt(6))
                        .build());
            }
        } catch(SQLException e) {
            throw new DataAccessException("getCards test not successful", e);
        }

        return result;
    }

    @Override
    public int[] getPackages() {
        int[] result = new int[3];
        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT id, created_by, is_bought
                FROM public.packages;
            """)) {
            ResultSet rs = select.executeQuery();
            if(rs.next()) {
                result[0] = rs.getInt(1);
                result[1] = rs.getInt(2);
                result[2] = rs.getBoolean(3) ? 1 : 0;
            }
        } catch(SQLException e) {
            throw new DataAccessException("getPackages test not successful", e);
        }

        return result;
    }

    @Override
    public List<String[]> getCardsInPackages() {
        List<String[]> result = new ArrayList<>();

        try(PreparedStatement select = this.unitOfWork.prepareStatement("""
                SELECT card_id, package_id
                FROM public.cards_in_packages cip
                LEFT JOIN public.cards c ON cip.card_id = c.id
                ORDER BY c.damage asc;
            """)) {
            ResultSet rs = select.executeQuery();
            while(rs.next()) {
                result.add(new String[] {rs.getString(1), String.valueOf(rs.getInt(2))});
            }
        } catch(SQLException e) {
            throw new DataAccessException("getCardsInPackages test not successful", e);
        }

        return result;
    }
}
