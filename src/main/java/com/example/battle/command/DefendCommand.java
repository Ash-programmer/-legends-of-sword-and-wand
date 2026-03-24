package com.example.battle.command;

import com.example.domain.Hero;

public class DefendCommand implements Command {

    private final Hero actor;

    public DefendCommand(Hero actor) {
        this.actor = actor;
    }

    @Override
    public void execute() {
        actor.heal(10);
        actor.addMana(5);
    }
}