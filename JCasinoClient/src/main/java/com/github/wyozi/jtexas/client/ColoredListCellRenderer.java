package com.github.wyozi.jtexas.client;

import com.github.wyozi.jtexas.commons.net.RankLevel;

import javax.swing.*;
import java.awt.*;

public class ColoredListCellRenderer extends JTextArea implements ListCellRenderer {

    public ColoredListCellRenderer() {
        this.setOpaque(true);
        this.setLineWrap(true);
        this.setWrapStyleWord(false);
    }

    @Override
    public Component getListCellRendererComponent(
            final JList list, final Object value, final int index,
            final boolean isSelected, final boolean cellHasFocus) {
        if (value == null || !(value instanceof RankedMessage))
            return this;
        final RankedMessage val = (RankedMessage) value;

        setBackground(isSelected ? Color.cyan : Color.white);

        if (val.getLevel() == null) {
            setForeground(Color.black);
        } else {
            setForeground(val.getLevel().getChatColor());
            if (val.getLevel() == RankLevel.Server) {
                setBackground(Color.black);
            }
        }

        setText(val.getMsg());

        return this;
    }
}
