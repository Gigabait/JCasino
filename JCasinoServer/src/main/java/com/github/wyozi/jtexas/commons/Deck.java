package com.github.wyozi.jtexas.commons;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class Deck {
    private final Stack<Card> cardStack = new Stack<>();

    public Deck() {
        for (final Suit suit : Suit.values()) {
            for (final Rank rank : Rank.values()) {
                cardStack.add(new Card(suit, rank));
            }
        }

        shuffle();
    }

    public void shuffle() {
        SecureRandom random = new SecureRandom();
        Collections.shuffle(cardStack, random);
    }

    public Card pop() {
        return cardStack.pop();
    }
}
