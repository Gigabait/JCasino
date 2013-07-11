package com.github.wyozi.jtexas.commons;


public enum Rank {
	
	DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;
	
    public static int getValue(final Rank suit) {
        for (int s = 0;s < values().length; s++) {
            if (values()[s] == suit)
                return s;
        }
        return -1;
    }
    
    public static Rank getRank(final int value) {
        return Rank.values()[value];
    }
}
