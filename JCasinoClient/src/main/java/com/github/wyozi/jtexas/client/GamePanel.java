package com.github.wyozi.jtexas.client;

import com.github.wyozi.jtexas.client.gamescript.Game;
import com.github.wyozi.jtexas.client.gamescript.Multiplayable;
import com.github.wyozi.jtexas.client.net.ClientPacketFactory;
import com.github.wyozi.jtexas.commons.net.RankLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public class GamePanel extends JPanel {

    public static final int GAMEPANEL_X_OFFSET = -300;
    public static final int GAMEPANEL_Y_OFFSET = -100;

    GameCanvas canvas;
    ChatPanel chat;
    ButtonPanel buttons;
    MainClient client;

    // Dimension canvasSize;

    Game game;

    public GamePanel(final MainClient client, final Game game, final int w, final int h) {

        this.game = game;

        this.client = client;

        canvas = new GameCanvas(client.createImage(client.getWidth() + GAMEPANEL_X_OFFSET, client.getHeight() + GAMEPANEL_Y_OFFSET));
        chat = new ChatPanel();
        buttons = new ButtonPanel();

        new Thread(canvas).start();

        setLayout(new BorderLayout());

        add(canvas, BorderLayout.CENTER);
        add(chat, BorderLayout.EAST);
        add(buttons, BorderLayout.SOUTH);

        canvas.addMouseListener(game);

    }

    public class GameCanvas extends Canvas implements Runnable {
        Image buffer;

        Dimension oldCanvasSize = new Dimension(0, 0);

        public GameCanvas(final Image buffer) {
            this.buffer = buffer;
        }

        public void resizeBuffer(final int w, final int h) {
            final BufferedImage resizedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g = resizedImage.createGraphics();
            g.drawImage(buffer, 0, 0, w, h, null);
            g.dispose();
            this.buffer = resizedImage;
        }

        @Override
        public void update(final Graphics g) {
            paint(g);
        }

        @Override
        public void paint(final Graphics g) {

            if (oldCanvasSize.width != client.getWidth() || oldCanvasSize.height != client.getHeight()) {
                resizeBuffer(client.getWidth() + GAMEPANEL_X_OFFSET, client.getHeight() + GAMEPANEL_Y_OFFSET);
            }

            final Graphics g2 = buffer.getGraphics();
            g.setColor(Color.white);
            g2.fillRect(0, 0, buffer.getWidth(null), buffer.getHeight(null));

            game.renderGame(g2);

            g2.dispose();

            g.drawImage(buffer, 0, 0, this);
            getToolkit().sync();

        }

        @Override
        public void run() {
            while (true) {

                game.updateGame(20); // TODO

                //if (getParent() != null) {
                repaint();
                //}

                try {
                    Thread.sleep(20);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public class ButtonPanel extends JPanel {
        Dimension size = new Dimension(300, 100);

        public ButtonPanel() {
            setSize(size);
            setMinimumSize(size);

            final List<Component> comps = game.getButtonManager().getComponents();

            setLayout(new GridLayout(2, (3 + comps.size()) / 2));

            for (final Component c : comps) {
                add(c);
            }
        }

    }

    public class ChatPanel extends JPanel {
        Dimension size = new Dimension(300, 300);

        DefaultListModel msgModel;
        JList msgList;

        JTextField chatBox;

        public ChatPanel() {
            setSize(size);
            setMinimumSize(size);

            setLayout(new BorderLayout());

            msgModel = new DefaultListModel();
            msgList = new JList(msgModel);
            msgList.setCellRenderer(new ColoredListCellRenderer());

            msgList.setFixedCellHeight(25);
            msgList.setFixedCellWidth(size.width);

            add(new JScrollPane(msgList), BorderLayout.CENTER);

            chatBox = new JTextField();

            chatBox.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                        try {
                            client.getNetClient().send(ClientPacketFactory.makeChatPacket(chatBox.getText()));
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        chatBox.setText("");
                    }
                }
            });

            add(chatBox, BorderLayout.SOUTH);
        }

        public void addMsg(final String msg, final byte level) {
            this.msgModel.add(0, new RankedMessage(RankLevel.getByLevel(level), msg));
            this.msgList.ensureIndexIsVisible(0);
        }
    }

    public Dimension getCanvasSize() {
        return new Dimension(client.getWidth() + GAMEPANEL_X_OFFSET, client.getHeight() + GAMEPANEL_Y_OFFSET);
    }

    public void setMaxIngamePlayers(final byte maxPlayers) {
        if (game instanceof Multiplayable) {
            ((Multiplayable) game).setMaxIngamePlayers(maxPlayers);
        }
    }
}