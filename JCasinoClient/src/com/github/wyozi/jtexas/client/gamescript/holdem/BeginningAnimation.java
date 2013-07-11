package com.github.wyozi.jtexas.client.gamescript.holdem;

import com.github.wyozi.jtexas.client.Animation;
import com.github.wyozi.jtexas.client.Asset;
import com.github.wyozi.jtexas.client.ClientCard;

import java.awt.*;

public class BeginningAnimation extends Animation{

    HeOtherPlayer player;
    
    public BeginningAnimation(final Point from, final Point to, final Asset moveAsset, final HeOtherPlayer player) {
        super(from, to, moveAsset, null);
        this.player = player;
    }
    
    boolean remove = false;

    @Override
    public void render(final Graphics g) {
        super.render(g);
        if (path == null && !remove) {
            if (player.hand == null) {
                player.hand = new ClientCard[1];
                remove = true;
            }
            else if (player.hand.length == 1) {
                player.hand = new ClientCard[2];
                if (player.queue != null) {
                    player.hand = player.queue;
                    player.queue = null;
                }
                remove = true;
            }
                
        }
    }
    
    @Override
    public boolean remove() {
        return remove;
    }
}
