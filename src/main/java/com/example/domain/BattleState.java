package com.example.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BattleState {

    private final Party playerParty;
    private final Party enemyParty;

    private final List<Hero> turnOrder;
    private int turnIndex;

    private boolean finished;

    public BattleState(Party playerParty, Party enemyParty) {
        this.playerParty = playerParty;
        this.enemyParty  = enemyParty;
        this.turnOrder   = new ArrayList<>();
        this.turnIndex   = 0;
        this.finished    = false;

        buildTurnOrder();
    }

    private void buildTurnOrder() {

        Comparator<Hero> byLevelThenAttack = Comparator
                .comparingInt(Hero::getLevel)
                .thenComparingInt(Hero::getAttack)
                .reversed();

        List<Hero> playerSorted = new ArrayList<>(playerParty.getHeroes());
        List<Hero> enemySorted  = new ArrayList<>(enemyParty.getHeroes());
        playerSorted.sort(byLevelThenAttack);
        enemySorted.sort(byLevelThenAttack);

        List<Hero> first, second;
        if (playerSorted.isEmpty()) {
            first  = enemySorted;
            second = playerSorted;
        } else if (enemySorted.isEmpty()) {
            first  = playerSorted;
            second = enemySorted;
        } else {
            Hero topPlayer = playerSorted.get(0);
            Hero topEnemy  = enemySorted.get(0);

            boolean playerGoesFirst =
                    topPlayer.getLevel() > topEnemy.getLevel() ||
                            (topPlayer.getLevel() == topEnemy.getLevel()
                                    && topPlayer.getAttack() >= topEnemy.getAttack());

            first  = playerGoesFirst ? playerSorted : enemySorted;
            second = playerGoesFirst ? enemySorted  : playerSorted;
        }

        int max = Math.max(first.size(), second.size());
        for (int i = 0; i < max; i++) {
            if (i < first.size())  turnOrder.add(first.get(i));
            if (i < second.size()) turnOrder.add(second.get(i));
        }
    }

    public Hero getCurrentHero() {
        if (turnOrder.isEmpty()) return null;

        for (int i = 0; i < turnOrder.size(); i++) {
            Hero candidate = turnOrder.get((turnIndex + i) % turnOrder.size());
            if (candidate.isAlive()) return candidate;
        }
        return null;
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) return;

        int attempts = 0;
        do {
            turnIndex = (turnIndex + 1) % turnOrder.size();
            attempts++;
        } while (!turnOrder.get(turnIndex).isAlive() && attempts < turnOrder.size());
    }

    public void checkBattleEnd() {
        if (!playerParty.hasLivingHeroes() || !enemyParty.hasLivingHeroes()) {
            finished = true;
        }
    }

    public Party getPlayerParty() { return playerParty; }
    public Party getEnemyParty()  { return enemyParty;  }
    public boolean isFinished()   { return finished;    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}