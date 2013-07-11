package com.github.wyozi.jtexas.client.gamescript.btnenums;

import com.github.wyozi.jtexas.commonsg.net.games.HoldEmAction;


public enum HoldEmButton implements AbstractBtnEnum {
	JoinTable (null),
	LeaveTable (null), 
	ExitTable (null),
	Fold (HoldEmAction.Fold),
	AllIn (HoldEmAction.AllIn),
	Check (HoldEmAction.Check),
	Bet (HoldEmAction.Bet);
	
	public final HoldEmAction act;
	HoldEmButton(HoldEmAction act) {
		this.act = act;
	}
}
