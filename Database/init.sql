--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2 (Debian 17.2-1.pgdg120+1)
-- Dumped by pg_dump version 17.2 (Ubuntu 17.2-1.pgdg24.04+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: cards; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.cards (
    id character varying(50) NOT NULL,
    name character varying(200),
    damage integer,
    specialty character varying(20),
    owned_by integer,
    type character varying(20)
);


ALTER TABLE public.cards OWNER TO mctg;

--
-- Name: cards_in_decks; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.cards_in_decks (
    deck_id integer,
    card_id character varying
);


ALTER TABLE public.cards_in_decks OWNER TO mctg;

--
-- Name: cards_in_packages; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.cards_in_packages (
    card_id character varying,
    package_id integer
);


ALTER TABLE public.cards_in_packages OWNER TO mctg;

--
-- Name: currently_logged_in; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.currently_logged_in (
    username character varying(200),
    token character varying(500)
);


ALTER TABLE public.currently_logged_in OWNER TO mctg;

--
-- Name: decks; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.decks (
    id integer NOT NULL,
    user_id integer
);


ALTER TABLE public.decks OWNER TO mctg;

--
-- Name: decks_id_seq; Type: SEQUENCE; Schema: public; Owner: mctg
--

ALTER TABLE public.decks ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.decks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: packages; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.packages (
    id integer NOT NULL,
    created_by integer,
    is_bought boolean DEFAULT false
);


ALTER TABLE public.packages OWNER TO mctg;

--
-- Name: pacakges_id_seq; Type: SEQUENCE; Schema: public; Owner: mctg
--

ALTER TABLE public.packages ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.pacakges_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: ready_to_battle; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.ready_to_battle (
    user_id integer
);


ALTER TABLE public.ready_to_battle OWNER TO mctg;


--
-- Name: trading; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.trading (
    id varchar(200) NULL,
    card_id varchar(200) NULL,
    isactive bool DEFAULT true NULL,
    "type" varchar NULL,
    minimumdamage int4 NULL,
    CONSTRAINT trading_unique UNIQUE (id)
);

ALTER TABLE public.trading ADD CONSTRAINT trading_cards_fk FOREIGN KEY (card_id) REFERENCES public.cards(id);


--
-- Name: users; Type: TABLE; Schema: public; Owner: mctg
--

CREATE TABLE public.users (
    id integer NOT NULL,
    username character varying(200),
    password character varying(200),
    image character varying(10),
    coins integer DEFAULT 20,
    isadmin boolean DEFAULT false,
    name character varying(200),
    bio character varying(200),
    wins integer DEFAULT 0,
    losses integer DEFAULT 0,
    ties integer DEFAULT 0,
    elo integer DEFAULT 1000
);


ALTER TABLE public.users OWNER TO mctg;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: mctg
--

ALTER TABLE public.users ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: cards; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.cards (id, name, damage, specialty, owned_by, type) FROM stdin;
\.


--
-- Data for Name: cards_in_decks; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.cards_in_decks (deck_id, card_id) FROM stdin;
\.


--
-- Data for Name: cards_in_packages; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.cards_in_packages (card_id, package_id) FROM stdin;
\.


--
-- Data for Name: currently_logged_in; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.currently_logged_in (username, token) FROM stdin;
\.


--
-- Data for Name: decks; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.decks (id, user_id) FROM stdin;
\.


--
-- Data for Name: packages; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.packages (id, created_by, is_bought) FROM stdin;
\.


--
-- Data for Name: ready_to_battle; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.ready_to_battle (user_id) FROM stdin;
\.


--
-- Data for Name: trading; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.trading (id, card_id, isactive) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: mctg
--

COPY public.users (id, username, password, image, coins, isadmin, name, bio, wins, losses, ties, elo) FROM stdin;
\.


--
-- Name: decks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mctg
--

SELECT pg_catalog.setval('public.decks_id_seq', 29, true);


--
-- Name: pacakges_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mctg
--

SELECT pg_catalog.setval('public.pacakges_id_seq', 87, true);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mctg
--

SELECT pg_catalog.setval('public.users_id_seq', 18, true);


--
-- Name: cards cards_pk; Type: CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT cards_pk PRIMARY KEY (id);


--
-- Name: decks decks_pkey; Type: CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.decks
    ADD CONSTRAINT decks_pkey PRIMARY KEY (id);


--
-- Name: packages pacakges_pk; Type: CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT pacakges_pk PRIMARY KEY (id);


--
-- Name: currently_logged_in pk_currently_logged_in_username; Type: CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.currently_logged_in
    ADD CONSTRAINT pk_currently_logged_in_username UNIQUE (username);


--
-- Name: trading trading_unique; Type: CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.trading
    ADD CONSTRAINT trading_unique UNIQUE (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: cards_in_decks cards_in_decks_cards_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.cards_in_decks
    ADD CONSTRAINT cards_in_decks_cards_fk FOREIGN KEY (card_id) REFERENCES public.cards(id);


--
-- Name: cards_in_decks cards_in_decks_decks_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.cards_in_decks
    ADD CONSTRAINT cards_in_decks_decks_fk FOREIGN KEY (deck_id) REFERENCES public.decks(id);


--
-- Name: cards_in_packages cards_in_packages_cards_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.cards_in_packages
    ADD CONSTRAINT cards_in_packages_cards_fk FOREIGN KEY (card_id) REFERENCES public.cards(id);


--
-- Name: cards_in_packages cards_in_packages_pacakges_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.cards_in_packages
    ADD CONSTRAINT cards_in_packages_pacakges_fk FOREIGN KEY (package_id) REFERENCES public.packages(id);


--
-- Name: cards cards_users_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.cards
    ADD CONSTRAINT cards_users_fk FOREIGN KEY (owned_by) REFERENCES public.users(id);


--
-- Name: packages pacakges_users_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT pacakges_users_fk FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: ready_to_battle ready_to_battle_users_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.ready_to_battle
    ADD CONSTRAINT ready_to_battle_users_fk FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: trading trading_cards_fk; Type: FK CONSTRAINT; Schema: public; Owner: mctg
--

ALTER TABLE ONLY public.trading
    ADD CONSTRAINT trading_cards_fk FOREIGN KEY (card_id) REFERENCES public.cards(id);


--
-- PostgreSQL database dump complete
--

