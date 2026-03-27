package com.example.domain;

public class Campaign {

    private int currentRoom;
    private Party party;
    private Inventory inventory;
    private int score;
    private boolean complete;
    private RoomType lastRoomType;

    public Campaign() {
        this.inventory = new Inventory();
        this.currentRoom = 1;
        this.score = 0;
        this.complete = false;
    }

    public Campaign(Party party) {
        this();
        this.party = party;
    }

    public int getCurrentRoom()         { return currentRoom; }
    public Party getParty()             { return party; }
    public Inventory getInventory()     { return inventory; }
    public int getScore()               { return score; }
    public boolean isComplete()         { return complete; }
    public boolean isFinished()         { return complete; }
    public RoomType getLastRoomType()   { return lastRoomType; }

    public void setCurrentRoom(int currentRoom)     { this.currentRoom = currentRoom; }
    public void setParty(Party party)               { this.party = party; }
    public void setInventory(Inventory inventory)   { this.inventory = inventory; }
    public void setScore(int score)                 { this.score = score; }
    public void setComplete(boolean complete)       { this.complete = complete; }
    public void setLastRoomType(RoomType type)      { this.lastRoomType = type; }

    public void advanceRoom() {
        currentRoom++;
        if (currentRoom > 30) {
            complete = true;
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