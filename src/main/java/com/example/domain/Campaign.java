package com.example.domain;

public class Campaign {

    public static final int FINAL_ROOM = 30;

    private int currentRoom;
    private Party party;
    private Inventory inventory;
    private int score;
    private boolean complete;
    private RoomType lastRoomType;

    public Campaign() {
        this.inventory = new Inventory();
        this.currentRoom = 1;          // next battle room starts at 1
        this.score = 0;
        this.complete = false;
        this.lastRoomType = RoomType.INN; // start at inn before room 1
    }

    public Campaign(Party party) {
        this();
        this.party = party;
    }

    public int getCurrentRoom()       { return currentRoom; }
    public Party getParty()           { return party; }
    public Inventory getInventory()   { return inventory; }
    public int getScore()             { return score; }
    public boolean isComplete()       { return complete; }
    public boolean isFinished()       { return complete; }
    public RoomType getLastRoomType() { return lastRoomType; }

    public void setCurrentRoom(int currentRoom)   { this.currentRoom = currentRoom; }
    public void setParty(Party party)             { this.party = party; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
    public void setScore(int score)               { this.score = score; }
    public void setComplete(boolean complete)     { this.complete = complete; }
    public void setLastRoomType(RoomType type)    { this.lastRoomType = type; }

    public void advanceRoom() {
        currentRoom++;
        if (currentRoom > FINAL_ROOM) {
            complete = true;
        }
    }

    public boolean isBattleRoom() {
        return lastRoomType == RoomType.BATTLE;
    }

    public void addScore(int value) {
        score += value;
    }

    public void calculateFinalScore() {
        if (party == null) return;

        score += party.getGold() / 10;
        for (Hero h : party.getHeroes()) {
            score += h.getLevel() * 100;
        }
    }

    public int getRoomsCleared() {
        return Math.max(0, currentRoom - 1);
    }

    public int getRoomsRemaining() {
        if (complete) return 0;
        return Math.max(0, FINAL_ROOM - currentRoom + 1);
    }

    public String getLocationDescription() {
        if (complete) {
            return "Campaign Complete";
        }

        if (lastRoomType == RoomType.INN) {
            return "Inn before room " + currentRoom;
        }

        return "Battle room " + currentRoom;
    }
}
