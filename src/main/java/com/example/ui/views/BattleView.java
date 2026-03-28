package com.example.ui.views;

import com.example.Main;
import com.example.controllers.BattleController;
import com.example.domain.ActionType;
import com.example.domain.BattleResult;
import com.example.domain.BattleState;
import com.example.domain.Hero;
import com.example.domain.Item;
import com.example.domain.Party;
import com.example.ui.UICommands;

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
        setSize(820, 580);
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

        if (appState.currentCampaign != null && appState.currentCampaign.getParty() != null) {
            playerParty = appState.currentCampaign.getParty();
        } else if (appState.currentParty != null) {
            playerParty = appState.currentParty;
        } else {
            playerParty = new Party();
            playerParty.addHero(new Hero("DemoHero", "Warrior"));
        }

        if (playerParty.getHeroes().isEmpty()) {
            log("Your party has no heroes.");
            refreshBattleView();
            return;
        }

        Party enemyParty = buildEnemyParty();

        battleState = controller.startBattle(playerParty, enemyParty);
        appState.battleInProgress = true;
        appState.currentlyInBattle = true;
        appState.currentlyInInn = false;
        rewardsApplied = false;
        actionLocked = false;
        battleLog.clear();

        log("Battle started.");
        refreshBattleView();

        if (!isPlayerTurn()) {
            scheduleSingleEnemyTurn();
        }
    }

    private Party buildEnemyParty() {
        Party enemy = new Party();
        int room = appState.currentCampaign != null ? appState.currentCampaign.getCurrentRoom() : 1;

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

        com.example.domain.Action action =
                new com.example.domain.Action(type, actor, target, manaCost);

        BattleResult result = controller.executeTurn(battleState, action);

        if (type == ActionType.ATTACK && target != null) {
            log(actor.getName() + " attacked " + target.getName() + ".");
        } else if (type == ActionType.DEFEND) {
            log(actor.getName() + " defended and recovered HP/mana.");
        } else if (type == ActionType.WAIT) {
            log(actor.getName() + " waited and moved to the end of the turn order.");
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

        Hero actor = battleState.getCurrentHero();
        BattleResult result = controller.executeTurn(
                battleState,
                new com.example.domain.Action(ActionType.WAIT, actor, null, 0)
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

    private void scheduleSingleEnemyTurn() {
        processSingleEnemyTurn();
    }

    private void processSingleEnemyTurn() {
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
            } else {
                actionLocked = false;
                refreshBattleView();
            }
            return;
        }

        int hpBefore = target.getHp();
        int shieldBefore = target.getShield();
        int actorAttack = actor.getAttack();
        int targetDefense = target.getDefense();

        target.takeDamage(actorAttack);

        battleState.checkBattleEnd();

        int hpAfter = target.getHp();
        int shieldAfter = target.getShield();
        int damageDone = hpBefore - hpAfter;

        log(actor.getName() + " attacked " + target.getName() + ".");
        log("Actor attack: " + actorAttack + ", Target defense: " + targetDefense);
        log("HP before: " + hpBefore + ", HP after: " + hpAfter +
                ", Shield before: " + shieldBefore + ", Shield after: " + shieldAfter +
                ", Damage dealt: " + damageDone);

        if (battleState.isFinished()) {
            refreshBattleView();
            finishBattle(new BattleResult(false, 0, 0, "Player lost"));
            return;
        }

        battleState.nextTurn();

        actionLocked = false;
        refreshBattleView();
    }

    private void finishBattle(BattleResult result) {
        applyBattleOutcome(result);
        refreshBattleView();
        actionLocked = false;
        setActionButtonsEnabled(false);
        SwingUtilities.invokeLater(this::postBattleChoice);
    }

    private void postBattleChoice() {
        appState.battleInProgress = false;
        appState.currentlyInBattle = false;

        String[] options = {"Head Back to Inn", "Go to Next Battle"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Battle finished. What do you want to do next?",
                "After Battle",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            appState.currentlyInInn = true;
            if (appState.currentCampaign != null) {
                appState.currentCampaign.setLastRoomType(com.example.domain.RoomType.INN);
                Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);
            }
            new InnView(appState, Main.innController).start();
            dispose();
            return;
        }

        if (!isPartyAtFullHealth()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Your heroes are not at full health. Continue to the next battle anyway?",
                    "Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                appState.currentlyInInn = true;
                if (appState.currentCampaign != null) {
                    appState.currentCampaign.setLastRoomType(com.example.domain.RoomType.INN);
                    Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);
                }
                new InnView(appState, Main.innController).start();
                dispose();
                return;
            }
        }

        if (appState.currentCampaign != null) {
            appState.currentCampaign.advanceRoom();
            appState.currentCampaign.setLastRoomType(com.example.domain.RoomType.BATTLE);
            Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);
        }

        appState.currentlyInBattle = true;
        appState.battleInProgress = true;
        startBattle();
    }

    private boolean isPartyAtFullHealth() {
        Party party = battleState != null ? battleState.getPlayerParty() : appState.currentParty;
        if (party == null) return true;

        for (Hero hero : party.getHeroes()) {
            if (hero.getHp() < hero.getMaxHp()) {
                return false;
            }
        }
        return true;
    }

    private void applyBattleOutcome(BattleResult result) {
        if (rewardsApplied) return;
        rewardsApplied = true;

        Party playerParty = battleState.getPlayerParty();

        if (result.didPlayerWin()) {
            playerParty.addGold(result.getGoldGained());

            int livingCount = 0;
            for (Hero hero : playerParty.getHeroes()) {
                if (hero.isAlive()) {
                    livingCount++;
                }
            }

            if (livingCount > 0) {
                int expEach = result.getExpGained() / livingCount;
                for (Hero hero : playerParty.getHeroes()) {
                    if (hero.isAlive()) {
                        hero.gainExperience(expEach);
                    }
                }
            }

            if (appState.currentCampaign != null) {
                appState.currentCampaign.addScore(result.getGoldGained() + result.getExpGained());
                Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);
            }

            log("Victory rewards applied.");
        } else {
            int goldLoss = Math.max(1, playerParty.getGold() / 10);
            playerParty.spendGold(goldLoss);

            for (Hero hero : playerParty.getHeroes()) {
                hero.loseExperiencePercent(0.30);
            }

            if (appState.currentCampaign != null) {
                Main.campaignController.saveProgress(appState.currentUser.getUserId(), appState.currentCampaign);
            }

            log("Defeat penalties applied.");
        }
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
            sb.append("Status: ").append(current.isAlive() ? "Ready" : "Dead").append("\n\n");
        }

        sb.append("=== PLAYER TEAM ===\n");
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

        sb.append("\n=== BATTLE LOG ===\n");
        int start = Math.max(0, battleLog.size() - 10);
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
                    .append(" HP ").append(h.getHp()).append("/").append(h.getMaxHp())
                    .append(" Mana ").append(h.getMana()).append("/").append(h.getMaxMana())
                    .append(" ATK ").append(h.getAttack())
                    .append(" DEF ").append(h.getDefense())
                    .append(" Shield ").append(h.getShield())
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
        useItemButton.setEnabled(enabled);
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
