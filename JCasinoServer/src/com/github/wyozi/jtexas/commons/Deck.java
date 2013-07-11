package com.github.wyozi.jtexas.commons;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {
    
    private final ArrayList<Card> cards = new ArrayList<Card>();
    private final ArrayList<Card> usedCards = new ArrayList<Card>();
    
    public Deck() {
        shuffle();
    }
    
    public void shuffle() {
        
        cards.clear();
        usedCards.clear();
        
        for (final Suit suit : Suit.values()) {
            for (final Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        
        int shuffles = (int) (Math.random()*20);
        for (int i = 0;i < shuffles; i++)
        	Collections.shuffle(cards);
    }
    
    public Card pickFirst() {
        final Card card = cards.remove(0);
        usedCards.add(card);
        return card;
    }
}
