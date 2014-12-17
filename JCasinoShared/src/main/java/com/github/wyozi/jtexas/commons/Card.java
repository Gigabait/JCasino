package com.github.wyozi.jtexas.commons;

public class Card {

    public Suit suit;
    public Rank rank;

    public Card(final Suit suit, final Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public byte toByte() {
        return (byte) (
                Rank.getValue(rank) | (Suit.getValue(suit) << 5)
        );
    }

    public static Card toCard(final byte b) {
        final Suit suit = Suit.getSuit((b >> 5) & 7);
        final Rank rank = Rank.getRank(b & 31);
        return new Card(suit, rank);
    }


}
