package com.example.ui.views;

import com.example.controllers.AuthController;
import com.example.domain.User;
import com.example.domain.Hero;
import com.example.domain.Party;
import com.example.Main;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class AuthView extends JFrame implements UICommands {

    private AuthController controller;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel message;

    public AuthView(AuthController controller) {
        this.controller = controller;
        init();
    }

    private void init() {

        setTitle("Login");
        setSize(400,200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4,2));

        panel.add(new JLabel("Username"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton login = new JButton("Login");
        JButton register = new JButton("Register");

        panel.add(login);
        panel.add(register);

        message = new JLabel("");
        panel.add(message);

        add(panel);

        login.addActionListener(e -> login());
        register.addActionListener(e -> register());
    }

    private void login() {

        String u = usernameField.getText();
        String p = new String(passwordField.getPassword());

        User user = controller.login(u,p);

        if(user != null) {

            message.setText("Login success");

            Party party = new Party();
            party.addHero(new Hero("Knight","Warrior"));

            CampaignView v =
                    new CampaignView(Main.campaignController);

            v.setData(user, party);

            dispose();

            v.start();

        }
        else {
            message.setText("Login failed");
        }
    }

    private void register() {

        String u = usernameField.getText();
        String p = new String(passwordField.getPassword());

        boolean ok = controller.register(u,p);

        if(ok)
            message.setText("Registered");
        else
            message.setText("Register failed");
    }

    public static void returnToLogin(AuthController controller) {
        AuthView authView = new AuthView(controller);
        authView.start();
    }

    public void start() {
        setVisible(true);
    }
}