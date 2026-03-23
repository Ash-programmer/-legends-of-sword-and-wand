package com.example.ui.views;

import com.example.controllers.CampaignController;
import com.example.domain.*;
import com.example.Main;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class CampaignView extends JFrame implements UICommands {

    private CampaignController controller;

    private JTextArea output;

    private Campaign campaign;
    private User user;
    private Party party;

    public CampaignView(CampaignController controller) {

        this.controller = controller;

        init();
    }

    private void init() {

        setTitle("Campaign");
        setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        output = new JTextArea();

        JButton start = new JButton("Start Campaign");
        JButton next = new JButton("Next Room");
        JButton save = new JButton("Save");
        JButton end = new JButton("End Campaign");
        JButton logout = new JButton("Logout");

        JPanel p = new JPanel();

        p.add(start);
        p.add(next);
        p.add(save);
        p.add(end);
        p.add(logout);

        add(new JScrollPane(output), BorderLayout.CENTER);
        add(p, BorderLayout.SOUTH);

        start.addActionListener(e -> startCampaign());
        next.addActionListener(e -> nextRoom());
        save.addActionListener(e -> save());
        end.addActionListener(e -> end());
        logout.addActionListener(e -> logout());
    }



    public void setData(User u, Party p) {

        this.user = u;
        this.party = p;

    }



    private void startCampaign() {

        if(user == null) {

            output.append("No user\n");
            return;
        }

        if(party == null) {

            output.append("No party\n");
            return;
        }

        campaign =
                controller.startCampaign(
                        user,
                        party
                );

        output.append("Campaign started\n");

        output.append(
                "Room: "
                        + campaign.getCurrentRoom()
                        + "\n"
        );

    }



    private void nextRoom() {

        if(campaign == null) {

            output.append("Start campaign first\n");
            return;
        }

        Room r =
                controller.nextRoom(
                        campaign
                );

        output.append(
                "Room "
                        + r.getRoomNumber()
                        + " "
                        + r.getType()
                        + "\n"
        );

    }



    private void save() {

        if(campaign == null) {

            output.append("Nothing to save\n");
            return;
        }

        controller.saveProgress(
                campaign
        );

        output.append("Saved\n");

    }



    private void end() {

        if(campaign == null) {

            output.append("No campaign\n");
            return;
        }

        if(user == null) {

            output.append("No user\n");
            return;
        }

        Score s =
                controller.endCampaign(
                        user,
                        campaign
                );

        output.append(
                "Score = "
                        + s.getValue()
                        + "\n"
        );

    }

    private void logout() {

        Main.authController.logout();
        dispose();
        AuthView.returnToLogin(Main.authController);

    }

    public void start() {

        setVisible(true);

    }

}