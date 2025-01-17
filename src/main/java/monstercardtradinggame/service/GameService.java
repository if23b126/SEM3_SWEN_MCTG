package monstercardtradinggame.service;

import httpserver.server.Request;
import httpserver.server.Response;

public interface GameService {
    Response createPackage(Request request);
    Response buyPackage(Request request);
    Response getCards(Request request);
    Response getDeck(Request request, Boolean asPlainString);
    Response putDeck(Request request);
    Response getStats(Request request);
    Response getScoreboards(Request request);
    Response battle(Request request);
    Response getTradings(Request request);
    Response createTrading(Request request);
    Response acceptTrading(Request request);
    Response deleteTrading(Request request);
}
