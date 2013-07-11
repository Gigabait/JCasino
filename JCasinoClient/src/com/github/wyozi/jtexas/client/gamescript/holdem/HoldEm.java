package com.github.wyozi.jtexas.client.gamescript.holdem;

import com.github.wyozi.jtexas.client.Animation;
import com.github.wyozi.jtexas.client.Asset;
import com.github.wyozi.jtexas.client.ClientCard;
import com.github.wyozi.jtexas.client.MainClient;
import com.github.wyozi.jtexas.client.gamescript.Game;
import com.github.wyozi.jtexas.client.gamescript.Multiplayable;
import com.github.wyozi.jtexas.client.gamescript.btnenums.AbstractBtnEnum;
import com.github.wyozi.jtexas.client.gamescript.btnenums.ButtonActionListener;
import com.github.wyozi.jtexas.client.gamescript.btnenums.ButtonManager;
import com.github.wyozi.jtexas.client.gamescript.btnenums.HoldEmButton;
import com.github.wyozi.jtexas.client.net.ClientPacketFactory;
import com.github.wyozi.jtexas.commons.Card;
import com.github.wyozi.jtexas.commonsg.net.NetClient;
import com.github.wyozi.jtexas.commonsg.net.NetInputStream;
import com.github.wyozi.jtexas.commonsg.net.RankLevel;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmAction;
import com.github.wyozi.jtexas.commonsg.net.games.HoldEmOpcodes;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class HoldEm extends Game implements ButtonActionListener, Multiplayable {

	ButtonManager bm = new ButtonManager(this);
	
	JLabel callAmount = new JLabel("");
	
    JSlider raiseSlider = new JSlider(0, 0, 0);
    JLabel raiseChips = new JLabel("");
    
    // game vars
    
    ArrayList<Animation> visibleCards = new ArrayList<Animation>();
    boolean attending = false;
    
    int maxIngamePlayers = 0;
    HeOtherPlayer[] tablePlayers = null;
    
    ClientCard[] board = new ClientCard[5];
    int wholePot = 0;
    
    byte turn = -1;
    boolean myTurn = false;
    int bid = 0;
    
    boolean allowRaises = false;
	
	public HoldEm(MainClient client) {
		
		super(client);
		
		bm.addBeforeComponent(callAmount);
		
		bm.addButton(HoldEmButton.JoinTable, "Join table", true);
		bm.addButton(HoldEmButton.LeaveTable, "Leave table", false);
		bm.addButton(HoldEmButton.ExitTable, "Exit table", true);
		
		bm.addButton(HoldEmButton.Check, "Check", false);
		bm.addButton(HoldEmButton.AllIn, "All-in", false);
		bm.addButton(HoldEmButton.Fold, "Fold", false);
		bm.addButton(HoldEmButton.Bet, "Bet", false);
		
		raiseSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				raiseChips.setText((bid > 0 ? "raising by " : "betting") + " " + raiseSlider.getValue() + " chips");
			}
		});
		
		bm.addAfterComponent(raiseSlider);
		bm.addAfterComponent(raiseChips);
		
	}
	
	public Point getDeckPos() {
		Dimension canvasSize = getCanvasSize();
		return new Point(canvasSize.width/4*3-100, canvasSize.height/2-30);
	}
	
	public Dimension getCanvasSize() {
		return client.game.getCanvasSize();
	}
	
	@Override
	public ButtonManager getButtonManager() {
		return this.bm;
	}

	@Override
	public void handlePacket(int opcode, NetInputStream packet)
			throws IOException {
		if (opcode == HoldEmOpcodes.LEAVE_TABLE_SEAT) {
            setAttending(false);
            updateButtonStatuses();
        }
        else if (opcode == HoldEmOpcodes.JOIN_TABLE) {
            final boolean finalAdd = packet.readBoolean();
            if (!finalAdd) {
                client.addChatMsg("You will be added to the table next round", (byte) RankLevel.Server.getRankWorth());
            }
            else {
                setAttending(true);
                client.addChatMsg("You have been added to the table", (byte) RankLevel.Server.getRankWorth());
                updateButtonStatuses();
            }
        }
        else if (opcode == HoldEmOpcodes.SEAT_JOINED) {
            
            final byte seat = packet.readByte();
            final String name = packet.readString();
            final int chips = packet.readInt();
            
            final HeOtherPlayer player = new HeOtherPlayer(name, chips);
            
            setSeat(seat, player);
        }
        else if (opcode == HoldEmOpcodes.SEAT_LEFT) {
            
            final byte seat = packet.readByte();
            setSeat(seat, null);
            
        }
        else if (opcode == HoldEmOpcodes.DEALER_CHOSEN) {
            
            final byte seat = packet.readByte();
            setDealer(seat);
            
        }
        else if (opcode == HoldEmOpcodes.REVEAL_CARDS) {
            
            final byte seat = packet.readByte();
            final byte card1 = packet.readByte();
            final byte card2 = packet.readByte();
            
            final HeOtherPlayer player = getSeat(seat);
            final ClientCard[] cards = new ClientCard[2];
            cards[0] = getCard(Card.toCard(card1));
            cards[1] = getCard(Card.toCard(card2));
            player.tryToPutCards(cards);
            
        }
        else if (opcode == HoldEmOpcodes.SHARE_HIDDEN_CARDS) {
            
            shareCards();
            
        }
        else if (opcode == HoldEmOpcodes.REVEAL_FLOP) {
            final Card card1 = Card.toCard(packet.readByte());
            final Card card2 = Card.toCard(packet.readByte());
            final Card card3 = Card.toCard(packet.readByte());
            
            shareBoardCards(0, getCard(card1), getCard(card2), getCard(card3));
        }
        else if (opcode == HoldEmOpcodes.REVEAL_TURN) {
            final Card card1 = Card.toCard(packet.readByte());
            
            shareBoardCards(3, getCard(card1));
        }
        else if (opcode == HoldEmOpcodes.REVEAL_RIVER) {
            final Card card1 = Card.toCard(packet.readByte());
            
            shareBoardCards(4, getCard(card1));
        }
        else if (opcode == HoldEmOpcodes.CHIPS_ADDED) {
            final byte seat = packet.readByte();
            final int newAmount = packet.readInt();
            final HeOtherPlayer zeat = getSeat(seat);
            if (zeat != null) {
                zeat.chips = newAmount;
            }
        }
        else if (opcode == HoldEmOpcodes.CHIPS_REMOVED) {
            final byte seat = packet.readByte();
            final int newAmount = packet.readInt();
            final HeOtherPlayer zeat = getSeat(seat);
            if (zeat != null) {
                zeat.chips = newAmount;
            }
        }
        else if (opcode == HoldEmOpcodes.TURN_CHANGED) {
            final byte seat = packet.readByte();
            final int bid = packet.readInt();
            final boolean allowRaises = packet.readBoolean();
            turnChanged(seat, bid, allowRaises);
        }
        else if (opcode == HoldEmOpcodes.CLEANUP) {
            
            for (int i = 0;i < getMaxIngamePlayers(); i++) {
                final HeOtherPlayer player = getSeat((byte) i);
                if(player == null) {
                    continue;
                }
                player.isDealer = false;
                player.hand = null;
                player.queue = null;
                player.act = null;
            }
            
            boardCleanup();
            
        }
        else if (opcode == HoldEmOpcodes.UPDATE_POT) {
            
            final int pot = packet.readInt();
            
            setPot(pot);
        }
        else if (opcode == HoldEmOpcodes.DO_ACTION) {
        	
        	byte seat = packet.readByte();
        	int act = packet.readInt();
        	
        	setAct(seat, HoldEmAction.getAction(act));
        	
        }
        else {
        	System.out.println("Unidentified packet " + opcode);
        } 
	}

	private void setAct(byte seat, HoldEmAction action) {
		HeOtherPlayer player = getSeat(seat);
		if (player != null) {
			player.act = action;
		}
	}

	@Override
	public void renderGame(Graphics g) {
		
        final Graphics2D g2 = (Graphics2D) g;
        
        Dimension canvasSize = getCanvasSize();
        
        Asset table = client.getAssets().getAsset("pytn");
        
        if (table == null) {
        	g.setColor(Color.black);
        	g.drawString("Waiting for assets..", 50, 50);
        	return;
        }
        
        g.setColor(Color.black);
        //g.drawOval(25, 25, canvasSize.width-50, canvasSize.height - 50);
        g.drawImage(table.getImg(), 0, 0, null);
        
        final BufferedImage backImg = client.getAssets().getBack().getImg();
        
        for (int i = 0;i < tablePlayers.length; i++) {
            
            final HeOtherPlayer player = tablePlayers[i];
            
            if (player == null) {
                continue;
            }
            
            final Point thisGuyLoc = getTableLocation(i, CARD_WX, CARD_HX, canvasSize);
            
            /*
            g.setColor(Color.red);
            g.fillRect(thisGuyLoc.x, thisGuyLoc.y, 5, 5);
            */
            
            if (player.hand != null) {
                for (int i2 = 0;i2 < player.hand.length; i2++) {
                    
                    BufferedImage img;
                    
                    if (player.hand[i2] == null) {
                        img = backImg;
                    }
                    else {
                        final Card card = player.hand[i2];
                        img = client.getAssets().getBySR(card.suit, card.rank).getImg();
                    }
                    
                    g.drawImage(img,thisGuyLoc.x + i2*20, thisGuyLoc.y, null);
                }
            }
            
            final Point dclc = getTableLocation(i, DEALER_CHIP_WX, DEALER_CHIP_HX, canvasSize);
            
            if (player.isDealer) {
                 g.drawImage(client.getAssets().getAsset("dealer").getImg(), dclc.x, dclc.y, null);
            }
            
            g.setColor(Color.black);
            g.drawString(player.name + (player.act == null ? "" : " [" + player.act.name() + "]"), dclc.x, dclc.y-10);
            
            if (turn == i) {
                g.setColor(new Color(255, 0, 0, 180));
                g.fillRect(dclc.x-10, dclc.y-18, 8, 8);
            }
            
            if (player.chips > 0) {
                final Point dcl = new Point(thisGuyLoc.x + 80, thisGuyLoc.y+50);
                drawChipsAt(g, dcl, player.chips);
            }
            
        }
        
        Point deckPos = getDeckPos();
        
        g2.drawImage(backImg, deckPos.x, deckPos.y, null);
        
        int i = 0;
        for (final ClientCard card : board) {
            if (card == null) {
                continue;
            }
            final Point p = getBoardLocation(i++);
            g2.drawImage(client.getAssets().getByCard(card).getImg(), p.x, p.y, null);
        }
            
        
        final Iterator<Animation> ait = visibleCards.iterator();
        while (ait.hasNext()) {
            final Animation a = ait.next();
            if (a.remove()) {
                ait.remove();
                continue;
            }
            a.render(g);
        }
	}

	@Override
	public void onPress(AbstractBtnEnum abn, JButton btn) {
        try {
        	NetClient netClient = client.getNetClient();
            if (abn == HoldEmButton.JoinTable) {
            	netClient.send(ClientPacketFactory.makeJoinTablePacket());
            }
            else if (abn == HoldEmButton.LeaveTable) {
            	netClient.send(ClientPacketFactory.makeLeaveTableSeatPacket());
            }
            else if (abn == HoldEmButton.ExitTable) {
            	if (attending)
            		netClient.send(ClientPacketFactory.makeLeaveTableSeatPacket());
            	netClient.send(ClientPacketFactory.makeSpectateTablePacket(-1));
            	client.gotoServerList();
            	netClient.send(ClientPacketFactory.makeRefreshTablesPacket());
            }
            else {
            	HoldEmAction act = ((HoldEmButton) abn).act;
            	if (btn.getText().equals("Raise")) {
            		act = HoldEmAction.Raise;
            	}
            	else if (btn.getText().equals("Call")) {
            		act = HoldEmAction.Call;
            	}
            	
            	short bid = (short) raiseSlider.getValue();
            	if (bid < 0)
            		bid = 0;
            		
            	netClient.send(ClientPacketFactory.makeHoldEmActionPacket(act, bid));
                 
                myTurn = false; // TODO
                updateButtonStatuses();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
	}

	
	// nonabstract
	
    private final static int CARD_WX = 350;
    private final static int CARD_HX = 170;
    
    private final static int DEALER_CHIP_WX = 390;
    private final static int DEALER_CHIP_HX = 210;
    
    public Point getTableLocation(final int i, final int wx, final int hx, Dimension canvasSize) {
        final double radiansPerPerson = (Math.PI*2)/tablePlayers.length;
        final Point theMiddle = new Point(canvasSize.width/2, canvasSize.height/2);
        
        final double myRadian = radiansPerPerson*i - Math.PI;
        
        final int x = theMiddle.x + (int) (Math.cos(myRadian)*wx);
        final int y = theMiddle.y + (int) (Math.sin(myRadian)*hx);
        
        return new Point(x, y);
    }
    
    public void setSeat(final byte seat, final HeOtherPlayer player) {
        tablePlayers[seat] = player;
    }
    
    public void setDealer(final byte seat) {
        for (final HeOtherPlayer p : tablePlayers) {
            if (p != null) {
                p.isDealer = false;
            }
        }
        if (tablePlayers[seat] != null) {
            tablePlayers[seat].isDealer = true;
        }
    }
    
    public void setAttending(final boolean b) {
        this.attending = b;
    }
    
    public void setMaxIngamePlayers(final byte b) {
        this.maxIngamePlayers = b;
        this.tablePlayers = new HeOtherPlayer[b];
    }
    
    public HeOtherPlayer getSeat(final byte seat) {
        return tablePlayers[seat];
    }
    
    public Point getBoardLocation(final int i) {
    	Point deckPos = getDeckPos();
        return new Point(deckPos.x-(150)+(i*20), deckPos.y);
    }
    
    public void shareCards() {
        
        new Thread(new Runnable() {
            @Override
            public void run() {
            	Dimension canvasSize = getCanvasSize();
                int i2 = 0;
                final Asset back = client.getAssets().getBack();
                for (final HeOtherPlayer player : tablePlayers) {
                    if (player == null) {
                        continue;
                    }
                    for(int i = 0;i < 2; i++) {
                        final BeginningAnimation anim = new BeginningAnimation(getDeckPos(), getTableLocation(i2, CARD_WX, CARD_HX, canvasSize), back, player);
                        visibleCards.add(anim);
                        try {
                            Thread.sleep(300);
                        } catch (final InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(200);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                    i2++;
                }
                
            }
        }).start();
        
    }
    
    public void shareBoardCards(final int startIndex, final ClientCard... cards) {
        for (int i = 0;i < getMaxIngamePlayers(); i++) {
            final HeOtherPlayer player = getSeat((byte) i);
            if(player == null) {
                continue;
            }
            player.act = null;
        }
    	new Thread(new Runnable() {
            @Override
            public void run() {
                
                for (int i = startIndex; i < startIndex+cards.length; i++) {
                    final ClientCard card = cards[i-startIndex];
                    final HoldemBoardAnimation anim = new HoldemBoardAnimation(getDeckPos(), getBoardLocation(i), client.getAssets().getByCard(card), i, card, HoldEm.this);
                    visibleCards.add(anim);
                    try {
                        Thread.sleep(500);
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
            }
        }).start();
    }
    
    public int mySeat() {
        int i = 0;
        for (final HeOtherPlayer player : tablePlayers) {
            if (player != null && player.name.equals(client.getMyName()))
                return i;
            i++;
        }
        return -1;
    }
    
    final int[] wiggly = new int[] {-1, 2, -2, 1};
    
    public void drawChipsAt(final Graphics g, final Point where, final int totalChips) {
        
        int blackChips = 0,
        greenChips = 0,
        blueChips = 0,
        redChips = 0,
        whiteChips = 0;
        
        int workWith = totalChips;
        
        blackChips = (int) Math.floor((totalChips / 100));
        workWith -= blackChips * 100;
        
        greenChips = (int) Math.floor(workWith / 25);
        workWith -= greenChips * 25;
        
        blueChips = (int) Math.floor(workWith / 10);
        workWith -= blueChips * 10;
        
        redChips = (int) Math.floor(workWith / 5);
        workWith -= redChips * 5;
        
        whiteChips = workWith;
        
        final Asset blackChip = client.getAssets().getAsset("blackchip");
        final Asset greenChip = client.getAssets().getAsset("greenchip");
        final Asset blueChip = client.getAssets().getAsset("bluechip");
        final Asset redChip = client.getAssets().getAsset("redchip");
        final Asset whiteChip = client.getAssets().getAsset("whitechip");
        
        for (int x = 0; x < 5; x++) {
            int amount = 0;
            Asset asset = null;
            if (x == 0) {
                asset = blackChip;
                amount = blackChips;
            } else if (x == 1) {
                asset = greenChip;
                amount = greenChips;
            } else if (x == 2) {	
                asset = blueChip;
                amount = blueChips;
            } else if (x == 3) {
                asset = redChip;
                amount = redChips;
            } else if (x == 4) {
                asset = whiteChip;
                amount = whiteChips;
            }
            
            if (amount == 0) {
                continue;
            }
            
            final int xl = where.x + x*14;
            
            for (int i = 0;i < amount; i++) {
                final int yl = where.y - i*4;
                g.drawImage(asset.getImg(), (xl+wiggly[i%4]), yl, null);
            }
        }
    }

    public void setBoardCard(final int boardIndex, final ClientCard card) {
        this.board[boardIndex] = card;
    }
    
    public int getMaxIngamePlayers() {
        return this.maxIngamePlayers;
    }
    
    public void boardCleanup() {
        board = new ClientCard[5];
        this.myTurn = false;
        this.turn = -1;
        updateButtonStatuses();
        bid = 0;
        this.wholePot = 0;
    }

    public void setPot(final int pot) {
        this.wholePot = pot;
    }
    
    public void toggleGameBtns(final boolean flagMyTurn) {
        bm.toggleButton(HoldEmButton.Check, flagMyTurn);
        bm.toggleButton(HoldEmButton.Bet, flagMyTurn);
        bm.toggleButton(HoldEmButton.AllIn, flagMyTurn);
        bm.toggleButton(HoldEmButton.Fold, flagMyTurn);
        if (flagMyTurn) {
            final int myChips = tablePlayers[mySeat()].chips;
            if (myChips == 1) {
                raiseSlider.setEnabled(false);
                bm.toggleButton(HoldEmButton.Check, false);
                bm.toggleButton(HoldEmButton.Bet, false);
            }
            else {
                raiseSlider.setEnabled(true);
                raiseSlider.setMinimum(1);
                if (bid > 0) {
                    raiseSlider.setMaximum(myChips-bid-2);
                }
                else {
                    raiseSlider.setMaximum(myChips-2);
                }
                raiseSlider.setValue(1);
                raiseChips.setText((bid > 0 ? "raising by " : "betting") + " 1 chips");
            }
            
            bm.toggleButton(HoldEmButton.Bet, allowRaises);
            
            if (bid > myChips) {
                bm.toggleButton(HoldEmButton.Check, false);
                bm.toggleButton(HoldEmButton.Bet, false);
                raiseSlider.setEnabled(false);
            }
            
            callAmount.setText("Amount to pay: " + bid + " chips");
            bm.setText(HoldEmButton.Check, bid > 0 ? "Call" : "Check");
            bm.setText(HoldEmButton.Bet, bid > 0 ? "Raise" : "Bet");
        }
        else {
            raiseSlider.setMinimum(0);
            raiseSlider.setMaximum(0);
            raiseSlider.setValue(0);
            raiseChips.setText("");
            
            callAmount.setText("Amount to pay: 0 chips");
        }
    }
    
    public void updateButtonStatuses() {
        if (!attending) {
        	bm.enableButton(HoldEmButton.JoinTable);
        	bm.disableButton(HoldEmButton.LeaveTable);
            toggleGameBtns(false);
        }
        else {
        	bm.disableButton(HoldEmButton.JoinTable);
        	bm.enableButton(HoldEmButton.LeaveTable);
            toggleGameBtns(myTurn);
        }
    }
    
    public static ClientCard getCard(final Card card) {
        return new ClientCard(card.suit, card.rank);// TODO
    }
    
    public void turnChanged(final byte seat, final int bid, final boolean allowRaises) {
        final int mySeat = mySeat();
        this.bid = bid;
        this.allowRaises = allowRaises;
        if (seat == -1 || seat == mySeat) {
            this.myTurn = true;
            this.turn = (byte) mySeat;
            Toolkit.getDefaultToolkit().beep();
        }
        else {
            this.turn = seat;
            this.myTurn = false;
        }
        updateButtonStatuses();
    }

	@Override
	public void updateGame(int delta) {
		// TODO Auto-generated method stub
		
	}
}
