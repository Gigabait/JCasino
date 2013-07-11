package com.github.wyozi.jtexas.client.gamescript.bj;

import com.github.wyozi.jtexas.client.Animation;
import com.github.wyozi.jtexas.client.Asset;

import java.awt.*;

public class BjBeginningAnimation extends Animation{

    BjOtherPlayer player;
    
    public BjBeginningAnimation(final Point from, final Point to, final Asset moveAsset, final BjOtherPlayer player) {
        super(from, to, moveAsset, null);
        this.player = player;
    }
    
    boolean remove = false;

    @Override
    public void render(final Graphics g) {
        super.render(g);
        if (path == null && !remove) {
            remove = true;
        }
    }
    
    @Override
    public boolean remove() {
        return remove;
    }
}
