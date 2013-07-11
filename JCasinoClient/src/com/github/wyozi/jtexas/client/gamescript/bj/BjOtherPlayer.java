package com.github.wyozi.jtexas.client.gamescript.bj;

import com.github.wyozi.jtexas.client.ClientCard;
import com.github.wyozi.jtexas.client.gamescript.OtherPlayer;
import com.github.wyozi.jtexas.commonsg.net.games.BlackJackAction;

import java.util.ArrayList;
import java.util.List;


public class BjOtherPlayer extends OtherPlayer {
    
	public BjOtherPlayer(String name, int chips) {
		super(name, chips);
	}
	
	public List<ClientCard> hand = new ArrayList<ClientCard>();
	public BlackJackAction act;
	
	
    
}
