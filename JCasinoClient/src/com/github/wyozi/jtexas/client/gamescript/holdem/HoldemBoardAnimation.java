package com.github.wyozi.jtexas.client.gamescript.holdem;

import com.github.wyozi.jtexas.client.Animation;
import com.github.wyozi.jtexas.client.Asset;
import com.github.wyozi.jtexas.client.ClientCard;

import java.awt.*;

public class HoldemBoardAnimation extends Animation{

    int boardIndex;
    ClientCard card;
    HoldEm game;
    
    public HoldemBoardAnimation(final Point from, final Point to, final Asset moveAsset, final int boardIndex, final ClientCard card, HoldEm game) {
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
