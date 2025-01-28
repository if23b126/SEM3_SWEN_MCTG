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



INSERT into public.users (username, password, isadmin)
values ('admin', 'dGVzdA==', true);