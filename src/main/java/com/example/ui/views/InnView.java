package com.example.ui.views;

import com.example.Main;
import com.example.controllers.InnController;
import com.example.domain.ActionResult;
import com.example.domain.Hero;
import com.example.domain.Item;
import com.example.domain.RoomType;
import com.example.domain.StatusReport;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class InnView extends JFrame implements UICommands {

    private final AppState state;
    private final InnController controller;

    private JTextArea output;

    public InnView(AppState state, InnController controller) {
        this.state = state;
        this.controller = controller;
        init();
        refreshOutput("Welcome to the inn.");
    }

    private void init() {
        setTitle("Inn");
        setSize(760, 500);
        setLocationRelativeTo(null);

        output = new JTextArea();
        output.setEditable(false);

        JButton rest = new JButton("Rest");
        JButton buyPotion = new JButton("Buy Potion");
        JButton buyEther = new JButton("Buy Ether");
        JButton recruitWarrior = new JButton("Recruit Warrior");
        JButton recruitMage = new JButton("Recruit Mage");
        JButton recruitOrder = new JButton("Recruit Order");
        JButton recruitChaos = new JButton("Recruit Chaos");
        JButton nextRoom = new JButton("Visit Next Room");
        JButton save = new JButton("Save Campaign");
        JButton close = new JButton("Close");

        JPanel top = new JPanel();
        top.add(rest);
        top.add(buyPotion);
        top.add(buyEther);

        JPanel middle = new JPanel();
        middle.add(recruitWarrior);
        middle.add(recruitMage);
        middle.add(recruitOrder);
        middle.add(recruitChaos);

        JPanel bottom = new JPanel();
        bottom.add(nextRoom);
        bottom.add(save);
        bottom.add(close);

        JPanel controls = new JPanel(new GridLayout(3, 1));
        controls.add(top);
        controls.add(middle);
        controls.add(bottom);

        add(new JScrollPane(output), BorderLayout.CENTER);
        add(controls, BorderLayout.SOUTH);

        rest.addActionListener(e -> rest());
        buyPotion.addActionListener(e -> buyPotion());
        buyEther.addActionListener(e -> buyEther());
        recruitWarrior.addActionListener(e -> recruit("Warrior"));
        recruitMage.addActionListener(e -> recruit("Mage"));
        recruitOrder.addActionListener(e -> recruit("Order"));
        recruitChaos.addActionListener(e -> recruit("Chaos"));
        nextRoom.addActionListener(e -> visitNextRoom());
        save.addActionListener(e -> saveCampaign());
        close.addActionListener(e -> dispose());
    }

    private void rest() {
        if (state.currentParty == null) {
            refreshOutput("No party available.");
            return;
        }

        StatusReport report = controller.getStatus(state.currentParty);
        StringBuilder msg = new StringBuilder(report.getMessage());

        if (!report.getRevivedDetails().isEmpty()) {
            msg.append("\nRevived:");
            for (String line : report.getRevivedDetails()) {
                msg.append("\n- ").append(line);
            }
        }

        if (!report.getHealedDetails().isEmpty()) {
            msg.append("\nHealed:");
            for (String line : report.getHealedDetails()) {
                msg.append("\n- ").append(line);
            }
        }

        refreshOutput(msg.toString());
    }

    private void buyPotion() {
        if (state.currentParty == null || state.currentInventory == null) {
            refreshOutput("No active party or inventory.");
            return;
        }

        Item potion = new Item("Potion", 10, "heal", 20);
        ActionResult result = controller.purchaseItem(state.currentParty, state.currentInventory, potion);
        refreshOutput(result.getMessage());
    }

    private void buyEther() {
        if (state.currentParty == null || state.currentInventory == null) {
            refreshOutput("No active party or inventory.");
            return;
        }

        Item ether = new Item("Ether", 15, "mana", 20);
        ActionResult result = controller.purchaseItem(state.currentParty, state.currentInventory, ether);
        refreshOutput(result.getMessage());
    }

    private void recruit(String heroType) {
        if (state.currentParty == null) {
            refreshOutput("No party available.");
            return;
        }

        String name = JOptionPane.showInputDialog(this, "Enter hero name for new " + heroType + " (cost: 50 gold):");
        if (name == null || name.isBlank()) {
            name = heroType;
        }

        Hero hero = new Hero(name, heroType);
        ActionResult result = controller.recruitHero(state.currentParty, hero);
        refreshOutput(result.getMessage());
    }

    private void visitNextRoom() {
        if (state.currentCampaign == null) {
            refreshOutput("No campaign available.");
            return;
        }

        if (state.currentCampaign.isComplete()) {
            refreshOutput("The campaign is already complete.");
            return;
        }

        if (state.currentParty == null || state.currentParty.getHeroes().isEmpty()) {
            refreshOutput("No party available.");
            return;
        }

        boolean hasLivingHero = false;
        for (Hero h : state.currentParty.getHeroes()) {
            if (h.isAlive()) {
                hasLivingHero = true;
                break;
            }
        }

        if (!hasLivingHero) {
            refreshOutput("All heroes are down. You must rest at the inn before retrying room "
                    + state.currentCampaign.getCurrentRoom() + ".");
            return;
        }

        state.currentCampaign.setLastRoomType(RoomType.BATTLE);
        Main.campaignController.saveProgress(state.currentUser.getUserId(), state.currentCampaign);

        state.currentlyInInn = false;
        state.currentlyInBattle = true;
        state.battleInProgress = true;
        state.lastBattleSummary = "Entering battle room " + state.currentCampaign.getCurrentRoom() + ".";

        new BattleView(state, Main.battleController).start();
        dispose();
    }

    private void saveCampaign() {
        if (state.currentCampaign == null) {
            refreshOutput("No campaign to save.");
            return;
        }

        Main.campaignController.saveProgress(state.currentUser.getUserId(), state.currentCampaign);
        refreshOutput("Campaign saved.");
    }

    private void refreshOutput(String message) {
        StringBuilder sb = new StringBuilder();

        sb.append(message).append("\n\n");

        if (state.currentCampaign != null) {
            sb.append("=== CAMPAIGN PROGRESS ===\n");
            sb.append("Current location: ").append(state.currentCampaign.getLocationDescription()).append("\n");
            sb.append("Next battle room: ").append(state.currentCampaign.getCurrentRoom())
                    .append(" / ").append(com.example.domain.Campaign.FINAL_ROOM).append("\n");
            sb.append("Rooms cleared: ").append(state.currentCampaign.getRoomsCleared()).append("\n");
            sb.append("Rooms remaining: ").append(state.currentCampaign.getRoomsRemaining()).append("\n\n");
        }

        if (state.currentParty != null) {
            sb.append("=== PARTY STATUS ===\n");
            sb.append("Gold: ").append(state.currentParty.getGold()).append("\n");
            sb.append("Party size: ").append(state.currentParty.getSize()).append("/").append(state.currentParty.getMaxSize()).append("\n");
            for (Hero h : state.currentParty.getHeroes()) {
                sb.append("- ")
                        .append(h.getName())
                        .append(" [").append(h.getType()).append("]")
                        .append(" L").append(h.getLevel())
                        .append(" EXP ").append(h.getExperience()).append("/").append(h.expToNextLevel(h.getLevel()))
                        .append(" HP ").append(h.getHp()).append("/").append(h.getMaxHp())
                        .append(" Mana ").append(h.getMana()).append("/").append(h.getMaxMana())
                        .append(h.isAlive() ? "" : " (DEAD)")
                        .append("\n");
            }
        }

        sb.append("\n=== INVENTORY ===\n");
        if (state.currentInventory == null || state.currentInventory.getItems().isEmpty()) {
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

        sb.append("\nAvailable shop items:\n");
        sb.append("- Potion (10 gold) heals 20 HP\n");
        sb.append("- Ether (15 gold) restores 20 mana\n");
        sb.append("- Recruiting a hero costs 50 gold\n");

        output.setText(sb.toString());
    }


    public void start() {
        setVisible(true);
    }
}
