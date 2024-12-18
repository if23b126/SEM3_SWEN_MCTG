package monstercardtradinggame.persistence.repository;

import monstercardtradinggame.persistence.UnitOfWork;

public class GameRepositoryImpl implements GameRepository {
    private UnitOfWork unitOfWork;


    public GameRepositoryImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }
}
