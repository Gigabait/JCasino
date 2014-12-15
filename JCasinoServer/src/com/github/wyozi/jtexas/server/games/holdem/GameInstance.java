package com.github.wyozi.jtexas.server.games.holdem;

import com.github.wyozi.jtexas.commons.Card;
import com.github.wyozi.jtexas.commons.Deck;
import com.github.wyozi.jtexas.commons.Rank;
import com.github.wyozi.jtexas.commons.Suit;
import com.github.wyozi.jtexas.server.MyServerClient;

import java.util.ArrayList;

public class GameInstance {
    public MyServerClient dealer = null;
    public Deck deck = null;
    public boolean cardsShared = false;
    public Card[] board = new Card[5];
    public long startTime = 0;

    public int boardCardsRevealed = 0;
    public boolean ownCardsRevealed = false;

    public int pot = 0;
    public int smallBlind = 0;

    public int bet = 0;
    public boolean firstBetRound = false;

    public ArrayList<MyServerClient> raises = new ArrayList<MyServerClient>();
    public ArrayList<MyServerClient> allIns = new ArrayList<MyServerClient>();
    public MyServerClient[] playerTurnList = null;
    public int[] paidThisRound = null;
    public int playerTurn = -1;
    public long playerTurnStarted = 0;

    public boolean roundDone = false;
    public boolean lulzRound = false;
    public Card[] lrCards = new Card[]{new Card(Suit.HEARTS, Rank.ACE), new Card(Suit.HEARTS, Rank.KING), new Card(Suit.HEARTS, Rank.QUEEN), new Card(Suit.HEARTS, Rank.JACK), new Card(Suit.HEARTS, Rank.TEN)};
}
