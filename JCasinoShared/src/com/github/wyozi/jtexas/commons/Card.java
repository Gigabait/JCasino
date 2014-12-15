package com.github.wyozi.jtexas.commons;

public class Card {

    public Suit suit;
    public Rank rank;

    public Card(final Suit suit, final Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public static byte toByte(final Card card) {
        int rValue = Rank.getValue(card.rank);
        final byte ret = (byte) (rValue |= Suit.getValue(card.suit) << 5);
        return ret;
    }

    public static Card toCard(final byte b) {
        return new Card(Suit.getSuit((b >> 5) & 7), Rank.getRank(b & 31));
    }


}
