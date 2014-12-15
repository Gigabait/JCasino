package com.github.wyozi.jtexas.client.gamescript;

import com.github.wyozi.jtexas.client.MainClient;
import com.github.wyozi.jtexas.client.gamegui.GuiElement;
import com.github.wyozi.jtexas.client.gamescript.btnenums.ButtonManager;
import com.github.wyozi.jtexas.commonsg.net.NetInputStream;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Game implements MouseListener {

    protected final MainClient client;
    protected final ArrayList<GuiElement> guiElements = new ArrayList<GuiElement>();

    public Game(MainClient client) {
        this.client = client;
    }

    public final void render(Graphics g) {
        this.renderGame(g);
        for (GuiElement el : guiElements) {
            el.render(g);
        }
    }

    public abstract void renderGame(Graphics g);

    public abstract void updateGame(int delta);

    public abstract void handlePacket(final int opcode, final NetInputStream packet) throws IOException;

    public abstract ButtonManager getButtonManager();

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
    }


}
