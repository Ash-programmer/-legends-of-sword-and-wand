package com.example.battle.command;

import com.example.domain.Hero;

public class AttackCommand implements Command {

    private final Hero actor;
    private final Hero target;

    public AttackCommand(Hero actor, Hero target) {
        this.actor  = actor;
        this.target = target;
    }

    @Override
    public void execute() {
        if (target == null) return;

        int damage = Math.max(0, actor.getAttack() - target.getDefense());
        target.takeDamage(damage);
    }
}
