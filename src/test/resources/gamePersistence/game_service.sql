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

CREATE TABLE public.currently_logged_in (
    username varchar(200) NULL,
    "token" varchar(500) NULL,
    CONSTRAINT pk_currently_logged_in_username UNIQUE (username)
);


CREATE TABLE public.packages (
    id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
    created_by int4 NULL,
    is_bought bool DEFAULT false NULL,
    CONSTRAINT pacakges_pk PRIMARY KEY (id)
);
ALTER TABLE public.packages ADD CONSTRAINT pacakges_users_fk FOREIGN KEY (created_by) REFERENCES public.users(id);


CREATE TABLE public.cards (
    id varchar(50) NOT NULL,
    "name" varchar(200) NULL,
    damage int4 NULL,
    specialty varchar(20) NULL,
    owned_by int4 NULL,
    "type" varchar(20) NULL,
    CONSTRAINT cards_pk PRIMARY KEY (id)
);
ALTER TABLE public.cards ADD CONSTRAINT cards_users_fk FOREIGN KEY (owned_by) REFERENCES public.users(id);


CREATE TABLE public.cards_in_packages (
    card_id varchar NULL,
    package_id int4 NULL
);
ALTER TABLE public.cards_in_packages ADD CONSTRAINT cards_in_packages_cards_fk FOREIGN KEY (card_id) REFERENCES public.cards(id);
ALTER TABLE public.cards_in_packages ADD CONSTRAINT cards_in_packages_pacakges_fk FOREIGN KEY (package_id) REFERENCES public.packages(id);


CREATE TABLE public.decks (
    id int4 GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START 1 CACHE 1 NO CYCLE) NOT NULL,
    user_id int4 NULL,
    CONSTRAINT decks_pkey PRIMARY KEY (id)
);

CREATE TABLE public.cards_in_decks (
    deck_id int4 NULL,
    card_id varchar NULL
);
ALTER TABLE public.cards_in_decks ADD CONSTRAINT cards_in_decks_cards_fk FOREIGN KEY (card_id) REFERENCES public.cards(id);
ALTER TABLE public.cards_in_decks ADD CONSTRAINT cards_in_decks_decks_fk FOREIGN KEY (deck_id) REFERENCES public.decks(id);


CREATE TABLE public.ready_to_battle (
    user_id int4 NULL
);
ALTER TABLE public.ready_to_battle ADD CONSTRAINT ready_to_battle_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id);


CREATE TABLE public.trading (
    id varchar(200) NOT NULL,
    card_id varchar(200) NULL,
    isactive bool DEFAULT true NULL,
    "type" varchar NULL,
    minimumdamage int4 NULL,
    CONSTRAINT trading_pk PRIMARY KEY (id)
);
---------------------------------------------------------------------------------------------------------------------------

INSERT into public.users (username, password, isadmin)
values ('admin', 'dGVzdA==', true);

INSERT into public.users (username, password)
values ('test', 'dGVzdA==');

INSERT into public.users (username, password)
values ('christian', 'dGVzdA==');

INSERT into public.users (username, password, coins)
values ('noMoney', 'dGVzdA==', 0);

INSERT into public.users (username, password, isadmin)
values ('admin2', 'dGVzdA==', true);

INSERT INTO public.currently_logged_in (username, token)
values ('test', 'test-mtcgToken');

INSERT INTO public.currently_logged_in (username, token)
values ('christian', 'christian-mtcgToken');

INSERT INTO public.currently_logged_in (username, token)
values ('admin', 'admin-mtcgToken');

INSERT INTO public.currently_logged_in (username, token)
values ('noMoney', 'noMoney-mtcgToken');

INSERT INTO public.packages
(created_by, is_bought)
VALUES(1, true);

INSERT INTO public.packages
(created_by, is_bought)
VALUES(2, false);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('644808c2-f87a-4600-b313-122b02322fd5', 'WaterGoblin', 9, 'water', 'monster', 2);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('f3fad0f2-a1af-45df-b80d-2e48825773d9', 'Ork', 45, 'normal', 'monster', 2);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('4ec8b269-0dfa-4f97-809a-2c63fe2a0025', 'Ork', 55, 'normal', 'monster', 2);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('88221cfe-1f84-41b9-8152-8e36c6a354de', 'WaterSpell', 22, 'water', 'spell', 2);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('951e886a-0fbf-425d-8df5-af2ee4830d85', 'Ork', 55, 'normal', 'monster', 3);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('a6fde738-c65a-4b10-b400-6fef0fdb28ba', 'FireSpell', 55, 'fire', 'spell', 3);

INSERT INTO public.cards
(id, name, damage, specialty, type, owned_by)
VALUES('a6fde738-c65a-4t10-b400-6fef0fdb28ba', 'Dragon', 55, 'fire', 'monster', 3);

INSERT INTO public.cards
(id, name, damage, specialty, type)
VALUES('74635fae-8ad3-4295-9139-320ab89c2844', 'FireSpell', 55, 'fire', 'spell');

INSERT INTO public.cards_in_packages
(card_id, package_id)
VALUES('644808c2-f87a-4600-b313-122b02322fd5', 1);

INSERT INTO public.cards_in_packages
(card_id, package_id)
VALUES('74635fae-8ad3-4295-9139-320ab89c2844', 2);

INSERT INTO public.decks
(user_id)
VALUES(2);

INSERT INTO public.decks
(user_id)
VALUES(3);

INSERT INTO public.cards_in_decks
(deck_id, card_id)
VALUES(1, '644808c2-f87a-4600-b313-122b02322fd5');

INSERT INTO public.cards_in_decks
(deck_id, card_id)
VALUES(2, '951e886a-0fbf-425d-8df5-af2ee4830d85');

INSERT INTO public.trading
(id, card_id, isactive, "type", minimumdamage)
VALUES('6cd85277-4590d-49d4-b0cf-ba0a9f1faad0', 'f3fad0f2-a1af-45df-b80d-2e48825773d9', true, 'spell', 50);