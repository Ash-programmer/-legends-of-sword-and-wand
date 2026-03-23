package com.example.ui.views;

import com.example.controllers.CampaignController;
import com.example.domain.*;
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
        setSize(500,400);
        setLocationRelativeTo(null);

        output = new JTextArea();

        JButton start = new JButton("Start");
        JButton next = new JButton("Next Room");
        JButton save = new JButton("Save");
        JButton end = new JButton("End");

        JPanel p = new JPanel();

        p.add(start);
        p.add(next);
        p.add(save);
        p.add(end);

        add(new JScrollPane(output),BorderLayout.CENTER);
        add(p,BorderLayout.SOUTH);

        start.addActionListener(e -> startCampaign());
        next.addActionListener(e -> nextRoom());
        save.addActionListener(e -> save());
        end.addActionListener(e -> end());
    }

    public void setData(User u, Party p) {
        user = u;
        party = p;
    }

    private void startCampaign() {

        if(user == null || party == null) {
            output.append("No user/party\n");
            return;
        }

        campaign = controller.startCampaign(user,party);

        output.append("Started\n");

    }

    private void nextRoom() {

        if(campaign == null) return;

        Room r = controller.nextRoom(campaign);

        output.append("Room "+r.getRoomNumber()+" "+r.getType()+"\n");
    }

    private void save() {

        if(campaign == null) return;

        controller.saveProgress(campaign);

        output.append("Saved\n");

    }

    private void end() {

        if(campaign == null) return;

        Score s = controller.endCampaign(user,campaign);

        output.append("Score "+s.getValue()+"\n");

    }

    public void start() {
        setVisible(true);
    }
}