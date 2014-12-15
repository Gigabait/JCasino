package com.github.wyozi.jtexas.server;

import com.github.wyozi.jtexas.commons.Rank;

import java.util.Comparator;

public class BigRankSorter implements Comparator<Rank> {

    @Override
    public int compare(final Rank rank1, final Rank rank2) {
        return Rank.getValue(rank1) - Rank.getValue(rank2);
    }
}
