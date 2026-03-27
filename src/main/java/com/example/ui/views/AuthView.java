package com.example.ui.views;

import com.example.Main;
import com.example.controllers.AuthController;
import com.example.domain.Campaign;
import com.example.domain.Inventory;
import com.example.domain.Party;
import com.example.domain.User;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class AuthView extends JFrame implements UICommands {

    private final AuthController controller;
    private final AppState state;

    JTextField userField;
    JPasswordField passField;

    public AuthView(AuthController controller) {
        this.controller = controller;
        this.state = new AppState();
        init();
    }

    private void init() {
        setTitle("Login");
        setSize(360, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        userField = new JTextField();
        passField = new JPasswordField();

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");

        JPanel p = new JPanel(new GridLayout(3, 2));
        p.add(new JLabel("Username"));
        p.add(userField);
        p.add(new JLabel("Password"));
        p.add(passField);
        p.add(login);
        p.add(register);

        add(p);

        login.addActionListener(e -> login());
        register.addActionListener(e -> register());
    }

    private void login() {
        User u = controller.login(
                userField.getText(),
                new String(passField.getPassword())
        );

        if (u == null) {
            JOptionPane.showMessageDialog(this, "Login failed");
            return;
        }

        state.currentUser = u;

        if (u.hasCampaign()) {
            Campaign c = u.getCampaigns().get(0);
            state.currentCampaign = c;
            state.currentParty = c.getParty();
            state.currentInventory = c.getInventory();
        } else if (u.hasSavedParty()) {
            state.currentParty = u.getSavedParties().get(0);
            state.currentInventory = new Inventory();
        } else {
            state.currentParty = null;
            state.currentInventory = new Inventory();
        }

        dispose();

        MainMenuView menu = new MainMenuView(
                state,
                Main.campaignController,
                Main.battleController,
                Main.innController,
                Main.pvpController,
                Main.continueController
        );
        menu.start();
    }

    private void register() {
        boolean ok = controller.register(
                userField.getText(),
                new String(passField.getPassword())
        );

        JOptionPane.showMessageDialog(this, ok ? "Registered" : "Failed");
    }

    public void start() {
        setVisible(true);
    }
}