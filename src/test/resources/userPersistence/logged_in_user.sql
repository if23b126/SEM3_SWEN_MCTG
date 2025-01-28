DROP ALL OBJECTS;

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


INSERT into public.users (username, password)
values ('test', 'dGVzdA==');

INSERT into public.users (username, password, isadmin)
values ('admin', 'dGVzdA==', true);


CREATE TABLE public.currently_logged_in (
    username varchar(200) NULL,
    "token" varchar(500) NULL,
    CONSTRAINT pk_currently_logged_in_username UNIQUE (username)
);

INSERT INTO public.currently_logged_in (username, token)
values ('test', 'test-mtcgToken');

INSERT INTO public.currently_logged_in (username, token)
values ('admin', 'admin-mtcgToken');