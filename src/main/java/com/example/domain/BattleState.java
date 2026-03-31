package com.example.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class BattleState {

    private final Party playerParty;
    private final Party enemyParty;
    private final List<Hero> turnOrder;
    private final Map<Hero, String> heroStatuses;
    private int turnIndex;
    private boolean finished;

    public BattleState(Party playerParty, Party enemyParty) {
        this.playerParty = playerParty;
        this.enemyParty = enemyParty;
        this.turnOrder = new ArrayList<>();
        this.heroStatuses = new IdentityHashMap<>();
        this.turnIndex = 0;
        this.finished = false;

        buildTurnOrder();
        for (Hero hero : turnOrder) {
            heroStatuses.put(hero, "Ready");
        }
        normalizeTurnIndex();
    }

    private void buildTurnOrder() {
        Comparator<Hero> byLevelThenAttack = Comparator
                .comparingInt(Hero::getLevel)
                .thenComparingInt(Hero::getAttack)
                .reversed();

        List<Hero> playerSorted = new ArrayList<>(playerParty.getHeroes());
        List<Hero> enemySorted = new ArrayList<>(enemyParty.getHeroes());

        playerSorted.sort(byLevelThenAttack);
        enemySorted.sort(byLevelThenAttack);

        List<Hero> first;
        List<Hero> second;

        if (playerSorted.isEmpty()) {
            first = enemySorted;
            second = playerSorted;
        } else if (enemySorted.isEmpty()) {
            first = playerSorted;
            second = enemySorted;
        } else {
            Hero topPlayer = playerSorted.get(0);
            Hero topEnemy = enemySorted.get(0);

            boolean playerGoesFirst =
                    topPlayer.getLevel() > topEnemy.getLevel()
                            || (topPlayer.getLevel() == topEnemy.getLevel()
                            && topPlayer.getAttack() >= topEnemy.getAttack());

            first = playerGoesFirst ? playerSorted : enemySorted;
            second = playerGoesFirst ? enemySorted : playerSorted;
        }

        int max = Math.max(first.size(), second.size());
        for (int i = 0; i < max; i++) {
            if (i < first.size()) turnOrder.add(first.get(i));
            if (i < second.size()) turnOrder.add(second.get(i));
        }
    }

    public Hero getCurrentHero() {
        if (turnOrder.isEmpty()) return null;

        normalizeTurnIndex();

        int attempts = 0;
        while (attempts < turnOrder.size()) {
            Hero candidate = turnOrder.get(turnIndex);

            if (!candidate.isAlive()) {
                setHeroStatus(candidate, "Dead");
                turnIndex = (turnIndex + 1) % turnOrder.size();
                attempts++;
                continue;
            }

            if (candidate.isStunned()) {
                setHeroStatus(candidate, "Stunned");
                candidate.clearStun();
                turnIndex = (turnIndex + 1) % turnOrder.size();
                attempts++;
                continue;
            }

            return candidate;
        }

        return null;
    }

    public void nextTurn() {
        if (turnOrder.isEmpty()) return;

        Hero current = getCurrentHero();
        if (current != null && current.isAlive()) {
            String status = getHeroStatus(current);
            if ("Current".equals(status) || "Attacked".equals(status) || "Special".equals(status)) {
                setHeroStatus(current, "Ready");
            }
        }

        turnIndex = (turnIndex + 1) % turnOrder.size();
        Hero next = getCurrentHero();
        if (next != null && next.isAlive()) {
            setHeroStatus(next, "Current");
        }
    }

    public void delayCurrentHero() {
        if (turnOrder.isEmpty()) return;

        Hero current = getCurrentHero();
        if (current == null) return;

        int idx = turnOrder.indexOf(current);
        if (idx < 0) return;

        turnOrder.remove(idx);
        turnOrder.add(current);
        setHeroStatus(current, "Waiting");

        if (turnIndex >= turnOrder.size()) {
            turnIndex = 0;
        }

        Hero next = getCurrentHero();
        if (next != null && next != current) {
            setHeroStatus(next, "Current");
        }
    }

    public void checkBattleEnd() {
        if (!playerParty.hasLivingHeroes() || !enemyParty.hasLivingHeroes()) {
            finished = true;
        }
    }

    private void normalizeTurnIndex() {
        if (turnOrder.isEmpty()) {
            turnIndex = 0;
            return;
        }

        if (turnIndex < 0 || turnIndex >= turnOrder.size()) {
            turnIndex = 0;
        }
    }

    public void setHeroStatus(Hero hero, String status) {
        if (hero != null) {
            heroStatuses.put(hero, status);
        }
    }

    public String getHeroStatus(Hero hero) {
        if (hero == null) return "";
        if (!hero.isAlive()) return "Dead";
        return heroStatuses.getOrDefault(hero, "Ready");
    }

    public List<Hero> getTurnOrder() {
        return new ArrayList<>(turnOrder);
    }

    public Party getPlayerParty() { return playerParty; }
    public Party getEnemyParty()  { return enemyParty; }
    public boolean isFinished()   { return finished; }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
