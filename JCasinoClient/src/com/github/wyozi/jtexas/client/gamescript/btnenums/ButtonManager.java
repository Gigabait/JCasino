package com.github.wyozi.jtexas.client.gamescript.btnenums;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;


public class ButtonManager implements ActionListener {
	private TreeMap<AbstractBtnEnum, JButton> buttons = new TreeMap<AbstractBtnEnum, JButton>();
	private ButtonActionListener listener;
	
	private ArrayList<Component> beforeComponents = new ArrayList<Component>();
	private ArrayList<Component> afterComponents = new ArrayList<Component>();
	
	public ButtonManager(ButtonActionListener listener) {
		this.listener = listener;
	}
	
	public void addButton(AbstractBtnEnum abn, String btnText, boolean btnEnabled) {
		JButton btn = new JButton(btnText);
		btn.setEnabled(btnEnabled);
		btn.addActionListener(this);
		
		this.buttons.put(abn, btn);
	}
	public JButton getButton(AbstractBtnEnum abn) {
		return this.buttons.get(abn);
	}
	
	public void disableButton(AbstractBtnEnum abn) {
		this.getButton(abn).setEnabled(false);
	}
	
	public void enableButton(AbstractBtnEnum abn) {
		this.getButton(abn).setEnabled(true);
	}
	
	public void toggleButton(AbstractBtnEnum abn, boolean to) {
		this.getButton(abn).setEnabled(to);
	}
	
	public void setText(AbstractBtnEnum abn, String to) {
		this.getButton(abn).setText(to);
	}
	
	public void addBeforeComponent(Component c) {
		this.beforeComponents.add(c);
	}
	
	public void addAfterComponent(Component c) {
		this.afterComponents.add(c);
	}
	
	public List<Component> getComponents() {
		ArrayList<Component> comps = new ArrayList<Component>();
		comps.addAll(beforeComponents);
		comps.addAll(buttons.values());
		comps.addAll(afterComponents);
		return comps;
	}
	
	private AbstractBtnEnum getEnum(JButton ref) {
		for (Entry<AbstractBtnEnum, JButton> entry : buttons.entrySet()) {
			if (entry.getValue() == ref) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton button = (JButton) e.getSource();
		AbstractBtnEnum abn = getEnum(button);
		this.listener.onPress(abn, button);
	}
}
