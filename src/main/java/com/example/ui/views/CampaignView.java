package com.example.ui.views;

import com.example.controllers.CampaignController;
import com.example.domain.*;
import com.example.ui.UICommands;
import javax.swing.*;
import java.awt.*;

public class CampaignView extends JFrame implements UICommands {

    private CampaignController controller;
    private AppState state;
    private JTextArea output;

    public CampaignView(AppState state, CampaignController controller) {
        this.state = state;
        this.controller = controller;
        init();
    }

    private void init() {
        setTitle("Campaign");
        setSize(500, 400);
        output = new JTextArea();
        JButton start = new JButton("Start");
        JButton next = new JButton("Next Room");
        JButton save = new JButton("Save");
        JPanel p = new JPanel();
        p.add(start);
        p.add(next);
        p.add(save);
        add(new JScrollPane(output), BorderLayout.CENTER);
        add(p, BorderLayout.SOUTH);
        start.addActionListener(e -> startCampaign());
        next.addActionListener(e -> nextRoom());
        save.addActionListener(e -> saveProgress());
    }

    private void startCampaign() {
        state.currentCampaign = controller.startCampaign(
                state.currentUser,
                state.currentParty
        );
        output.append("Campaign started\n");
    }

    private void nextRoom() {
        Room r = controller.nextRoom(state.currentCampaign);
        output.append("Room " + r.getRoomNumber() + " " + r.getType() + "\n");
    }

    private void saveProgress() {
        controller.saveProgress(state.currentUser.getUserId(), state.currentCampaign);
        output.append("Saved\n");
    }

    public void start() {
        setVisible(true);
    }
}