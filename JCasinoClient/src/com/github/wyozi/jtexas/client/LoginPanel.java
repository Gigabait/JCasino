package com.github.wyozi.jtexas.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginPanel extends JPanel implements ActionListener {

    JTextField user;
    JPasswordField pass;
    JLabel status;
    MainClient client;

    public LoginPanel(final MainClient client) {

        super(new GridLayout(4, 2));

        add(new JLabel("Status:"));

        status = new JLabel("Idle");

        add(status);

        add(new JLabel("Username:"));

        user = new JTextField();

        add(user);

        add(new JLabel("Password:"));

        pass = new JPasswordField();

        add(pass);

        final JButton login = new JButton("Login");

        login.addActionListener(this);

        add(login);

        this.client = client;
    }

    public void setStatus(final String msg) {
        this.status.setText(msg);
    }

    @Override
    public void actionPerformed(final ActionEvent arg0) {
        final JButton src = (JButton) arg0.getSource();
        if (src.getText().equals("Login")) {
            client.startSocket(user.getText(), new String(pass.getPassword()));
        }
    }
}