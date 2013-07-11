package com.github.wyozi.jtexas.client;

import com.github.wyozi.jtexas.commonsg.net.RankLevel;

public class RankedMessage {
    
    RankLevel level;
    String msg;
    
    public RankedMessage (final RankLevel level, final String msg) {
        this.level = level;
        this.msg = msg;
    }
    
    public RankLevel getLevel() {
        return level;
    }
    
    public String getMsg() {
        return msg;
    }
    
    @Override
    public String toString() {
        return getMsg();
    }
    
}
