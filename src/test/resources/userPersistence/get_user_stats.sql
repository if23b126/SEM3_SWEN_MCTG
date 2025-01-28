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


INSERT into public.users (username, password, "name", bio, image, wins, losses, ties, elo)
values ('test1', 'dGVzdA==', 'testName1', 'me playin', ':-)', 5, 10, 3, 897);

INSERT into public.users (username, password, "name", bio, image, wins, losses, ties, elo)
values ('test1', 'dGVzdA==', 'testName2', 'me playin you', ';-)', 30, 5, 1, 1900);
