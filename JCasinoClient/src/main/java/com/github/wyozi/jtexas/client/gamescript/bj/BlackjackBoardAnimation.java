package com.github.wyozi.jtexas.client.gamescript.bj;

import com.github.wyozi.jtexas.client.Animation;
import com.github.wyozi.jtexas.client.Asset;
import com.github.wyozi.jtexas.client.ClientCard;

import java.awt.*;

public class BlackjackBoardAnimation extends Animation {

    int boardIndex;
    ClientCard card;
    Blackjack game;

    public BlackjackBoardAnimation(final Point from, final Point to, final Asset moveAsset, final int boardIndex, final ClientCard card, Blackjack game) {
        super(from, to, moveAsset, null);
        this.boardIndex = boardIndex;
        this.card = card;
        this.game = game;
    }

    boolean remove = false;

    @Override
    public void render(final Graphics g) {
        super.render(g);
        if (path == null && !remove) {

            game.setBoardCard(boardIndex, card);
            remove = true;

        }
    }

    @Override
    public boolean remove() {
        return remove;
    }
}
