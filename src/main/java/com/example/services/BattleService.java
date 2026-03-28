package com.example.services;

import com.example.domain.Action;
import com.example.domain.ActionType;
import com.example.domain.BattleResult;
import com.example.domain.BattleState;
import com.example.domain.Hero;
import com.example.domain.Party;

public class BattleService {

    public BattleState startBattle(Party player, Party enemy) {
        return new BattleState(player, enemy);
    }

    public BattleResult executeTurn(BattleState state, Action action) {
        if (state == null) {
            return new BattleResult(false, 0, 0, "Invalid battle state");
        }

        Hero actor = state.getCurrentHero();
        Hero target = action.getTarget();

        if (actor == null) {
            state.checkBattleEnd();
            return new BattleResult(false, 0, 0, "No valid actor");
        }

        if (!actor.isAlive()) {
            state.nextTurn();
            return new BattleResult(false, 0, 0, "Actor is dead");
        }

        ActionType type = action.getType();

        if (type == ActionType.ATTACK) {
            if (target == null || !target.isAlive()) {
                return new BattleResult(false, 0, 0, "Invalid target");
            }

            target.takeDamage(actor.getAttack());
            state.checkBattleEnd();

            if (state.isFinished()) {
                return buildEndResult(state);
            }

            state.nextTurn();
            return new BattleResult(false, 0, 0, "Attack complete");
        }

        if (type == ActionType.DEFEND) {
            actor.heal(10);
            actor.addMana(5);

            state.checkBattleEnd();
            if (state.isFinished()) {
                return buildEndResult(state);
            }

            state.nextTurn();
            return new BattleResult(false, 0, 0, "Defend complete");
        }

        if (type == ActionType.WAIT) {
            state.delayCurrentHero();
            state.checkBattleEnd();

            if (state.isFinished()) {
                return buildEndResult(state);
            }

            return new BattleResult(false, 0, 0, "Wait complete");
        }

        if (type == ActionType.SPECIAL) {
            if (!actor.hasManaFor(action)) {
                return new BattleResult(false, 0, 0, "Not enough mana");
            }

            actor.castSpecial(target, state.getPlayerParty(), state.getEnemyParty());
            state.checkBattleEnd();

            if (state.isFinished()) {
                return buildEndResult(state);
            }

            state.nextTurn();
            return new BattleResult(false, 0, 0, "Special complete");
        }

        return new BattleResult(false, 0, 0, "Unknown action");
    }

    private BattleResult buildEndResult(BattleState state) {
        boolean playerWon = !state.getEnemyParty().hasLivingHeroes();

        if (playerWon) {
            int exp = 0;
            int gold = 0;

            for (Hero enemy : state.getEnemyParty().getHeroes()) {
                exp += 50 * enemy.getLevel();
                gold += 75 * enemy.getLevel();
            }

            return new BattleResult(true, exp, gold, "Player won");
        }

        return new BattleResult(false, 0, 0, "Player lost");
    }
}