package com.github.wyozi.jtexas.client;

import javax.swing.*;
import java.awt.*;

public class FrameViewer {
    public static void main(final String[] args) {
        final JFrame frame = new JFrame();
        final Dimension size = new Dimension(1100, 500);
        frame.setSize(size);
        frame.setMinimumSize(size);
        
        final JApplet applet = new MainClient();
        
        frame.add(applet);
        
        applet.setSize(size);
        
        applet.init();
        applet.start();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    frame.validate();
                    try {
                        Thread.sleep(20);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            
        }).start();
    }

}
