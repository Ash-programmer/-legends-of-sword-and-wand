package com.example.ui.views;

import com.example.Main;
import com.example.controllers.CampaignController;
import com.example.domain.*;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CampaignView extends JFrame implements UICommands {

    private final CampaignController controller;
    private final AppState state;

    private JTextArea output;
    private JComboBox<String> classBox;
    private JTextField heroNameField;

    public CampaignView(AppState state, CampaignController controller) {
        this.state = state;
        this.controller = controller;
        init();
        refreshOutput();
    }

    private void init() {
        setTitle("Campaign");
        setSize(820, 560);
        setLocationRelativeTo(null);

        output = new JTextArea();
        output.setEditable(false);

        classBox = new JComboBox<>(new String[]{"Warrior", "Mage", "Order", "Chaos"});
        heroNameField = new JTextField("Hero", 10);

        JButton start = new JButton("Start New Campaign");
        JButton nextRoom = new JButton("Visit Next Room");
        JButton useItem = new JButton("Use Item");
        JButton save = new JButton("Save Progress");
        JButton endCampaign = new JButton("End Campaign");
        JButton exit = new JButton("Save & Exit");

        JPanel top = new JPanel();
        top.add(new JLabel("Hero Name"));
        top.add(heroNameField);
        top.add(new JLabel("Class"));
        top.add(classBox);
        top.add(start);

        JPanel bottom = new JPanel();
        bottom.add(nextRoom);
        bottom.add(useItem);
        bottom.add(save);
        bottom.add(endCampaign);
        bottom.add(exit);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        start.addActionListener(e -> startCampaign());
        nextRoom.addActionListener(e -> visitNextRoom());
        useItem.addActionListener(e -> useItemOnHero());
        save.addActionListener(e -> saveProgress());
        endCampaign.addActionListener(e -> endCampaignFlow());
        exit.addActionListener(e -> saveAndExit());
    }

    private void startCampaign() {
        String heroType = (String) classBox.getSelectedItem();
        String heroName = heroNameField.getText().trim();
        if (heroName.isBlank()) {
            heroName = heroType;
        }

        Party party = new Party();
        party.setName(state.currentUser.getUsername() + "'s Party");
        party.addHero(new Hero(heroName, heroType));

        state.currentParty = party;
        state.currentInventory = new Inventory();
        state.currentCampaign = controller.startCampaign(state.currentUser, party);
        state.currentUser.getCampaigns().clear();
        state.currentUser.addCampaign(state.currentCampaign);
        state.currentlyInInn = true;
        state.currentlyInBattle = false;
        state.battleInProgress = false;
        state.pvpMode = false;
        state.lastBattleSummary = "Started new campaign. Room 1 is an inn.";

        controller.saveProgress(state.currentUser.getUserId(), state.currentCampaign);
        refreshOutput();
        new InnView(state, Main.innController).start();
        dispose();
    }

    private void visitNextRoom() {
        if (state.currentCampaign == null) {
            appendSummary("No active campaign.");
            refreshOutput();
            return;
        }

        if (state.currentCampaign.isComplete()) {
            endCampaignFlow();
            return;
        }

        state.currentCampaign.setLastRoomType(RoomType.BATTLE);
        controller.saveProgress(state.currentUser.getUserId(), state.currentCampaign);

        state.currentlyInInn = false;
        state.currentlyInBattle = true;
        state.battleInProgress = true;

        appendSummary("Entering battle room " + state.currentCampaign.getCurrentRoom() + ".");
        new BattleView(state, Main.battleController).start();
        dispose();
    }

    private void saveProgress() {
        if (state.currentCampaign == null) {
            appendSummary("No campaign to save.");
            refreshOutput();
            return;
        }

        controller.saveProgress(state.currentUser.getUserId(), state.currentCampaign);
        appendSummary("Campaign progress saved.");
        refreshOutput();
    }

    private void useItemOnHero() {
        if (state.currentCampaign == null || state.currentInventory == null || state.currentParty == null) {
            appendSummary("No active campaign.");
            refreshOutput();
            return;
        }

        List<Item> items = state.currentInventory.getItems();
        if (items.isEmpty()) {
            appendSummary("Inventory is empty.");
            refreshOutput();
            return;
        }

        List<Hero> heroes = state.currentParty.getHeroes();
        if (heroes.isEmpty()) {
            appendSummary("No heroes in party.");
            refreshOutput();
            return;
        }

        String[] itemOptions = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            itemOptions[i] = item.getName() + " (" + item.getEffectType() + " " + item.getEffectValue() + ")";
        }

        String itemChoice = (String) JOptionPane.showInputDialog(
                this,
                "Choose an item:",
                "Use Item",
                JOptionPane.PLAIN_MESSAGE,
                null,
                itemOptions,
                itemOptions[0]
        );

        if (itemChoice == null) return;

        int itemIndex = findSelectedIndex(itemOptions, itemChoice);
        if (itemIndex < 0) return;
        Item selectedItem = items.get(itemIndex);

        String[] heroOptions = new String[heroes.size()];
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            heroOptions[i] = h.getName() + " [" + h.getType() + "] HP " + h.getHp() + "/" + h.getMaxHp();
        }

        String heroChoice = (String) JOptionPane.showInputDialog(
                this,
                "Choose a hero:",
                "Use Item",
                JOptionPane.PLAIN_MESSAGE,
                null,
                heroOptions,
                heroOptions[0]
        );

        if (heroChoice == null) return;

        int heroIndex = findSelectedIndex(heroOptions, heroChoice);
        if (heroIndex < 0) return;
        Hero selectedHero = heroes.get(heroIndex);

        boolean used = state.currentInventory.useItem(selectedItem, selectedHero);
        appendSummary(used
                ? "Used " + selectedItem.getName() + " on " + selectedHero.getName() + "."
                : "Could not use item.");
        refreshOutput();
    }

    private void endCampaignFlow() {
        if (state.currentCampaign == null) {
            appendSummary("No active campaign.");
            refreshOutput();
            return;
        }

        boolean keepParty = JOptionPane.showConfirmDialog(
                this,
                "Do you want to keep this party after the campaign ends?",
                "Keep Party?",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;

        Integer replacePartyId = null;

        if (keepParty && state.currentUser.getSavedParties().size() >= 5) {
            String[] options = new String[state.currentUser.getSavedParties().size()];
            for (int i = 0; i < state.currentUser.getSavedParties().size(); i++) {
                Party p = state.currentUser.getSavedParties().get(i);
                String name = p.getName() == null ? "Party " + p.getId() : p.getName();
                options[i] = p.getId() + " - " + name;
            }

            String selected = (String) JOptionPane.showInputDialog(
                    this,
                    "You already have 5 saved parties. Choose one to replace:",
                    "Replace Party",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (selected == null) {
                appendSummary("End campaign cancelled.");
                refreshOutput();
                return;
            }

            String idPart = selected.split(" - ")[0].trim();
            replacePartyId = Integer.parseInt(idPart);
        }

        Score score = controller.endCampaign(state.currentUser, state.currentCampaign, keepParty, replacePartyId);

        if (keepParty) {
            if (replacePartyId != null) {
                final Integer finalReplacePartyId = replacePartyId;
                state.currentUser.getSavedParties().removeIf(p -> p.getId() == finalReplacePartyId);
            }
            state.currentUser.addParty(state.currentCampaign.getParty());
        }

        state.currentUser.getCampaigns().clear();
        state.currentCampaign = null;
        state.currentlyInInn = false;
        state.currentlyInBattle = false;
        state.battleInProgress = false;
        appendSummary("Campaign ended. Final score added: " + score.getValue());
        refreshOutput();
    }

    private void saveAndExit() {
        if (state.currentCampaign == null) {
            dispose();
            return;
        }

        boolean canExit = controller.canExitCampaign(state.battleInProgress, state.currentCampaign);
        if (!canExit) {
            appendSummary("You cannot exit while a battle is in progress.");
            refreshOutput();
            return;
        }

        controller.saveProgress(state.currentUser.getUserId(), state.currentCampaign);
        dispose();
    }

    private void refreshOutput() {
        StringBuilder sb = new StringBuilder();

        sb.append("=== PROFILE ===\n");
        sb.append("User: ").append(state.currentUser.getUsername()).append("\n");
        sb.append("Score: ").append(state.currentUser.getScore()).append("\n");
        sb.append("Ranking: ").append(state.currentUser.getRanking()).append("\n");
        sb.append("Saved parties: ").append(state.currentUser.getSavedParties().size()).append("\n\n");

        if (state.currentCampaign == null) {
            sb.append("No active campaign.\n");
        } else {
            sb.append("=== CAMPAIGN ===\n");
            sb.append("Current location: ").append(state.currentCampaign.getLocationDescription()).append("\n");
            sb.append("Next battle room: ").append(state.currentCampaign.getCurrentRoom())
                    .append(" / ").append(com.example.domain.Campaign.FINAL_ROOM).append("\n");
            sb.append("Rooms cleared: ").append(state.currentCampaign.getRoomsCleared()).append("\n");
            sb.append("Rooms remaining: ").append(state.currentCampaign.getRoomsRemaining()).append("\n");
            sb.append("Complete: ").append(state.currentCampaign.isComplete()).append("\n");
            sb.append("Campaign score: ").append(state.currentCampaign.getScore()).append("\n\n");
        }

        if (state.currentParty != null) {
            sb.append("=== PARTY ===\n");
            sb.append("Gold: ").append(state.currentParty.getGold()).append("\n");
            sb.append("Heroes:\n");
            for (Hero h : state.currentParty.getHeroes()) {
                sb.append("- ")
                        .append(h.getName()).append(" [").append(h.getType()).append("]")
                        .append(" L").append(h.getLevel())
                        .append(" EXP ").append(h.getExperience()).append("/").append(h.expToNextLevel(h.getLevel()))
                        .append(" HP ").append(h.getHp()).append("/").append(h.getMaxHp())
                        .append(" Mana ").append(h.getMana())
                        .append(" ATK ").append(h.getAttack())
                        .append(" DEF ").append(h.getDefense())
                        .append(h.isAlive() ? "" : " (DEAD)")
                        .append("\n");
            }
            sb.append("\n");
        }

        if (state.currentInventory != null) {
            sb.append("=== INVENTORY ===\n");
            if (state.currentInventory.getItems().isEmpty()) {
                sb.append("No items.\n");
            } else {
                for (Item item : state.currentInventory.getItems()) {
                    sb.append("- ")
                            .append(item.getName())
                            .append(" / cost ").append(item.getCost())
                            .append(" / ").append(item.getEffectType())
                            .append(" ").append(item.getEffectValue())
                            .append("\n");
                }
            }
        }

        output.setText(sb.toString());
    }

    private void appendSummary(String message) {
        state.lastBattleSummary = message;
    }

    private int findSelectedIndex(String[] options, String selected) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selected)) return i;
        }
        return -1;
    }

    public void start() {
        setVisible(true);
    }
}
