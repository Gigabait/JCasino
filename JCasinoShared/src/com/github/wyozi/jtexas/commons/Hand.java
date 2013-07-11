package com.github.wyozi.jtexas.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Hand {
    Card[] hand;

    public Hand(final Card... hand) {
        this.hand = hand;
    }
    
    public boolean contains(final Card card) {
        for (final Card c : hand) {
            if (c == null) {
                continue;
            }
            if (c.suit == card.suit && c.rank == card.rank)
                return true;
        }
        return false;
    }
    
    public boolean containsRank(final Rank rank) {
        for (final Card c : hand) {
            if (c == null) {
                continue;
            }
            if (c.rank == rank)
                return true;
        }
        return false;
    }
    
    public boolean containsSuit(final Suit suit) {
        for (final Card c : hand) {
            if (c == null) {
                continue;
            }
            if (c.suit == suit)
                return true;
        }
        return false;
    }
    
    public void sort(final Comparator<Card> comparator) {
        Arrays.sort(hand, comparator);
    }
    
    public Card[] getCards() {
        return hand;
    }

    public void reverse() {
        final List<Card> handr = Arrays.asList(hand);
        Collections.reverse(handr);
        this.hand = handr.toArray(new Card[0]);
    }
}
