package com.example.domain;

public class Action {

    private ActionType type;
    private Hero actor;
    private Hero target;
    private int manaCost;

    public Action(ActionType type, Hero actor, Hero target, int manaCost) {
        this.type = type;
        this.actor = actor;
        this.target = target;
        this.manaCost = manaCost;
    }

    public ActionType getType() {
        return type;
    }

    public Hero getActor() {
        return actor;
    }

    public Hero getTarget() {
        return target;
    }

    public int getManaCost() {
        return manaCost;
    }
}