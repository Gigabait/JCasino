package com.github.wyozi.jtexas.client;

import com.github.wyozi.jtexas.client.gamescript.Game;
import com.github.wyozi.jtexas.client.net.ClientPacketFactory;
import com.github.wyozi.jtexas.client.net.ClientPacketHandler;
import com.github.wyozi.jtexas.commonsg.net.NetClient;
import org.mindrot.jBCrypt.BCrypt;

import javax.swing.*;
import java.applet.AppletStub;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class MainClient extends JApplet implements AppletStub {

    public final static short PROTOCOL_VERSION = 3;

    private AssetLoader assets;

    //int width;
    //int height;

    Properties properties;

    Container c;

    public GamePanel game;
    LoginPanel login;
    ServerListPanel serverList;

    private NetClient netClient;
    ClientPacketHandler netHandler;

    @Override
    public void init() {

        //width = getSize().width;
        //height = getSize().height;

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
        }

        properties = new Properties();
        try { // First try loading from inside jar
            properties.load(this.getClass().getClassLoader().getResourceAsStream("config.cfg"));
        } catch (Exception ignored) {
            // Try loading from a file in /
            File f = new File("config.cfg");
            try {
                properties.load(new FileReader(f));
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                showError("Couldn't find config file! Searched from " + new File(".").getAbsolutePath());
                stop();
                return;
            }
        }

        c = getContentPane();
        gotoLogin();

        this.assets = new AssetLoader(this);
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    getAssets().loadFromInputstream(new URL("http://dl.dropbox.com/u/18458187/rdtbeta/assets.zip").openStream());
                } catch (final Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                    return;
                }
            }

        }).start();

        setStub(this);
    }

    String myName;

    public void startSocket(final String user, String pass) {
        String pHash;

        this.myName = user;

        try {
            pHash = BCrypt.hashpw(pass, BCrypt.gensalt());
            pass = null;
        } catch (final Exception e) {
            showError("Error while hashing: " + e.getMessage());
            return;
        }

        String host = properties.getProperty("ip");
        int port = 12424;
        if (host == null) {
            showError("Missing 'ip' in config file!");
            return;
        }
        if (host.contains(":")) {
            String[] spl = host.split(":");
            host = spl[0];
            port = Integer.parseInt(spl[1]);
        }

        netHandler = new ClientPacketHandler(this);
        setNetClient(new NetClient(host, port, netHandler));
        try {
            getNetClient().connect();
        } catch (final IOException e) {
            showError("Error while connecting to server: " + e.getMessage());
            setNetClient(null);
            return;
        }

        try {
            getNetClient().send(
                    ClientPacketFactory.makeLoginPacket(user, pHash));
        } catch (final IOException e) {
            showError("Error while trying to send loginPacket");
            return;
        }

    }

    public void gotoLogin() {
        if (login == null) {
            login = new LoginPanel(this);
        }
        this.setContent(login);
        cleanGame();
        setNetClient(null);
    }

    public void gotoServerList() {
        if (serverList == null) {
            serverList = new ServerListPanel(this);
        }
        this.setContent(serverList);
        cleanGame();
    }

    public void cleanGame() {
        game = null; // TODO
    }

    public void showError(final String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
        System.err.println("Error: " + msg);
    }

    public void showInfo(final String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
        System.out.println("Info: " + msg);
    }

    public void addChatMsg(final String msg, final byte chatLevel) {
        if (game != null) {
            game.chat.addMsg(msg, chatLevel);
        }
    }

    public void setContent(final Component comp) {
        c.removeAll();
        c.add(comp);

        c.validate();
        c.repaint();
        validate();
        repaint();
    }

    public void repaint() {
        super.repaint();
        /*if (this.getWidth() != this.width || this.getHeight() != this.height) {
			appletResizeNotify(this.getWidth(), this.getHeight());
		}*/
    }


    public void setLoginStatus(final String msg) {
        login.setStatus(msg);
    }

    public void openGame(final Game game2, final byte maxPlayers) {
        game = new GamePanel(this, game2, getWidth(), getHeight());

        this.game.setMaxIngamePlayers(maxPlayers);
        this.setContent(this.game);
    }

    public void appletResizeNotify(final int w, final int h) {
        if (game != null) {
            //game.setCanvasSize(w, h);
        }
    }

    @Override
    public void resize(Dimension d) {
        super.resize(d);
        appletResizeNotify(d.width, d.height);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        appletResizeNotify(width, height);
    }

    public void setTables(final List<Table> tables) {
        if (serverList != null) {
            serverList.refresh(tables);
        }
    }

    public AssetLoader getAssets() {
        return assets;
    }

    public Object getMyName() {
        return this.myName;
    }

    private void setNetClient(final NetClient netClient) {
        this.netClient = netClient;
    }

    public NetClient getNetClient() {
        return netClient;
    }

    public Game getGame() {
        if (game == null)
            return null;
        return game.game;
    }

    @Override
    public void appletResize(int width, int height) {
        appletResizeNotify(width, height);
    }

}
