package monstercardtradinggametest.persistence;

import monstercardtradinggame.persistence.DataAccessException;
import monstercardtradinggame.persistence.UnitOfWork;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrepareUserImpl implements PrepareUser {
    private UnitOfWork unitOfWork;

    public PrepareUserImpl(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }


    @Override
    public boolean prepareTableForCreateUserTest() {
        boolean result = false;

        try(PreparedStatement create = this.unitOfWork.prepareStatement("""
                CREATE TABLE public.users (
                	id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
                	username varchar(200) NULL,
                	"password" varchar(200) NULL,
                	image varchar(10) NULL,
                	coins int4 DEFAULT 20 NULL,
                	isadmin bool DEFAULT false NULL,
                	"name" varchar(200) NULL,
                	bio varchar(200) NULL,
                	wins int4 DEFAULT 0 NULL,
                	losses int4 DEFAULT 0 NULL,
                	"ties" int4 DEFAULT 0 NULL,
                	elo int4 DEFAULT 1000 NULL,
                	CONSTRAINT users_pkey PRIMARY KEY (id)
                );
            """)) {
            create.executeUpdate();
            result = true;
            this.unitOfWork.commitTransaction();
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("create table unsuccessful", e);
        }

        return result;
    }

    @Override
    public boolean prepareTableForLoginUserTest() {
        boolean result = false;

        try(PreparedStatement create = this.unitOfWork.prepareStatement("""
                CREATE TABLE public.currently_logged_in (
                    username varchar(200) NULL,
                    "token" varchar(500) NULL,
                    CONSTRAINT pk_currently_logged_in_username UNIQUE (username)
                );
            """)) {
            create.executeUpdate();
            result = true;
            this.unitOfWork.commitTransaction();
        } catch(SQLException e) {
            this.unitOfWork.rollbackTransaction();
            throw new DataAccessException("create table unsuccessful", e);
        }

        return result;
    }
}
