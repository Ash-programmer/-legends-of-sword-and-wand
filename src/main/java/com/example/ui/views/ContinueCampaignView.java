package com.example.ui.views;

import com.example.Main;
import com.example.controllers.ContinueCampaignController;
import com.example.domain.Campaign;
import com.example.domain.RoomType;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class ContinueCampaignView extends JFrame implements UICommands {

    private final AppState state;
    private final ContinueCampaignController controller;
    private JTextArea output;

    public ContinueCampaignView(AppState state, ContinueCampaignController controller) {
        this.state = state;
        this.controller = controller;
        init();
    }

    private void init() {
        setTitle("Continue Campaign");
        setSize(420, 220);
        setLocationRelativeTo(null);

        output = new JTextArea();
        output.setEditable(false);

        JButton load = new JButton("Load My Campaign");

        add(load, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);

        load.addActionListener(e -> loadCampaign());
    }

    private void loadCampaign() {
        Campaign c = controller.loadCampaign(state.currentUser.getUserId());

        if (c == null) {
            output.setText("No saved campaign found.");
            return;
        }

        state.currentCampaign = c;
        state.currentParty = c.getParty();
        state.currentInventory = c.getInventory();
        state.battleInProgress = false;
        state.pvpMode = false;
        state.lastBattleSummary = "Campaign loaded. Room " + c.getCurrentRoom() + ".";

        output.setText("Loaded campaign.\nRoom: " + c.getCurrentRoom()
                + "\nCurrent room type: " + c.getLastRoomType());

        if (c.getLastRoomType() == RoomType.INN) {
            state.currentlyInInn = true;
            state.currentlyInBattle = false;
            InnView innView = new InnView(state, Main.innController);
            innView.start();
        } else {
            state.currentlyInInn = false;
            state.currentlyInBattle = false;
            CampaignView campaignView = new CampaignView(state, Main.campaignController);
            campaignView.start();
        }
        dispose();
    }

    public void start() {
        setVisible(true);
    }
}
