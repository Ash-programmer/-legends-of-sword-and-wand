package com.example.domain;

public class Campaign {

    private int currentRoom;
    private Party party;
    private Inventory inventory;
    private int score;
    private boolean finished;

    public Campaign(Party party) {
        this.party = party;
        this.inventory = new Inventory();
        this.currentRoom = 1;
        this.score = 0;
        this.finished = false;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public Party getParty() {
        return party;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getScore() {
        return score;
    }

    public boolean isFinished() {
        return finished;
    }

    public void advanceRoom() {
        currentRoom++;
        if (currentRoom > 30) {
            finished = true;
        }
    }

    public boolean isBattleRoom() {
        return Math.random() < 0.6;
    }

    public void addScore(int value) {
        score += value;
    }

    public void calculateFinalScore() {
        score += party.getGold() / 10;
        for (Hero h : party.getHeroes()) {
            score += h.getLevel() * 100;
        }
    }
}