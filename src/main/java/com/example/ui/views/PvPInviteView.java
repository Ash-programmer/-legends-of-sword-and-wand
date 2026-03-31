package com.example.ui.views;

import com.example.Main;
import com.example.controllers.PvPInviteController;
import com.example.domain.Party;
import com.example.domain.PvPInvite;
import com.example.domain.User;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PvPInviteView extends JFrame implements UICommands {

    private final PvPInviteController controller;
    private final AppState state;

    private JTextField opponent;
    private JTextArea output;

    public PvPInviteView(AppState state, PvPInviteController controller) {
        this.state = state;
        this.controller = controller;
        init();
        refreshInvites();
    }

    private void init() {
        setTitle("PvP");
        setSize(620, 420);
        setLocationRelativeTo(null);

        opponent = new JTextField();
        output = new JTextArea();
        output.setEditable(false);

        JButton send = new JButton("Send Invite");
        JButton refresh = new JButton("Refresh Invites");
        JButton accept = new JButton("Accept Selected Invite");
        JButton decline = new JButton("Decline Selected Invite");

        JPanel north = new JPanel(new BorderLayout());
        north.add(new JLabel("Opponent Username:"), BorderLayout.WEST);
        north.add(opponent, BorderLayout.CENTER);
        north.add(send, BorderLayout.EAST);

        JPanel south = new JPanel();
        south.add(refresh);
        south.add(accept);
        south.add(decline);

        add(north, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        send.addActionListener(e -> send());
        refresh.addActionListener(e -> refreshInvites());
        accept.addActionListener(e -> acceptInvite());
        decline.addActionListener(e -> declineInvite());
    }

    private void send() {
        if (state.currentUser.getSavedParties().isEmpty()) {
            JOptionPane.showMessageDialog(this, "You need at least one saved party to send a PvP invite.");
            return;
        }

        int partyId = choosePartyId(state.currentUser, "Choose which saved party to send into PvP:");
        if (partyId < 0) return;

        String message = controller.sendInvite(state.currentUser, partyId, opponent.getText().trim());
        JOptionPane.showMessageDialog(this, message);
        refreshInvites();
    }

    private void acceptInvite() {
        List<PvPInvite> invites = controller.getPendingInvites(state.currentUser);
        if (invites.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending invites.");
            return;
        }

        String[] options = new String[invites.size()];
        for (int i = 0; i < invites.size(); i++) {
            PvPInvite invite = invites.get(i);
            options[i] = invite.getId() + " - from " + invite.getFromUsername();
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Choose an invite to accept:",
                "Accept Invite",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        if (selected == null) return;

        int inviteId = Integer.parseInt(selected.split(" - ")[0].trim());

        if (state.currentUser.getSavedParties().isEmpty()) {
            JOptionPane.showMessageDialog(this, "You need at least one saved party to accept.");
            return;
        }

        int chosenPartyId = choosePartyId(state.currentUser, "Choose your saved party for this PvP battle:");
        if (chosenPartyId < 0) return;

        PvPInvite invite = controller.acceptInvite(inviteId, state.currentUser, chosenPartyId);
        if (invite == null) {
            JOptionPane.showMessageDialog(this, "Could not accept invite.");
            refreshInvites();
            return;
        }

        Party playerParty = controller.loadParty(chosenPartyId);
        Party enemyParty = controller.loadParty(invite.getFromPartyId());
        User challenger = controller.loadUserById(invite.getFromUserId());

        state.pvpMode = true;
        state.currentInviteId = invite.getId();
        state.currentParty = playerParty;
        state.currentOpponentParty = enemyParty;
        state.currentOpponentUser = challenger;
        state.currentCampaign = null;
        state.currentInventory = null;
        state.battleInProgress = true;
        state.currentlyInBattle = true;
        state.currentlyInInn = false;
        state.lastBattleSummary = "PvP battle started against " + challenger.getUsername() + ".";

        new BattleView(state, Main.battleController).start();
        dispose();
    }

    private void declineInvite() {
        List<PvPInvite> invites = controller.getPendingInvites(state.currentUser);
        if (invites.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending invites.");
            return;
        }

        String[] options = new String[invites.size()];
        for (int i = 0; i < invites.size(); i++) {
            PvPInvite invite = invites.get(i);
            options[i] = invite.getId() + " - from " + invite.getFromUsername();
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Choose an invite to decline:",
                "Decline Invite",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
        if (selected == null) return;

        int inviteId = Integer.parseInt(selected.split(" - ")[0].trim());
        boolean ok = controller.declineInvite(inviteId, state.currentUser);
        JOptionPane.showMessageDialog(this, ok ? "Invite declined." : "Could not decline invite.");
        refreshInvites();
    }

    private int choosePartyId(User user, String prompt) {
        String[] partyOptions = new String[user.getSavedParties().size()];
        for (int i = 0; i < user.getSavedParties().size(); i++) {
            Party party = user.getSavedParties().get(i);
            String name = party.getName() == null ? "Party " + party.getId() : party.getName();
            partyOptions[i] = party.getId() + " - " + name + " (heroes: " + party.getSize() + ")";
        }

        String partyChoice = (String) JOptionPane.showInputDialog(
                this,
                prompt,
                "Choose Party",
                JOptionPane.PLAIN_MESSAGE,
                null,
                partyOptions,
                partyOptions[0]
        );

        if (partyChoice == null) return -1;
        return Integer.parseInt(partyChoice.split(" - ")[0].trim());
    }

    private void refreshInvites() {
        List<PvPInvite> invites = controller.getPendingInvites(state.currentUser);
        StringBuilder sb = new StringBuilder();
        sb.append("Saved parties: ").append(state.currentUser.getSavedParties().size()).append("\n\n");
        sb.append("Pending invites for ").append(state.currentUser.getUsername()).append(":\n");

        if (invites.isEmpty()) {
            sb.append("- none\n");
        } else {
            for (PvPInvite invite : invites) {
                sb.append("- ID ").append(invite.getId())
                        .append(" from ").append(invite.getFromUsername())
                        .append(" using party ").append(invite.getFromPartyId())
                        .append("\n");
            }
        }

        output.setText(sb.toString());
    }

    public void start() {
        setVisible(true);
    }
}
