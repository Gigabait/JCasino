package com.github.wyozi.jtexas.client;

import com.github.wyozi.jtexas.commons.Rank;
import com.github.wyozi.jtexas.commons.Suit;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AssetLoader {
    private final Asset[][] cards = new Asset[4][13];
    private final Asset[] jokers = new Asset[2];
    private final HashMap<String, Asset> otherAssets = new HashMap<String, Asset>();
    private Asset back = null;
    
    private static Suit[] suits;
    private static Rank[] ranks;
    
    private final MainClient client;
    
    public AssetLoader(final MainClient client) {
        this.client = client;
    }
    
    static {
        suits = new Suit[Suit.values().length];
        ranks = new Rank[Rank.values().length];
        for (int i = 0;i < suits.length; i++) {
            suits[i] = Suit.values()[i];
        }
        for (int i = 0;i < ranks.length; i++) {
            ranks[i] = Rank.values()[i];
        }
    }
    
    public byte[] getBytes(final InputStream is) throws IOException {
        final ByteArrayOutputStream bais = new ByteArrayOutputStream();
        try {
          final byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
          int n;

          while ( (n = is.read(byteChunk)) > 0 ) {
            bais.write(byteChunk, 0, n);
          }
        }
        catch (final IOException e) {
          System.err.printf ("Failed while reading bytes from inputstream: %s", e.getMessage());
          e.printStackTrace ();
          // Perform any other exception handling that's appropriate.
        }
        finally {
          if (is != null) { is.close(); }
        }
        return bais.toByteArray();
    }
    
    public void loadFromInputstream(final InputStream is) throws MalformedURLException, IOException {

        final byte[] getZip = getBytes(is);
        final ByteArrayInputStream bais = new ByteArrayInputStream(getZip);
        
        final ZipInputStream zis = new ZipInputStream(bais);
        
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            final byte[] buffer = new byte[512];
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int length;
            while ((length = zis.read(buffer)) > -1) {
                baos.write(buffer, 0, length);
            }
            
            final ByteArrayInputStream bais2 = new ByteArrayInputStream(baos.toByteArray());
            
            final String fs = "/";
            
            final String fullname = entry.getName();
            final String filename = fullname.substring(fullname.lastIndexOf(fs)+1);
            System.out.println("Unpacking " + fullname);
            
            if (fullname.startsWith("assets" + fs + "cards" + fs + "back" + fs)) {
                if (filename.equals("b1fv.png")) {
                    this.back = new Asset(ImageIO.read(bais2));
                }
            }
            else if (fullname.startsWith("assets" + fs + "cards" + fs)) {
                if (filename.contains("_")) {
                    final String[] rawspl = filename.replace(".png", "").split("_");
                    final int suit = Integer.parseInt(rawspl[0]);
                    final int rank = Integer.parseInt(rawspl[1]);
                    if (suit == 4) {
                        jokers[rank] = new Asset(ImageIO.read(bais2));
                    } else {
                        cards[suit][12-rank /* XXX reverse*/] = new Asset(ImageIO.read(bais2));
                    }
                }
            }
            else {
                if (!fullname.contains("cards")) {
                    this.otherAssets.put(filename.replace(".png", ""), new Asset(ImageIO.read(bais2)));
                }
            }
            
            //ImageIO.read();
        }
        
        zis.close();
        /*
        System.out.println(new URL(client.getDocumentBase(), "assets/cards/back/b1fv.png").getPath());
        
        for (int s = 0;s < cards.length; s++) {
            final Asset[] suit = cards[s];
            for (int r = 0; r < suit.length; r++) {
                
                final BufferedImage img = ImageIO.read(new URL(client.getDocumentBase(), "assets/cards/" + s + "_" + r + ".png"));
                
                suit[r] = new Asset(img);
            }
        }

        BufferedImage img = ImageIO.read(new URL(client.getDocumentBase(), "assets/cards/back/b1fv.png"));
        back = new Asset(img);
        
        img = ImageIO.read(new URL(client.getDocumentBase(), "assets/dealer.png"));
        otherAssets.put("dealer", new Asset(img));
        */
        
        /*
        final File folder = new File("assets/cards");
        for (final File f : folder.listFiles()) {
            String name = f.getName();
            name = name.replace(".png", "");
            if (!name.contains("_")) {
                continue;
            }
            final String[] spl = name.split("_");
            
            final int suit = Integer.parseInt(spl[0]);
            final int rank = Integer.parseInt(spl[1]);
            
            final BufferedImage img = ImageIO.read(f);
            if (suit == 4) {
                jokers[rank] = new Asset(img);
            } else {
                cards[suit][rank] = new Asset(img);
            }
        }
        */
    }
    
    public Asset getBySR(final Suit suit, final Rank rank) {
        int s = 0;
        while (!suit.equals(suits[s])) {
            s++;
        }
        int r = 0;
        while (!rank.equals(ranks[r])) {
            r++;
        }
        return cards[s][r];
    }
    
    public Asset getByIndex(final int suit, final int rank) {
        return cards[suit][rank];
    }
    
    public Asset getAsset(final String id) {
        return otherAssets.get(id);
    }
    
    public Asset getBack() {
        return this.back;
    }

    public Asset getByCard(final ClientCard card) {
        return getBySR(card.suit, card.rank);
    }
}

