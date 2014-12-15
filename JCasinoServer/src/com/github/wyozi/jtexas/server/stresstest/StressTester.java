package com.github.wyozi.jtexas.server.stresstest;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTester {
    static AtomicInteger connects = new AtomicInteger(10000);

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (connects.get() > 0) {
                        try {
                            new Socket("74.63.212.136", 12424);
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
}
