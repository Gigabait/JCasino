package com.github.wyozi.jtexas.client;

import java.awt.*;

public class TempAnimation extends Animation{

    public TempAnimation(final Point from, final Point to, final Asset moveAsset) {
        super(from, to, moveAsset, null);
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
