package com.github.wyozi.jtexas.server;

import com.github.wyozi.jtexas.commons.Card;
import com.github.wyozi.jtexas.commons.Hand;
import com.github.wyozi.jtexas.commons.Rank;
import com.github.wyozi.jtexas.commons.Suit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;


public class CardValueCalculator {
    
    public static RankPair<CardValue, Rank[]> getValueOfHoldEmHand(final Card[] hand, final Card[] board) {

        final Hand whole = new Hand(concat(hand, board));
        whole.sort(new CardComparator());
        /*
        for (final Card c : whole.getCards()) {
            System.out.println(c.suit + " " + c.rank);
        }
        */
        final ArrayList<Card> straight = new ArrayList<Card>();
        final HashMap<Suit, ArrayList<Card>> flush = new HashMap<Suit, ArrayList<Card>>();
        Card lastCard = null;
        for (final Card c : whole.getCards()) {
            if (!flush.containsKey(c.suit)) {
                flush.put(c.suit, new ArrayList<Card>());
            }
            flush.get(c.suit).add(c);
            if (lastCard == null) {
                lastCard = c;
                continue;
            }
            if (c.rank.equals(lastCard.rank)) {
                continue;
            }
            else if (Rank.getValue(c.rank) == Rank.getValue(lastCard.rank)+1) {
                straight.add(c);
                if (!straight.contains(lastCard)) {
                    straight.add(lastCard);
                }
            }
            else if (straight.size() < 5) {
                straight.clear();
            }
            lastCard = c;
        }
        
        RankPair<CardValue, Rank[]> straightEntry = null;
        RankPair<CardValue, Rank[]> fullHouseEntry = null;
        RankPair<CardValue, Rank[]> threeOfKindEntry = null;
        RankPair<CardValue, Rank[]> pairEntry = null;
        
        if (straight.size() >= 5) {
            straightEntry = new RankPair<CardValue, Rank[]>(CardValue.Straight, new Rank[] {straight.get(0).rank});
            Suit goingSuit = null;
            int sfm = 0;
            for (int i = 0;i < straight.size(); i++) {
                if (goingSuit == null) {
                    goingSuit = straight.get(i).suit;
                }
                else if (straight.get(i).suit != goingSuit) {
                    if (straight.size() - (i+1) < 5) {
                        break; // no straight flush
                    }
                    sfm = 0;
                }
                else {
                    sfm++;
                }
                if (sfm == 5)
                    return new RankPair<CardValue, Rank[]>(CardValue.StraightFlush, new Rank[] {straight.get(0).rank});
                    // got a straight flush!
            }
        }
        
        final Card[][] multipleKinds = new Card[13][4];

        for (final Card c : whole.getCards()) {
            multipleKinds[Rank.getValue(c.rank)][Suit.getValue(c.suit)] = c;
        }
        
        Rank bigFullHouse = null;
        Rank smallFullHouse = null;
        
        Rank[] twoPairs = null;
        
        Rank highcard = null;
        Rank highcardKicker = null;
        
        // going from top to down because top cards are at beginning
        for (int rank = 0; rank < multipleKinds.length;  rank++) {
            final ArrayList<Card> cards = new ArrayList<Card>();
            for (final Card mk : multipleKinds[rank]) {
                if(mk == null) {
                    continue;
                }
                cards.add(mk);
            }
            if (cards.size() == 4)
                return new RankPair<CardValue, Rank[]>(CardValue.FourOfKind, new Rank[] {Rank.getRank(rank)});
            else if (cards.size() == 3) {
                bigFullHouse = Rank.getRank(rank);
                threeOfKindEntry = new RankPair<CardValue, Rank[]>(CardValue.ThreeOfKind, new Rank[] {bigFullHouse});
            }
            else if (cards.size() == 2) {
                smallFullHouse = Rank.getRank(rank);
                pairEntry = new RankPair<CardValue, Rank[]>(CardValue.Pair, new Rank[] {smallFullHouse});
                if (twoPairs == null) {
                    twoPairs = new Rank[2];
                    twoPairs[0] = smallFullHouse;
                }
                else if (twoPairs[1] == null) {
                    twoPairs[1] = smallFullHouse;
                }
                else {
                    int worseIndex = 0;
                    if (Rank.getValue(twoPairs[0]) > Rank.getValue(twoPairs[1])) {
                        worseIndex = 1;
                    }
                    
                    if (Rank.getValue(twoPairs[worseIndex]) < rank) {
                        twoPairs[worseIndex] = smallFullHouse;
                    }
                }
            }
            else if (cards.size() == 1) {
            	highcardKicker = highcard;
            	highcard = Rank.getRank(rank);
            }
        }
        
        if (smallFullHouse != null && bigFullHouse != null) {
            fullHouseEntry = new RankPair<CardValue, Rank[]>(CardValue.FullHouse, new Rank[] {bigFullHouse, smallFullHouse});
        }
        
        if (fullHouseEntry != null)
            return fullHouseEntry;
        
        for (final Entry<Suit, ArrayList<Card>> flushEntry : flush.entrySet()) {
            if (flushEntry.getValue().size() >= 5)
                return new RankPair<CardValue, Rank[]>(CardValue.Flush, new Rank[] {flushEntry.getValue().get(0).rank});
        }
        
        if (straightEntry != null)
            return straightEntry;
        
        if (threeOfKindEntry != null)
            return threeOfKindEntry;
        
        if (twoPairs != null && twoPairs[1] != null)
            return new RankPair<CardValue, Rank[]>(CardValue.TwoPairs, new Rank[] {twoPairs[0], twoPairs[1]});
        
        if (pairEntry != null)
            return pairEntry;
         
        return new RankPair<CardValue, Rank[]>(CardValue.High, new Rank[] {highcard, highcardKicker});
    }
    
    public static <T> T[] concat(final T[] first, final T[] second) {
        final T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
    
    public static void main(final String[] args) {
        final Card[] hand = new Card[] {
                new Card(Suit.HEARTS, Rank.EIGHT),
                new Card(Suit.DIAMONDS, Rank.KING)
        };
        final Card[] board = new Card[] {
                new Card(Suit.CLUBS, Rank.SEVEN),
                new Card(Suit.SPADES, Rank.THREE),
                new Card(Suit.CLUBS, Rank.JACK),
                new Card(Suit.CLUBS, Rank.TEN),
                new Card(Suit.SPADES, Rank.TEN),
                new Card(Suit.HEARTS, Rank.QUEEN),
        };
        System.out.println(getValueOfHoldEmHand(hand, board).one);
    }

}
class CardComparator implements Comparator<Card> {

    @Override
    public int compare(final Card card1, final Card card2) {
        final int card1val = Rank.getValue(card1.rank);
        final int card2val = Rank.getValue(card2.rank);
        return card1val - card2val;
    }
    
}
