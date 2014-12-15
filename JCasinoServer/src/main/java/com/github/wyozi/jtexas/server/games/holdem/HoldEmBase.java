package com.github.wyozi.jtexas.server.games.holdem;

import com.github.wyozi.jtexas.commons.Card;
import com.github.wyozi.jtexas.commons.Deck;
import com.github.wyozi.jtexas.commons.Rank;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;
import com.github.wyozi.jtexas.commons.net.RankLevel;
import com.github.wyozi.jtexas.commons.net.games.HoldEmAction;
import com.github.wyozi.jtexas.server.*;
import com.github.wyozi.jtexas.server.chiphandler.ChipHandler;
import com.github.wyozi.jtexas.server.games.GameBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class HoldEmBase extends GameBase {

    HoldemInstance ins = null;
    ArrayList<MyServerClient> queueTableAdd = new ArrayList<MyServerClient>();

    ChipHandler chipHandler;
    HoldEmGameState state = HoldEmGameState.BetweenGameBreak;
    long breakStarted = 0;
    int breakTime = 15000;

    MyServerClient lastDealer = null;

    public final int MAX_THINKING_TIME = 60000;
    public final int SMALL_BLIND = 12;
    public final int MIN_CHIPS_TO_JOIN = SMALL_BLIND * 2 + 1;

    public HoldEmBase(DBToolkit db) {
        super(db);
    }

    @Override
    public boolean attemptToAddTablePlayer(MyServerClient client) throws IOException {
        final int fs = getFreeTableSpot();
        if (isInTable(client)) {
            client.send(ServerPacketFactory.makeInfoPacket("You're already in the table"));
        } else if (fs == -1) {
            client.send(ServerPacketFactory.makeChatPacket("Unfortunately table places are filled already.", RankLevel.Server));
        } else if (chipHandler.getChipAmount(client) < MIN_CHIPS_TO_JOIN) {
            client.send(ServerPacketFactory.makeInfoPacket("You dont have enough chips to join the table (minimum " + MIN_CHIPS_TO_JOIN + " needed)"));
        } else if (!queueTableAdd.contains(client)) {
            queueTableAdd.add(client);
            client.send(ServerPacketFactory.makeJoinTablePacket(false));
            return true;
        }
        return false;
    }

    public void kickFromTable(final MyServerClient client) {
        removeTablePlayer(client);
        try {
            client.send(ServerPacketFactory.makeInfoPacket("You have been removed from table due to lack of chips"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public int getBetIndexOf(final MyServerClient clientz) {
        if (ins == null || ins.playerTurnList == null)
            return -1;

        int i = 0;
        for (final MyServerClient client : ins.playerTurnList) {
            if (client == clientz)
                return i;
            i++;
        }
        return -1;
    }

    public boolean removeTablePlayer(final MyServerClient client) {
        for (int i = 0; i < tablePlayers.length; i++) {
            if (tablePlayers[i] == client) {
                tablePlayers[i] = null;
                try {
                    table.broadcast(ServerPacketFactory.makeSeatLeftPacket((byte) i));
                    client.send(ServerPacketFactory.makeLeaveTableSeatPacket());
                } catch (final IOException e) {
                    // TODO apparently player quit so no need to spam stderr with this
                }
                debug("Removed table player #" + i);
                return true;
            }
        }
        return false;
    }

    public boolean isInTable(final MyServerClient client2) {
        return getSeatOf(client2) != -1;
    }

    public int getSeatOf(final MyServerClient client) {
        for (int i = 0; i < tablePlayers.length; i++) {
            if (tablePlayers[i] == client)
                return i;
        }
        return -1;
    }

    public int getFreeTableSpot() {
        for (int i = 0; i < tablePlayers.length; i++) {
            if (tablePlayers[i] == null)
                return i;
        }
        return -1;
    }

    public MyServerClient firstPlayer() {
        for (final MyServerClient client : tablePlayers) {
            if (client != null)
                return client;
        }
        return null;
    }

    public MyServerClient nextPlayer(final MyServerClient cur) {
        boolean itsNext = false;
        for (final MyServerClient client : tablePlayers) {
            if (itsNext) {
                if (client == null) {
                    continue;
                }
                return client;
            }
            if (client == cur) {
                itsNext = true;
            }
        }
        return firstPlayer();
    }

    public void startBettingRound(final MyServerClient starter, final ArrayList<MyServerClient> clients, final int bet) {

        debug("Starting betting round with initial bet of " + bet);

        final MyServerClient[] turnList = new MyServerClient[clients.size()];
        turnList[0] = starter;


        for (int i = 0; i < turnList.length; i++) {
            final MyServerClient nextPlayer = nextPlayer(turnList[i]);
            if (nextPlayer == null || i >= turnList.length - 1) {
                break;
            }
            turnList[i + 1] = nextPlayer;
        }

        ins.bet = bet; // TODO

        ins.roundDone = false;
        ins.playerTurnList = turnList;
        ins.paidThisRound = new int[turnList.length];
        ins.raises.clear();
        ins.firstBetRound = true;
        ins.playerTurn = 0;
        ins.playerTurnStarted = System.currentTimeMillis();
        if (!ins.allIns.contains(starter) && !ins.folds.contains(starter)) {
            try {
                starter.send(ServerPacketFactory.makeTurnChangedPacket((byte) -1, ins.bet, true));
                table.broadcastAllButOne(ServerPacketFactory.makeTurnChangedPacket((byte) getSeatOf(starter), ins.bet, true), starter);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            nextPlayerTurn();
        }
    }

    @Override
    public void readDoAction(MyServerClient client, NetInputStream input) throws IOException {
        final byte id = input.readByte();
        final int seat = getSeatOf(client);
        final short bid = input.readShort();
        if (bid < 0) {
            client.kick("Exploit detected");
            log_error("Tried negative exploit " + bid, client);
        }
        if (seat == -1) {
            log_error("DO_ACTION when not in table", client);
            client.kick("Not in table");
        } else if (ins == null) {
            log_error("DO_ACTION when ins null", client);
            client.kick("Possible hacking try detected");
        } else if (ins.playerTurn != getBetIndexOf(client)) {
            log_error("DO_ACTION when not my turn realTurn:" + ins.playerTurn + " betIndex:" + getBetIndexOf(client), client);

            //table.broadcast(ServerPacketFactory.makeChatPacket("DO_ACTION when not my turn realTurn:" + ins.playerTurn + " seat:" + seat, RankLevel.Server));
            client.kick("Not your turn");
        } else {
            final HoldEmAction act = HoldEmAction.getAction(id);

            final int i = ins.playerTurn;
            final int leftToPay = ins.bet - ins.paidThisRound[i];
            if (act != null) {
                if (act == HoldEmAction.AllIn) {

                    // TODO allow player to press all in button

                    ins.allIns.add(client);
                    final int amnt = chipHandler.getChipAmount(client);
                    ins.pot += amnt;
                    ins.bet += amnt;
                    ins.paidThisRound[i] += amnt;
                    chipHandler.removeChips(client, amnt, "All in", seat);

                    ins.raises.add(client);

                } else if (act == HoldEmAction.Call) {
                    if (leftToPay > chipHandler.getChipAmount(client)) {
                        kickFromTable(client);
                        return;
                    }
                    ins.pot += leftToPay;
                    ins.paidThisRound[i] += leftToPay;
                    chipHandler.removeChips(client, leftToPay, "Call", seat);
                } else if (act == HoldEmAction.Check) {
                    if (leftToPay > 0) {
                        client.kick("Can't check if leftToPay > 0");
                    }
                } else if (act == HoldEmAction.Fold) {
                    ins.folds.add(client);
                    // TODO
                } else if (act == HoldEmAction.Bet || act == HoldEmAction.Raise) {
                    if (ins.raises.contains(client))
                        return; // TODO
                    if (leftToPay > 0) {
                        if (bid + leftToPay > chipHandler.getChipAmount(client)) {
                            kickFromTable(client);
                            return;
                        }
                        ins.bet += bid;
                        ins.pot += leftToPay + bid;
                        ins.paidThisRound[i] += bid + leftToPay;
                        chipHandler.removeChips(client, bid + leftToPay, act.name(), seat);
                    } else {
                        if (bid > chipHandler.getChipAmount(client)) {
                            kickFromTable(client);
                            return;
                        }

                        ins.pot += bid;
                        chipHandler.removeChips(client, bid, act.name(), seat);
                        ins.paidThisRound[i] += bid;
                        ins.bet = bid;
                    }
                    ins.raises.add(client);
                }
                log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.ACTION + act.name());
                table.broadcast(ServerPacketFactory.makeActionDonePacket((byte) seat, act));
                table.broadcast(ServerPacketFactory.makeChatPacket(client.getName() + " did " + act.name() + "; Pot is " + ins.pot, RankLevel.Server));
                nextPlayerTurn();
            }
        }

    }


    public class HoldemInstance {
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
        public ArrayList<MyServerClient> folds = new ArrayList<MyServerClient>();
        public MyServerClient[] playerTurnList = null;
        public int[] paidThisRound = null;
        public int playerTurn = -1;
        public long playerTurnStarted = 0;

        public boolean roundDone = false;

    }

    public void possiblyEndRound() {

        debug("Last bet of round was " + ins.bet);
        for (int i = 0; i < ins.paidThisRound.length; i++) {
            final MyServerClient client = ins.playerTurnList[i];
            debug(ins.paidThisRound[i] + " was bet of " + client.getName());
            if (client == null || ins.allIns.contains(client) || !isInTable(client)) {
                continue;
            }
            if (ins.paidThisRound[i] < ins.bet) {
                ins.playerTurn = i;
                ins.firstBetRound = false;
                debug("Starting round from " + client.getName() + "(" + i + ") again");
                try {
                    client.send(ServerPacketFactory.makeTurnChangedPacket((byte) -1, ins.bet - ins.paidThisRound[i], !ins.raises.contains(client)));
                    table.broadcastAllButOne(ServerPacketFactory.makeTurnChangedPacket((byte) getSeatOf(client), ins.bet - ins.paidThisRound[i], !ins.raises.contains(client)), client);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        ins.playerTurn = -1;
        ins.playerTurnList = null;
        ins.paidThisRound = null;
        ins.firstBetRound = true;
        ins.raises.clear();
        ins.playerTurnStarted = 0;
        ins.roundDone = true;
        debug("Round done");
        try {
            table.broadcast(ServerPacketFactory.makeTurnChangedPacket((byte) -2, ins.bet, true));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        //XXX

    }

    public void nextPlayerTurn() {

        if (ins.playerTurnList.length <= ins.playerTurn) {

            possiblyEndRound();

            return;
        }

        do {
            ins.playerTurn++;
            if (ins.playerTurnList.length - 1 <= ins.playerTurn) {
                break;
            }
        }
        while (ins.playerTurnList[ins.playerTurn] == null || ins.allIns.contains(ins.playerTurnList[ins.playerTurn]) || ins.folds.contains(ins.playerTurnList[ins.playerTurn]) || (ins.paidThisRound[ins.playerTurn] == ins.bet && !ins.firstBetRound));

        MyServerClient client = ins.playerTurnList.length <= ins.playerTurn ? null : ins.playerTurnList[ins.playerTurn];

        if (ins.playerTurnList.length <= ins.playerTurn || client == null || ins.folds.contains(client) || ins.allIns.contains(client)) {

            possiblyEndRound();

            return;
        }
        debug("New turn: " + ins.playerTurn + "(" + ins.playerTurnList[ins.playerTurn].getName() + ")");
        ins.playerTurnStarted = System.currentTimeMillis();
        try {
            client.send(ServerPacketFactory.makeChatPacket("You got " + (MAX_THINKING_TIME / 1000) + " seconds to play", RankLevel.Server));
            client.send(ServerPacketFactory.makeTurnChangedPacket((byte) -1, ins.bet, !ins.raises.contains(client)));
            table.broadcastAllButOne(ServerPacketFactory.makeTurnChangedPacket((byte) getSeatOf(client), ins.bet - ins.paidThisRound[ins.playerTurn], !ins.raises.contains(client)), client);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gameLoop() {
        if (state == HoldEmGameState.BetweenGameBreak && ins == null) {
            while (queueTableAdd.size() > 0) {
                final MyServerClient client = queueTableAdd.remove(0);
                if (!client.isOnline()) {
                    continue;
                }
                final int fs = getFreeTableSpot();
                if (fs != -1) {
                    tablePlayers[fs] = client;
                    try {
                        table.broadcast(ServerPacketFactory.makeSeatJoinedPacket((byte) fs, client, chipHandler.getChipAmount(client)));
                        client.send(ServerPacketFactory.makeJoinTablePacket(true));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        client.send(ServerPacketFactory.makeChatPacket("Unfortunately table places were filled before you could join.", RankLevel.Server));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        final ArrayList<MyServerClient> clients = new ArrayList<MyServerClient>();
        for (final MyServerClient client : tablePlayers) {
            if (client != null && (ins == null || !ins.folds.contains(client))) {
                clients.add(client);
            }
        }
        if (clients.size() < 2 || (state == HoldEmGameState.BetweenGameBreak && breakStarted > System.currentTimeMillis() - breakTime)) {
            if (ins != null) {
                final MyServerClient client = clients.size() > 0 ? clients.get(0) : null;
                if (client != null) {
                    chipHandler.addChips(client, ins.pot, "gamewin", getSeatOf(client));
                    for (final Card c : ins.board) {
                        if (c == null) {
                            break;
                        }
                        log_addGameEvent(ins.startTime, "endgame", Card.toByte(c) + ">");
                    }
                    for (final MyServerClient clientz : clients) {
                        final Card[] hand = clientz.getHand();
                        if (hand == null || hand.length != 2 || hand[0] == null || hand[1] == null) {
                            continue;
                        }
                        log_addGameEvent(ins.startTime, "endgame", clientz.getName() + ">" + Card.toByte(hand[0]) + ">" + Card.toByte(hand[1]) + "|");
                    }
                    log_addGameEvent(ins.startTime, "winner", client.getName());
                    log_addGameEvent(ins.startTime, "winamount", ins.pot + "");

                    try {
                        table.broadcast(ServerPacketFactory.makeCleanupPacket());
                        table.broadcast(ServerPacketFactory.makeChatPacket(client.getName() + " was only one to not fold", RankLevel.Server));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }

                }
                ins = null;
                this.breakStarted = System.currentTimeMillis();
            }
            state = HoldEmGameState.BetweenGameBreak;
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }

        if (ins != null && ins.dealer != null && !ins.dealer.isOnline()) {
            if (lastDealer == null) {
                ins.dealer = firstPlayer();
            } else {
                ins.dealer = nextPlayer(lastDealer);
            }
            try {
                table.broadcast(ServerPacketFactory.makeDealerChosenPacket((byte) getSeatOf(ins.dealer)));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.DEALER_CHOSEN + ins.dealer.getName() + "|");
        }

        if (ins != null && ins.playerTurnList != null) {
            // TODO below threw arrayindexoutofboundsexception because nextPlayer apparently raised playerTurnIndex over the max. fix or no?
            if (ins.playerTurnStarted != 0 && (ins.playerTurnList[ins.playerTurn] == null || ins.playerTurnStarted < System.currentTimeMillis() - MAX_THINKING_TIME)) {
                if (ins.playerTurnList[ins.playerTurn] != null) {
                    final MyServerClient removed = ins.playerTurnList[ins.playerTurn];
                    removeTablePlayer(removed);
                    try {
                        removed.send(ServerPacketFactory.makeInfoPacket("You have been removed from the table due to inactivity"));
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                nextPlayerTurn();
                return;
            }
        }

        if (ins != null && ins.roundDone) {
            ins.roundDone = false;
            if (state == HoldEmGameState.BetweenGameBreak) {
                state = HoldEmGameState.Flop;
            } else if (state == HoldEmGameState.Flop) {
                state = HoldEmGameState.Turn;
            } else if (state == HoldEmGameState.Turn) {
                state = HoldEmGameState.River;
            } else if (state == HoldEmGameState.River) {
                state = HoldEmGameState.Showdown;
            }
        }

        if (state == HoldEmGameState.BetweenGameBreak) {
            if (ins == null) {
                ins = new HoldemInstance();
            }
            if (ins.smallBlind == 0) {
                ins.smallBlind = SMALL_BLIND;
            }
            if (ins.startTime == 0) {
                ins.startTime = System.currentTimeMillis();
                log_addGame(ins.startTime);
                for (final MyServerClient client : clients) {
                    log_addGameEvent(ins.startTime, "players", client.getName() + "|");
                }
            }
            if (ins.dealer == null) {
                if (lastDealer == null) {
                    ins.dealer = firstPlayer();
                } else {
                    ins.dealer = nextPlayer(lastDealer);
                }
                lastDealer = ins.dealer;

                try {
                    table.broadcast(ServerPacketFactory.makeDealerChosenPacket((byte) getSeatOf(ins.dealer)));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            if (ins.deck == null) {
                ins.deck = new Deck();// TODO shuffle packet?
            }
            if (!ins.cardsShared) {
                try {
                    table.broadcast(ServerPacketFactory.makeShareCardsPacket());
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(clients.size() * 850);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                ins.cardsShared = true;
            }

            if (!ins.ownCardsRevealed) {
                int s = 0;
                for (final MyServerClient client : tablePlayers) {
                    if (client != null) {
                        final Card[] pHand = new Card[]{ins.deck.pickFirst(), ins.deck.pickFirst()};
                        client.setHand(pHand);
                        try {
                            client.send(ServerPacketFactory.makeRevealCardsPacket((byte) s, pHand[0], pHand[1]));
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                    }
                    s++;
                }
                ins.ownCardsRevealed = true;

            }

            if (ins.playerTurnList == null) {
                MyServerClient firstBlind = null;

                if (clients.size() < 3) {
                    final int dChips = chipHandler.getChipAmount(ins.dealer);
                    final MyServerClient dealer = ins.dealer;
                    if (dChips < ins.smallBlind) {
                        kickFromTable(dealer);
                        return;
                    }

                    firstBlind = dealer;
                } else {

                    final MyServerClient nPlayer = nextPlayer(ins.dealer);
                    if (nPlayer == null) {
                        return;
                    }
                    final int dChips = chipHandler.getChipAmount(nPlayer);
                    if (dChips < ins.smallBlind) {
                        kickFromTable(nPlayer);
                        return;
                    }

                    firstBlind = nPlayer;

                    // small blind from next player and big blind from nextnext player
                }

                log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.SMALL_BLIND + firstBlind.getName() + "|");
                ins.pot += ins.smallBlind;
                chipHandler.removeChips(firstBlind, ins.smallBlind, "smallblind", getSeatOf(firstBlind));

                final int bigBlind = ins.smallBlind * 2;

                MyServerClient bigBlinder = null;
                while (true) {
                    bigBlinder = nextPlayer(firstBlind);
                    if (bigBlinder == null) {
                        debug("no big blinder found");
                        return;
                    }
                    if (chipHandler.getChipAmount(bigBlinder) < bigBlind) {
                        kickFromTable(bigBlinder);
                        continue;
                    }
                    ins.pot += bigBlind;
                    chipHandler.removeChips(bigBlinder, bigBlind, "bigblind", getSeatOf(bigBlinder));
                    log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.BIG_BLIND + bigBlinder.getName() + "|");
                    break;
                }

                startBettingRound(clients.size() < 3 ? ins.dealer : nextPlayer(bigBlinder), clients, 0);

            }

            // small is 50% of big blind

            // TODO add turns here already

            // second+ raises must be equals to highest so far raise
            // If a raise or re-raise is all-in and does not equal the size of the previous raise, the initial raiser cannot re-raise again. This only matters of course if there were a call before the re-raise

        } else if (state == HoldEmGameState.Flop) {
            if (ins.boardCardsRevealed == 0) {
                ins.boardCardsRevealed = 3;

                final Card[] flop = new Card[3];

                for (int i = 0; i < flop.length; i++) {
                    ins.deck.pickFirst(); // traditionz of hold em
                    flop[i] = ins.deck.pickFirst();
                }

                ins.board[0] = flop[0];
                ins.board[1] = flop[1];
                ins.board[2] = flop[2];

                try {
                    table.broadcast(ServerPacketFactory.makeRevealFlopPacket(flop[0], flop[1], flop[2]));
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.FLOP + "|");

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                startBettingRound(ins.dealer, clients, 0); // TODO
            }
        } else if (state == HoldEmGameState.Turn) {
            if (ins.boardCardsRevealed == 3) {
                ins.boardCardsRevealed = 4;

                ins.deck.pickFirst();

                Card turn = ins.board[3] = ins.deck.pickFirst();

                try {
                    table.broadcast(ServerPacketFactory.makeRevealTurnPacket(turn));
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.TURN + "|");

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                startBettingRound(ins.dealer, clients, 0); // TODO
            }
        } else if (state == HoldEmGameState.River) {
            if (ins.boardCardsRevealed == 4) {
                ins.boardCardsRevealed = 5;

                ins.deck.pickFirst();

                Card river = ins.board[4] = ins.deck.pickFirst();

                try {
                    table.broadcast(ServerPacketFactory.makeRevealRiverPacket(river));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                log_addGameEvent(ins.startTime, "events", HoldEmLogOpcodes.RIVER + "|");

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                startBettingRound(ins.dealer, clients, 0); // TODO
            }
        } else if (state == HoldEmGameState.Showdown) {

            // TODO verify board is full

            final HashMap<MyServerClient, RankPair<CardValue, Rank[]>> showdowns = new HashMap<MyServerClient, RankPair<CardValue, Rank[]>>(clients.size());

            for (final MyServerClient client : clients) {
                final int s = getSeatOf(client);
                final Card[] pHand = client.getHand();
                try {
                    table.broadcast(ServerPacketFactory.makeRevealCardsPacket((byte) s, pHand[0], pHand[1]));
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                final RankPair<CardValue, Rank[]> cValue = CardValueCalculator.getValueOfHoldEmHand(pHand, ins.board);

                showdowns.put(client, cValue);

                try {
                    table.broadcast(ServerPacketFactory.makeChatPacket(client.getName() + " has " + cValue.one.name() + " (" + Arrays.toString(cValue.two) + ")", RankLevel.Server));
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(2000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }

            MyServerClient winner = null;
            RankPair<CardValue, Rank[]> winningCards = null;
            Rank kicker = null;

            final Rank[] boardRanks = new Rank[5];
            for (int i = 0; i < boardRanks.length; i++) {
                boardRanks[i] = ins.board[i].rank;
            }

            final ArrayList<MyServerClient> splitPot = new ArrayList<MyServerClient>();

            for (final Entry<MyServerClient, RankPair<CardValue, Rank[]>> entry : showdowns.entrySet()) {

                final MyServerClient client = entry.getKey();
                final RankPair<CardValue, Rank[]> cardValue = entry.getValue();

                if (winner == null) {
                    winner = client;
                    winningCards = cardValue;
                    splitPot.clear();
                    kicker = null;
                    continue;
                }

                if (cardValue.one.getValue() > winningCards.one.getValue()) {
                    debug(cardValue.one.name() + " is better than " + winningCards.one.name());
                    winner = client;
                    winningCards = cardValue;
                    splitPot.clear();
                    kicker = null;
                } else if (cardValue.one.getValue() == winningCards.one.getValue()) {
                    debug("Both have " + cardValue.one.name());
                    int winning = 2; // old winner

                    final Rank[] challengerCards = cardValue.two;
                    final Rank[] winnerCards = winningCards.two;

                    Arrays.sort(challengerCards, new BigRankSorter());
                    Arrays.sort(winnerCards, new BigRankSorter());

                    for (int il = 0; il < challengerCards.length; il++) {
                        if (Rank.getValue(challengerCards[il]) > Rank.getValue(winnerCards[il])) {
                            debug(challengerCards[il] + " is better than " + winnerCards[il]);
                            winning = 1;
                            break;
                        } else if (Rank.getValue(challengerCards[il]) == Rank.getValue(winnerCards[il])) {
                            debug(winnerCards[il] + " is same as " + challengerCards[il]);
                            winning = 0;
                            continue;
                        }

                        debug("Apparently " + winnerCards[il] + " is better than " + challengerCards[il]);
                        /*if (il == cardValue.two.length-1) {
                            winning = 0;
                            debug("Last card and still draw");
                        }*/
                    }
                    // TODO check if kicker checking works

                    if (winning == 0 && (cardValue.one == CardValue.Pair || cardValue.one == CardValue.TwoPairs || cardValue.one == CardValue.ThreeOfKind || cardValue.one == CardValue.FourOfKind)) {
                        Rank[] challengerWHand = CardValueCalculator.concat(boardRanks, challengerCards);
                        Rank[] winnerWHand = CardValueCalculator.concat(boardRanks, winnerCards);

                        Arrays.sort(challengerWHand, new BigRankSorter());
                        Arrays.sort(winnerWHand, new BigRankSorter());
                        if (Rank.getValue(winnerWHand[0]) > Rank.getValue(challengerWHand[0])) {
                            debug("Old winner has bigger kicker");
                            winning = 2;
                            kicker = winnerWHand[0];
                        } else if (Rank.getValue(winnerWHand[0]) < Rank.getValue(challengerWHand[0])) {
                            debug("Challenger has bigger kicker");
                            winning = 1;
                            kicker = challengerWHand[0];
                        }
                    }

                    if (winning == 0) {
                        splitPot.add(client);
                        kicker = null;
                    } else if (winning == 1) {
                        winner = client;
                        winningCards = cardValue;
                        splitPot.clear();
                    }
                }
            }

            splitPot.add(winner);

            try {
                if (splitPot.size() == 1) {
                    table.broadcast(ServerPacketFactory.makeChatPacket("The winner of this game is " + winner.getName() + (kicker != null ? "(kicker: " + kicker.name() + ")" : ""), RankLevel.Server));
                } else {
                    final StringBuffer msg = new StringBuffer("The pot is split between ");
                    for (final MyServerClient client : splitPot) {
                        msg.append(client.getName() + ", ");
                    }
                    table.broadcast(ServerPacketFactory.makeChatPacket(msg.toString(), RankLevel.Server));
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }

            final int potPerOne = ins.pot / splitPot.size();

            for (final MyServerClient client : splitPot) {
                chipHandler.addChips(client, potPerOne, "showdown win", getSeatOf(client));

                log_addGameEvent(ins.startTime, "winner", client.getName() + ",");
            }

            for (final Card c : ins.board) {
                if (c == null) {
                    break;
                }
                log_addGameEvent(ins.startTime, "endgame", Card.toByte(c) + ">");
            }
            for (final MyServerClient clientz : clients) {
                final Card[] hand = clientz.getHand();
                if (hand == null || hand.length != 2 || hand[0] == null || hand[1] == null) {
                    continue;
                }
                log_addGameEvent(ins.startTime, "endgame", clientz.getName() + ">" + Card.toByte(hand[0]) + ">" + Card.toByte(hand[1]) + "|");
            }

            log_addGameEvent(ins.startTime, "winamount", ins.pot + "");

            this.ins = null;
            this.state = HoldEmGameState.BetweenGameBreak;
            this.breakStarted = System.currentTimeMillis();

            try {
                Thread.sleep(6000);
            } catch (final InterruptedException e1) {
                e1.printStackTrace();
            }

            for (final MyServerClient client : clients) {
                if (chipHandler.getChipAmount(client) <= 0 && client.isOnline()) {
                    try {
                        client.send(ServerPacketFactory.makeInfoPacket("Sorry. You seem to have run out of chips"));
                        removeTablePlayer(client);
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                table.broadcast(ServerPacketFactory.makeCleanupPacket());
                table.broadcast(ServerPacketFactory.makeChatPacket("The next game starts in " + (breakTime / 1000) + "s", RankLevel.Server));
            } catch (final IOException e) {
                e.printStackTrace();
            }

        }


        try {
            Thread.sleep(1000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean allowSpectators() {
        return true;
    }

    @Override
    public int getPlayerCount() {
        int c = 0;
        for (MyServerClient client : tablePlayers) {
            if (client != null)
                c++;
        }
        return c;
    }

    @Override
    public byte getGameId() {
        return 1;
    }

    @Override
    public String getType() {
        return "Hold 'em";
    }

    @Override
    public void sendWelcomePacket(MyServerClient client) throws IOException {
        byte seat = 0;
        for (final MyServerClient client2 : tablePlayers) {
            if (client2 != null) {
                client.send(ServerPacketFactory.makeSeatJoinedPacket(seat, client2, chipHandler.getChipAmount(client2)));
            }
            seat++;
        }
    }

    public abstract void log_error(String error, MyServerClient client);

    public abstract void log_addGame(final long startTime);

    public abstract void log_addGameEvent(final long startTime, String field, final String data);

}
