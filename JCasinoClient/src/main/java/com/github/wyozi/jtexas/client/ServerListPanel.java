package com.github.wyozi.jtexas.client;

import com.github.wyozi.jtexas.client.net.ClientPacketFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

public class ServerListPanel extends JPanel implements ActionListener {

    DefaultTableModel tModel;
    JTable serverList;
    MainClient client;

    JButton connect;

    public ServerListPanel(final MainClient client) {

        super(new BorderLayout());

        this.client = client;

        tModel = new DefaultTableModel();
        serverList = new JTable(tModel);

        tModel.addColumn("Name");
        tModel.addColumn("Type");
        tModel.addColumn("Players");

        JPanel bottomBtn = new JPanel();
        JPanel topBtn = new JPanel();

        JButton refresh = new JButton("Refresh tables");
        refresh.addActionListener(this);

        topBtn.add(refresh);
        connect = new JButton("Connect");
        connect.setEnabled(false);
        connect.addActionListener(this);
        bottomBtn.add(connect);

        serverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        serverList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (serverList.getSelectedRow() == -1) {
                    connect.setEnabled(false);
                } else {
                    connect.setEnabled(true);
                }
            }
        });

        add(topBtn, BorderLayout.NORTH);
        add(new JScrollPane(serverList), BorderLayout.CENTER);
        add(bottomBtn, BorderLayout.SOUTH);
    }

    private int rowIndex(String name) {
        for (int i = 0; i < tModel.getRowCount(); i++) {
            if (((Table) tModel.getValueAt(i, 0)).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void refresh(List<Table> tables) {
        System.out.println("Adding " + tables.size() + " tables to tModel");
        for (Table t : tables) {
            int i = rowIndex(t.name);
            if (i == -1) {
                System.out.println("Adding new row with " + t);
                tModel.addRow(new Object[]{t, t.type, t.players + "/" + t.maxPlayers});
            } else {
                tModel.setValueAt(t.players + "/" + t.maxPlayers, i, 2);
            }
        }
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        Object src = arg0.getSource();
        if (src instanceof JButton) {
            JButton cSrc = (JButton) src;
            if (cSrc.getText().equals("Refresh tables")) {
                try {
                    client.getNetClient().send(ClientPacketFactory.makeRefreshTablesPacket());
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            } else if (cSrc.getText().equals("Connect")) {
                int id = ((Table) tModel.getValueAt(serverList.getSelectedRow(), 0)).id;
                try {
                    client.getNetClient().send(ClientPacketFactory.makeSpectateTablePacket(id));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connecting to table id " + id);
            }
        }
    }
}