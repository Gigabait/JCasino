package com.github.wyozi.jtexas.client;

import java.awt.image.BufferedImage;

public class Asset {
    private final BufferedImage img;
    public Asset(final BufferedImage img2) {
        this.img = img2;
    }
    public BufferedImage getImg() {
        return this.img;
    }
}
