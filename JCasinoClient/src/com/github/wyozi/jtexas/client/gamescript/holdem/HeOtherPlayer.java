package com.github.wyozi.jtexas.client.gamescript.holdem;

import com.github.wyozi.jtexas.client.ClientCard;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmAction;


public class HeOtherPlayer {

    public String name;
    public ClientCard[] hand = null;
    public ClientCard[] queue = null;
    public int chips;
    public boolean isDealer = false;
    public HoldEmAction act = null;

    public void tryToPutCards(final ClientCard[] cards) {
        if (hand == null || hand.length != 2) {
            queue = cards;
        } else {
            hand = cards;
        }

    }

    public HeOtherPlayer(final String name, final int chips) {
        this.name = name;
        this.chips = chips;
    }
}
