package com.github.wyozi.jtexas.client.gamescript;

import com.github.wyozi.jtexas.client.MainClient;
import com.github.wyozi.jtexas.client.gamescript.btnenums.AbstractBtnEnum;
import com.github.wyozi.jtexas.client.gamescript.btnenums.ButtonActionListener;
import com.github.wyozi.jtexas.client.gamescript.btnenums.ButtonManager;
import com.github.wyozi.jtexas.client.net.ClientPacketFactory;
import com.github.wyozi.jtexas.commons.net.NetClient;
import com.github.wyozi.jtexas.commons.net.io.NetInputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class SlotMachines extends Game implements ButtonActionListener {

    ButtonManager bm;

    public class Slot {

        int width = 100, height = 100;
        SlotElement element;

        public Slot() {
            this.element = spinner.elements[0];
        }

        public void render(Graphics g) {
            g.setColor(Color.black);
            g.drawRect(0, 0, width, height);
            element.render(g);
        }

        public void spin() {
            element = spinner.next(element);
        }

    }

    public class SlotSpinner {

        SlotElement[] elements;

        public SlotSpinner() {
            elements = new SlotElement[5];
            for (int i = 0; i < elements.length; i++) {
                elements[i] = new SlotElement();
                elements[i].stringRepresentation = "sup" + i;
            }
        }

        public SlotElement back(SlotElement old) {
            SlotElement lasty = null;
            for (int i = 0; i < elements.length; i++) {
                if (old == elements[i]) {
                    if (lasty == null)
                        break;
                    else
                        return lasty;
                }
                lasty = elements[i];
            }
            return elements[elements.length - 1];
        }

        public SlotElement next(SlotElement old) {
            boolean returnNext = false;
            for (int i = 0; i < elements.length; i++) {
                if (returnNext)
                    return elements[i];
                else if (elements[i] == old)
                    returnNext = true;
            }
            return elements[0];
        }

    }

    public class SlotElement {
        String stringRepresentation = "";

        public void render(Graphics g) {
            g.drawString(stringRepresentation, 0, 0);
        }
    }

    Slot[] slots;
    SlotSpinner spinner;

    public SlotMachines(MainClient client) {
        super(client);
        this.bm = new ButtonManager(this);
        this.bm.addButton(com.github.wyozi.jtexas.client.gamescript.btnenums.SlotmachineButton.ExitTable, "Exit table", true);

        this.spinner = new SlotSpinner();
        this.slots = new Slot[1];
        this.slots[0] = new Slot();
    }

    @Override
    public void renderGame(Graphics g) {
        int w = 50;
        for (int i = 0; i < slots.length; i++) {
            Slot slot = slots[i];

            g.translate(w, 50);
            slot.render(g);
            g.translate(-w, -50);

            w += slot.width + 50;
        }
    }

    @Override
    public void handlePacket(int opcode, NetInputStream packet)
            throws IOException {
    }

    @Override
    public ButtonManager getButtonManager() {
        return bm;
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        super.mousePressed(arg0);
    }

    @Override
    public void onPress(AbstractBtnEnum abn, JButton btn) {
        try {
            NetClient netClient = client.getNetClient();
            if (abn == com.github.wyozi.jtexas.client.gamescript.btnenums.SlotmachineButton.ExitTable) {
                netClient.send(ClientPacketFactory.makeSpectateTablePacket(-1));
                client.gotoServerList();
                netClient.send(ClientPacketFactory.makeRefreshTablesPacket());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateGame(int delta) {

    }

}
