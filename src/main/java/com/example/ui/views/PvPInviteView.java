package com.example.ui.views;

import com.example.controllers.PvPInviteController;
import com.example.domain.User;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class PvPInviteView extends JFrame implements UICommands {

    private PvPInviteController controller;

    private JTextField fromField;
    private JTextField toField;
    private JTextArea output;

    public PvPInviteView(
            PvPInviteController controller) {

        this.controller = controller;
        init();
    }

    private void init() {

        setTitle("PvP");
        setSize(400,250);

        fromField = new JTextField();
        toField = new JTextField();
        output = new JTextArea();

        JButton send = new JButton("Send");

        JPanel p = new JPanel(new GridLayout(3,2));

        p.add(new JLabel("From"));
        p.add(fromField);

        p.add(new JLabel("To"));
        p.add(toField);

        p.add(send);

        add(p,BorderLayout.NORTH);
        add(output,BorderLayout.CENTER);

        send.addActionListener(e -> send());
    }

    private void send() {

        User u =
                new User(1,fromField.getText(),"h");

        boolean ok =
                controller.sendInvite(
                        u,
                        toField.getText()
                );

        if(ok)
            output.setText("Sent");
        else
            output.setText("Fail");
    }

    public void start() {
        setVisible(true);
    }
}