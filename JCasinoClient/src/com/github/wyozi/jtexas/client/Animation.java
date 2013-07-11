package com.github.wyozi.jtexas.client;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Animation {
    
    int i = 0;
    protected Point[] path;
    Point cur;
    Asset moveAsset;
    Asset finishAsset;
    
    Asset curAsset;
    
    public Animation (final Point from, final Point to, final Asset moveAsset, final Asset finishAsset) {
        
        path = makePath(from, to);
        this.moveAsset = moveAsset;
        this.finishAsset = finishAsset;
        this.curAsset = moveAsset;
        
    }
    
    public void render(final Graphics g) {
        if (path != null) {
            if (++i >= path.length) {
                cur = path[path.length-1];
                path = null;
                if (finishAsset != null) {
                    this.curAsset = finishAsset;
                }
            }
            else {
                cur = path[i];
            }
        }
        g.drawImage(curAsset.getImg(), cur.x, cur.y, null);
    }
    
    public Point[] makePath(final Point from, final Point to) {
        final ArrayList<Point> pathz = new ArrayList<Point>();
        final Point2D cur = (Point) from.clone();
        
        final double angle = Math.atan2(to.y - from.y, to.x - from.x);
        
        final float speed = 20;
        
        final int max = (int) (from.distance(to) / speed);
        
        int d = 0;
        
        do {
            pathz.add(new Point((int) cur.getX(), (int) cur.getY()));
            cur.setLocation(cur.getX() + Math.cos(angle)*speed,
                    cur.getY() + Math.sin(angle)*speed);
            d++;
        }
        while (d < max);
        pathz.add((Point) to.clone());
        return pathz.toArray(new Point[0]);
    }
    
    public boolean remove() {
        return false;
    }
}
