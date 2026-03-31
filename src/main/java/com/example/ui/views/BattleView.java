package com.example.ui.views;

import com.example.Main;
import com.example.controllers.BattleController;
import com.example.domain.*;
import com.example.domain.Action;
import com.example.ui.UICommands;
import com.example.domain.RoomType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BattleView extends JFrame implements UICommands {

    private final BattleController controller;
    private final AppState appState;

    private JTextArea output;
    private JComboBox<String> targetBox;

    private JButton startButton;
    private JButton attackButton;
    private JButton defendButton;
    private JButton waitButton;
    private JButton specialButton;
    private JButton useItemButton;
    private JButton refreshButton;

    private BattleState battleState;
    private boolean rewardsApplied = false;
    private boolean actionLocked = false;

    private final List<String> battleLog = new ArrayList<>();

    public BattleView(AppState state, BattleController controller) {
        this.appState = state;
        this.controller = controller;
        init();
    }

    private void init() {
        setTitle("Battle");
        setSize(860, 620);
        setLocationRelativeTo(null);

        output = new JTextArea();
        output.setEditable(false);

        targetBox = new JComboBox<>();

        startButton = new JButton("Start Battle");
        attackButton = new JButton("Attack");
        defendButton = new JButton("Defend");
        waitButton = new JButton("Wait");
        specialButton = new JButton("Special");
        useItemButton = new JButton("Use Item");
        refreshButton = new JButton("Refresh");

        JPanel top = new JPanel();
        top.add(startButton);
        top.add(new JLabel("Target"));
        top.add(targetBox);
        top.add(attackButton);
        top.add(defendButton);
        top.add(waitButton);
        top.add(specialButton);
        top.add(useItemButton);
        top.add(refreshButton);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(output), BorderLayout.CENTER);

        startButton.addActionListener(e -> startBattle());
        attackButton.addActionListener(e -> playerAction(ActionType.ATTACK));
        defendButton.addActionListener(e -> playerAction(ActionType.DEFEND));
        waitButton.addActionListener(e -> playerAction(ActionType.WAIT));
        specialButton.addActionListener(e -> playerAction(ActionType.SPECIAL));
        useItemButton.addActionListener(e -> useItem());
        refreshButton.addActionListener(e -> refreshBattleView());

        setActionButtonsEnabled(false);
    }

    private void startBattle() {
        if (actionLocked) return;

        Party playerParty;
        Party enemyParty;

        if (appState.pvpMode) {
            playerParty = appState.currentParty;
            enemyParty = appState.currentOpponentParty;
        } else if (appState.currentCampaign != null && appState.currentCampaign.getParty() != null) {
            playerParty = appState.currentCampaign.getParty();
            enemyParty = buildEnemyParty();
        } else if (appState.currentParty != null) {
            playerParty = appState.currentParty;
            enemyParty = buildEnemyParty();
        } else {
            playerParty = new Party();
            playerParty.addHero(new Hero("DemoHero", "Warrior"));
            enemyParty = buildEnemyParty();
        }

        if (playerParty == null || playerParty.getHeroes().isEmpty()) {
            log("Your party has no heroes.");
            refreshBattleView();
            return;
        }

        if (enemyParty == null || enemyParty.getHeroes().isEmpty()) {
            log("Enemy party could not be loaded.");
            refreshBattleView();
            return;
        }

        battleState = controller.startBattle(playerParty, enemyParty);
        appState.battleInProgress = true;
        appState.currentlyInBattle = true;
        appState.currentlyInInn = false;
        rewardsApplied = false;
        actionLocked = false;
        battleLog.clear();

        log(appState.pvpMode ? "PvP battle started." : "Battle started.");
        if (battleState.getCurrentHero() != null) {
            battleState.setHeroStatus(battleState.getCurrentHero(), "Current");
        }
        refreshBattleView();

        if (!isPlayerTurn()) {
            processSingleEnemyTurn();
        }
    }

    private Party buildEnemyParty() {
        if (appState.pvpMode && appState.currentOpponentParty != null) {
            return appState.currentOpponentParty;
        }

        Party enemy = new Party();
        int room = appState.currentCampaign != null ? appState.currentCampaign.getCurrentRoom() : 2;

        if (room < 10) {
            enemy.addHero(new Hero("Goblin", "Chaos"));
        } else if (room < 20) {
            enemy.addHero(new Hero("Bandit", "Warrior"));
            enemy.addHero(new Hero("Cultist", "Chaos"));
        } else {
            enemy.addHero(new Hero("Knight", "Order"));
            enemy.addHero(new Hero("Warlock", "Mage"));
            enemy.addHero(new Hero("Brute", "Warrior"));
        }

        return enemy;
    }

    private void playerAction(ActionType type) {
        if (actionLocked) return;

        if (battleState == null) {
            log("Start a battle first.");
            refreshBattleView();
            return;
        }

        if (battleState.isFinished()) {
            log("Battle is already over.");
            refreshBattleView();
            return;
        }

        Hero actor = battleState.getCurrentHero();
        if (actor == null) {
            log("No current actor.");
            refreshBattleView();
            return;
        }

        if (!isPlayerHero(actor)) {
            log("It is not your turn.");
            refreshBattleView();
            return;
        }

        Hero target = null;
        int manaCost = 0;

        if (type == ActionType.ATTACK || type == ActionType.SPECIAL) {
            target = selectedEnemyTarget();
            if (target == null) {
                log("Choose a target.");
                refreshBattleView();
                return;
            }

            if (type == ActionType.SPECIAL) {
                manaCost = manaCostFor(actor);
            }
        }

        actionLocked = true;
        setActionButtonsEnabled(false);

        Action action = new Action(type, actor, target, manaCost);
        BattleResult result = controller.executeTurn(battleState, action);

        if (type == ActionType.ATTACK && target != null) {
            log(actor.getName() + " attacked " + target.getName() + ".");
        } else if (type == ActionType.DEFEND) {
            log(actor.getName() + " defended.");
        } else if (type == ActionType.WAIT) {
            log(actor.getName() + " waited and moved to the end of the order.");
        } else if (type == ActionType.SPECIAL) {
            log(actor.getName() + " used a special ability.");
        }

        log(result.getMessage());
        refreshBattleView();

        if (battleState.isFinished()) {
            finishBattle(result);
            return;
        }

        actionLocked = false;

        if (!isPlayerTurn()) {
            processSingleEnemyTurn();
        } else {
            refreshBattleView();
        }
    }

    private void useItem() {
        if (actionLocked) return;

        if (battleState == null || battleState.isFinished()) {
            log("Start a battle first.");
            refreshBattleView();
            return;
        }

        if (!isPlayerTurn()) {
            log("You can only use items on your turn.");
            refreshBattleView();
            return;
        }

        if (appState.currentInventory == null || appState.currentInventory.getItems().isEmpty()) {
            log("You have no items.");
            refreshBattleView();
            return;
        }

        List<Item> items = appState.currentInventory.getItems();
        List<Hero> heroes = livingHeroes(battleState.getPlayerParty());

        if (heroes.isEmpty()) {
            log("No living heroes can receive an item.");
            refreshBattleView();
            return;
        }

        String[] itemOptions = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            itemOptions[i] = item.getName() + " (" + item.getEffectType() + " " + item.getEffectValue() + ")";
        }

        String selectedItemText = (String) JOptionPane.showInputDialog(
                this,
                "Choose an item:",
                "Use Item",
                JOptionPane.PLAIN_MESSAGE,
                null,
                itemOptions,
                itemOptions[0]
        );

        if (selectedItemText == null) return;

        int itemIndex = findSelectedIndex(itemOptions, selectedItemText);
        if (itemIndex < 0) return;

        Item selectedItem = items.get(itemIndex);

        String[] heroOptions = new String[heroes.size()];
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            heroOptions[i] = h.getName() + " HP " + h.getHp() + "/" + h.getMaxHp() + " Mana " + h.getMana();
        }

        String selectedHeroText = (String) JOptionPane.showInputDialog(
                this,
                "Choose a hero:",
                "Use Item",
                JOptionPane.PLAIN_MESSAGE,
                null,
                heroOptions,
                heroOptions[0]
        );

        if (selectedHeroText == null) return;

        int heroIndex = findSelectedIndex(heroOptions, selectedHeroText);
        if (heroIndex < 0) return;

        Hero selectedHero = heroes.get(heroIndex);

        boolean used = appState.currentInventory.useItem(selectedItem, selectedHero);
        if (!used) {
            log("Could not use item.");
            refreshBattleView();
            return;
        }

        log("Used " + selectedItem.getName() + " on " + selectedHero.getName() + ".");

        BattleResult result = controller.executeTurn(
                battleState,
                new Action(ActionType.WAIT, battleState.getCurrentHero(), null, 0)
        );

        log("Using an item consumed the turn.");
        log(result.getMessage());
        refreshBattleView();

        if (battleState.isFinished()) {
            finishBattle(result);
            return;
        }

        actionLocked = false;

        if (!isPlayerTurn()) {
            processSingleEnemyTurn();
        } else {
            refreshBattleView();
        }
    }

    private void processSingleEnemyTurn() {
        while (true) {
            if (battleState == null || battleState.isFinished()) {
                actionLocked = false;
                refreshBattleView();
                return;
            }

            Hero actor = battleState.getCurrentHero();
            if (actor == null) {
                actionLocked = false;
                refreshBattleView();
                return;
            }

            if (isPlayerHero(actor)) {
                actionLocked = false;
                refreshBattleView();
                return;
            }

            Hero target = firstLivingHero(battleState.getPlayerParty());
            if (target == null) {
                battleState.checkBattleEnd();
                refreshBattleView();
                if (battleState.isFinished()) {
                    finishBattle(new BattleResult(false, 0, 0, "Player lost"));
                }
                return;
            }

            int hpBefore = target.getHp();
            target.takeDamage(actor.getAttack());
            battleState.setHeroStatus(actor, "Attacked");
            battleState.checkBattleEnd();
            int hpAfter = target.getHp();

            log(actor.getName() + " attacked " + target.getName() + " for " + (hpBefore - hpAfter) + " damage.");

            if (battleState.isFinished()) {
                refreshBattleView();
                finishBattle(new BattleResult(false, 0, 0, "Player lost"));
                return;
            }

            battleState.nextTurn();
        }
    }

    private void finishBattle(BattleResult result) {
        applyBattleOutcome(result);
        refreshBattleView();
        actionLocked = false;
        setActionButtonsEnabled(false);
        SwingUtilities.invokeLater(() -> finishFlow(result));
    }

    private void finishFlow(BattleResult result) {
        appState.battleInProgress = false;
        appState.currentlyInBattle = false;

        if (appState.pvpMode) {
            boolean currentUserWon = result.didPlayerWin();
            String msg = Main.pvpController.completeInvite(
                    appState.currentInviteId,
                    appState.currentUser,
                    currentUserWon
            );

            appState.lastBattleSummary =
                    result.getMessage() + "\n" + buildSurvivorSummary() + "\n" + msg;

            JOptionPane.showMessageDialog(
                    this,
                    appState.lastBattleSummary,
                    "PvP Result",
                    JOptionPane.INFORMATION_MESSAGE
            );

            appState.pvpMode = false;
            appState.currentInviteId = 0;
            appState.currentOpponentParty = null;
            appState.currentOpponentUser = null;
            dispose();
            return;
        }

        String summary = result.getMessage() + "\n" + buildSurvivorSummary();
        appState.lastBattleSummary = summary;

        if (appState.currentCampaign != null) {
            int roomNumber = appState.currentCampaign.getCurrentRoom();

            if (result.didPlayerWin()) {
                if (roomNumber >= com.example.domain.Campaign.FINAL_ROOM) {
                    appState.currentCampaign.advanceRoom();
                    appState.currentCampaign.setLastRoomType(RoomType.INN);
                    Main.campaignController.saveProgress(
                            appState.currentUser.getUserId(),
                            appState.currentCampaign
                    );

                    JOptionPane.showMessageDialog(
                            this,
                            summary + "\nYou cleared room " + roomNumber + ".\nCampaign complete.",
                            "Battle Result",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    CampaignView campaignView = new CampaignView(appState, Main.campaignController);
                    campaignView.start();
                    dispose();
                    return;
                }

                appState.currentCampaign.advanceRoom();

                appState.currentCampaign.setLastRoomType(RoomType.INN);
                appState.currentlyInInn = true;

                Main.campaignController.saveProgress(
                        appState.currentUser.getUserId(),
                        appState.currentCampaign
                );

                JOptionPane.showMessageDialog(
                        this,
                        summary + "\nYou cleared room " + roomNumber
                                + ".\nReturning to the inn."
                                + "\nNext battle room: " + appState.currentCampaign.getCurrentRoom()
                                + " / " + com.example.domain.Campaign.FINAL_ROOM,
                        "Battle Result",
                        JOptionPane.INFORMATION_MESSAGE
                );

                new InnView(appState, Main.innController).start();
                dispose();
                return;
            } else {
                // loss: do NOT advance room
                appState.currentCampaign.setLastRoomType(RoomType.INN);
                appState.currentlyInInn = true;

                Main.campaignController.saveProgress(
                        appState.currentUser.getUserId(),
                        appState.currentCampaign
                );

                JOptionPane.showMessageDialog(
                        this,
                        summary + "\nYou lost in room " + roomNumber
                                + ".\nYou must retry the same room after recovering at the inn.",
                        "Battle Result",
                        JOptionPane.INFORMATION_MESSAGE
                );

                new InnView(appState, Main.innController).start();
                dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this, summary, "Battle Result", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private String buildSurvivorSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Surviving heroes: ");
        List<Hero> survivors = livingHeroes(battleState.getPlayerParty());
        if (survivors.isEmpty()) {
            sb.append("none");
        } else {
            for (int i = 0; i < survivors.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(survivors.get(i).getName());
            }
        }
        return sb.toString();
    }

    private void applyBattleOutcome(BattleResult result) {
        if (rewardsApplied) return;
        rewardsApplied = true;

        Party playerParty = battleState.getPlayerParty();

        if (appState.pvpMode) {
            log("PvP battle: no gold or experience awarded.");
            return;
        }

        if (result.didPlayerWin()) {
            int totalExp = 0;
            int totalGold = 0;

            for (Hero enemy : battleState.getEnemyParty().getHeroes()) {
                int enemyLevel = enemy.getLevel();
                totalExp += 50 * enemyLevel;
                totalGold += 75 * enemyLevel;
            }

            java.util.List<Hero> standingHeroes = new java.util.ArrayList<>();
            for (Hero hero : playerParty.getHeroes()) {
                if (hero.getHp() > 1) {
                    standingHeroes.add(hero);
                }
            }

            int expPerHero = 0;
            if (!standingHeroes.isEmpty()) {
                expPerHero = totalExp / standingHeroes.size();
            }

            playerParty.addGold(totalGold);

            StringBuilder rewardLog = new StringBuilder();
            rewardLog.append("Victory rewards:\n");
            rewardLog.append("Total EXP: ").append(totalExp).append("\n");
            rewardLog.append("Total Gold: ").append(totalGold).append("\n");
            rewardLog.append("Standing heroes: ").append(standingHeroes.size()).append("\n");
            rewardLog.append("EXP per standing hero: ").append(expPerHero).append("\n");

            for (Hero hero : standingHeroes) {
                int oldLevel = hero.getLevel();
                hero.gainExperience(expPerHero);

                rewardLog.append(hero.getName())
                        .append(" gained ")
                        .append(expPerHero)
                        .append(" EXP");

                if (hero.getLevel() > oldLevel) {
                    rewardLog.append(" and leveled up to ").append(hero.getLevel());
                }

                rewardLog.append(".\n");
            }

            if (appState.currentCampaign != null) {
                appState.currentCampaign.addScore(totalGold + totalExp);
                Main.campaignController.saveProgress(
                        appState.currentUser.getUserId(),
                        appState.currentCampaign
                );
            }

            log(rewardLog.toString());
        } else {
            int goldLoss = (int) Math.floor(playerParty.getGold() * 0.10);
            playerParty.spendGold(goldLoss);

            StringBuilder penaltyLog = new StringBuilder();
            penaltyLog.append("Defeat penalties:\n");
            penaltyLog.append("Lost gold: ").append(goldLoss).append("\n");

            for (Hero hero : playerParty.getHeroes()) {
                int lostExp = hero.loseCurrentLevelExperiencePercent(0.30);
                penaltyLog.append(hero.getName())
                        .append(" lost ")
                        .append(lostExp)
                        .append(" EXP from the current level.\n");
            }

            if (appState.currentCampaign != null) {
                Main.campaignController.saveProgress(
                        appState.currentUser.getUserId(),
                        appState.currentCampaign
                );
            }

            log(penaltyLog.toString());
        }
    }


    private void postBattleChoice() {
        appState.battleInProgress = false;
        appState.currentlyInBattle = false;

        if (appState.currentCampaign != null) {
            int clearedRoom = appState.currentCampaign.getCurrentRoom();

            if (clearedRoom >= com.example.domain.Campaign.FINAL_ROOM) {
                appState.currentCampaign.advanceRoom();
                appState.currentCampaign.setLastRoomType(RoomType.INN);
                Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);

                JOptionPane.showMessageDialog(
                        this,
                        "You cleared room " + clearedRoom + ".\nThe campaign is complete.",
                        "Campaign Complete",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();
                return;
            }

            appState.currentCampaign.advanceRoom(); // next battle room
            appState.currentCampaign.setLastRoomType(RoomType.INN);
            Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);

            appState.currentlyInInn = true;

            JOptionPane.showMessageDialog(
                    this,
                    "You cleared room " + clearedRoom + ".\nReturning to the inn.\nNext battle room: "
                            + appState.currentCampaign.getCurrentRoom() + " / "
                            + com.example.domain.Campaign.FINAL_ROOM,
                    "Battle Finished",
                    JOptionPane.INFORMATION_MESSAGE
            );

            new InnView(appState, Main.innController).start();
            dispose();
            return;
        }

        dispose();
    }

    private void refreshBattleView() {
        if (battleState == null) {
            output.setText("No battle started.\n");
            targetBox.setModel(new DefaultComboBoxModel<>(new String[0]));
            setActionButtonsEnabled(false);
            return;
        }

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (Hero enemy : livingHeroes(battleState.getEnemyParty())) {
            model.addElement(enemy.getName());
        }
        targetBox.setModel(model);

        StringBuilder sb = new StringBuilder();

        sb.append("=== TURN ===\n");
        Hero current = battleState.getCurrentHero();
        if (current != null) {
            sb.append("Current actor: ").append(current.getName())
                    .append(" [").append(current.getType()).append("]\n");
            sb.append("Side: ").append(isPlayerHero(current) ? "Player" : "Enemy").append("\n");
            sb.append("Status: ").append(battleState.getHeroStatus(current)).append("\n\n");
        }

        sb.append("=== TURN ORDER ===\n");
        for (Hero hero : battleState.getTurnOrder()) {
            sb.append("- ")
                    .append(hero.getName())
                    .append(" (").append(isPlayerHero(hero) ? "Player" : "Enemy").append(")")
                    .append(" -> ").append(battleState.getHeroStatus(hero))
                    .append("\n");
        }

        sb.append("\n=== PLAYER TEAM ===\n");
        appendParty(sb, battleState.getPlayerParty());

        sb.append("\n=== ENEMY TEAM ===\n");
        appendParty(sb, battleState.getEnemyParty());

        sb.append("\n=== INVENTORY ===\n");
        if (appState.currentInventory == null || appState.currentInventory.getItems().isEmpty()) {
            sb.append("No items.\n");
        } else {
            for (Item item : appState.currentInventory.getItems()) {
                sb.append("- ")
                        .append(item.getName())
                        .append(" / ")
                        .append(item.getEffectType())
                        .append(" ")
                        .append(item.getEffectValue())
                        .append("\n");
            }
        }

        if (appState.currentCampaign != null) {
            sb.append("=== CAMPAIGN PROGRESS ===\n");
            sb.append("Current location: ").append(appState.currentCampaign.getLocationDescription()).append("\n");
            sb.append("Battle room: ").append(appState.currentCampaign.getCurrentRoom())
                    .append(" / ").append(com.example.domain.Campaign.FINAL_ROOM).append("\n");
            sb.append("Rooms cleared: ").append(appState.currentCampaign.getRoomsCleared()).append("\n");
            sb.append("Rooms remaining: ").append(appState.currentCampaign.getRoomsRemaining()).append("\n\n");
        }

        sb.append("\n=== BATTLE LOG ===\n");
        int start = Math.max(0, battleLog.size() - 12);
        for (int i = start; i < battleLog.size(); i++) {
            sb.append("- ").append(battleLog.get(i)).append("\n");
        }

        output.setText(sb.toString());
        updateTurnControls();
    }

    private void appendParty(StringBuilder sb, Party party) {
        sb.append("Gold: ").append(party.getGold()).append("\n");
        for (Hero h : party.getHeroes()) {
            sb.append("- ")
                    .append(h.getName())
                    .append(" [").append(h.getType()).append("]")
                    .append(" L").append(h.getLevel())
                    .append(" EXP ").append(h.getExperience()).append("/").append(h.expToNextLevel(h.getLevel()))
                    .append(" HP ").append(h.getHp()).append("/").append(h.getMaxHp())
                    .append(" Mana ").append(h.getMana()).append("/").append(h.getMaxMana())
                    .append(" ATK ").append(h.getAttack())
                    .append(" DEF ").append(h.getDefense())
                    .append(" Shield ").append(h.getShield())
                    .append(" Status ").append(battleState.getHeroStatus(h))
                    .append(h.isAlive() ? "" : " (DEAD)")
                    .append("\n");
        }
    }

    private void updateTurnControls() {
        boolean canAct = battleState != null
                && !battleState.isFinished()
                && !actionLocked
                && isPlayerTurn();

        setActionButtonsEnabled(canAct);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        attackButton.setEnabled(enabled);
        defendButton.setEnabled(enabled);
        waitButton.setEnabled(enabled);
        specialButton.setEnabled(enabled);
        useItemButton.setEnabled(enabled && !appState.pvpMode);
    }

    private boolean isPlayerTurn() {
        if (battleState == null || battleState.isFinished()) {
            return false;
        }
        Hero current = battleState.getCurrentHero();
        return current != null && isPlayerHero(current);
    }

    private boolean isPlayerHero(Hero hero) {
        return battleState.getPlayerParty().getHeroes().contains(hero);
    }

    private Hero selectedEnemyTarget() {
        Object selected = targetBox.getSelectedItem();
        if (selected == null) return null;

        String name = selected.toString();
        for (Hero h : battleState.getEnemyParty().getHeroes()) {
            if (h.isAlive() && h.getName().equals(name)) {
                return h;
            }
        }
        return null;
    }

    private Hero firstLivingHero(Party party) {
        for (Hero h : party.getHeroes()) {
            if (h.isAlive()) return h;
        }
        return null;
    }

    private List<Hero> livingHeroes(Party party) {
        List<Hero> result = new ArrayList<>();
        for (Hero h : party.getHeroes()) {
            if (h.isAlive()) result.add(h);
        }
        return result;
    }

    private int manaCostFor(Hero hero) {
        return switch (hero.getType()) {
            case "Order" -> 35;
            case "Chaos" -> 40;
            case "Warrior" -> 60;
            case "Mage" -> 80;
            default -> 20;
        };
    }

    private int findSelectedIndex(String[] options, String selected) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selected)) {
                return i;
            }
        }
        return -1;
    }

    private void log(String message) {
        battleLog.add(message);
    }

    public void start() {
        setVisible(true);
    }
}
