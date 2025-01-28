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


INSERT into public.users (username, password, name)
values ('test1', 'dGVzdA==', 'test1');

INSERT into public.users (username, password, name)
values ('test2', 'dGVzdA==', 'test2');