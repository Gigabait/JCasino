package com.github.wyozi.jtexas.server.chiphandler;

import com.github.wyozi.jtexas.server.MyServerClient;
import com.github.wyozi.jtexas.server.Table;

public class DebugChipHandler extends FreeChipHandler {

	public DebugChipHandler(int startWithChips, Table table) {
		super(startWithChips, table);
	}

	@Override
	public void addChips(MyServerClient client, int amount, String type, int seat) {
		super.addChips(client, amount, type, seat);
		
		System.out.println(amount + " chips added to " + client.getName() + " type " + type + " seat " + seat);
	}

	@Override
	public void removeChips(MyServerClient client, int amount, String type, int seat) {
		super.removeChips(client, amount, type, seat);
		
		System.out.println(amount + " chips removed from " + client.getName() + " type " + type + " seat " + seat);
	}
	
	

}
