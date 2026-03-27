package com.example.ui.views;

import com.example.controllers.*;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class MainMenuView extends JFrame implements UICommands {

    private final AppState state;
    private final CampaignController campaignController;
    private final BattleController battleController;
    private final InnController innController;
    private final PvPInviteController pvpController;
    private final ContinueCampaignController continueController;

    private JLabel profileLabel;

    public MainMenuView(
            AppState state,
            CampaignController campaignController,
            BattleController battleController,
            InnController innController,
            PvPInviteController pvpController,
            ContinueCampaignController continueController
    ) {
        this.state = state;
        this.campaignController = campaignController;
        this.battleController = battleController;
        this.innController = innController;
        this.pvpController = pvpController;
        this.continueController = continueController;

        init();
    }

    private void init() {
        setTitle("Main Menu");
        setSize(460, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        profileLabel = new JLabel(profileText(), SwingConstants.CENTER);

        JButton campaign = new JButton("Start / View Campaign");
        JButton battle = new JButton("Battle");
        JButton inn = new JButton("Inn");
        JButton pvp = new JButton("PvP Invite");
        JButton continueBtn = new JButton("Continue Campaign");

        JPanel center = new JPanel(new GridLayout(5, 1, 5, 5));
        center.add(campaign);
        center.add(battle);
        center.add(inn);
        center.add(pvp);
        center.add(continueBtn);

        add(profileLabel, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        campaign.addActionListener(e -> new CampaignView(state, campaignController).start());
        battle.addActionListener(e -> new BattleView(state, battleController).start());
        inn.addActionListener(e -> new InnView(state, innController).start());
        pvp.addActionListener(e -> new PvPInviteView(state, pvpController).start());
        continueBtn.addActionListener(e -> new ContinueCampaignView(state, continueController).start());
    }

    private String profileText() {
        int partyCount = state.currentUser.getSavedParties().size();
        String campaignText = state.currentUser.hasCampaign() ? "yes" : "no";

        return "<html><div style='text-align:center;'>"
                + "<b>User:</b> " + state.currentUser.getUsername()
                + " &nbsp; <b>Score:</b> " + state.currentUser.getScore()
                + " &nbsp; <b>Ranking:</b> " + state.currentUser.getRanking()
                + " &nbsp; <b>Saved Parties:</b> " + partyCount
                + " &nbsp; <b>Active Campaign:</b> " + campaignText
                + "</div></html>";
    }

    public void start() {
        setVisible(true);
    }
}
