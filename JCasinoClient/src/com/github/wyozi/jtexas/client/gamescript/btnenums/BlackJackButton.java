package com.github.wyozi.jtexas.client.gamescript.btnenums;

import com.github.wyozi.jtexas.commonsg.net.games.BlackJackAction;


public enum BlackJackButton implements AbstractBtnEnum {
    JoinTable(null),
    LeaveTable(null),
    ExitTable(null),
    Bet(BlackJackAction.Bet),
    Hit(BlackJackAction.Hit),
    Stand(BlackJackAction.Stand),
    Double(BlackJackAction.Double),
    Split(BlackJackAction.Split);


    public final BlackJackAction act;

    BlackJackButton(BlackJackAction act) {
        this.act = act;
    }
}
