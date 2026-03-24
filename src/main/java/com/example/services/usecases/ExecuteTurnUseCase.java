package com.example.services.usecases;

import com.example.battle.command.AttackCommand;
import com.example.battle.command.DefendCommand;
import com.example.domain.*;

import java.util.LinkedList;
import java.util.Queue;

public class ExecuteTurnUseCase {

    private final Queue<Hero> waitQueue = new LinkedList<>();

    public BattleResult execute(BattleState state, Action action) {

        if (state == null || state.isFinished()) {
            return new BattleResult(false, 0, 0, "No active battle.");
        }

        Hero actor = action.getActor();
        Hero target = action.getTarget();

        if (actor == null) {
            return new BattleResult(false, 0, 0, "Invalid actor.");
        }
        if (!actor.isAlive()) {
            return new BattleResult(false, 0, 0, actor.getName() + " is dead and cannot act.");
        }

        Hero expectedActor = waitQueue.isEmpty()
                ? state.getCurrentHero()
                : waitQueue.peek();

        if (!actor.equals(expectedActor)) {
            return new BattleResult(false, 0, 0, "It is not " + actor.getName() + "'s turn.");
        }

        switch (action.getType()) {

            case ATTACK -> {
                if (target == null) {
                    return new BattleResult(false, 0, 0, "Attack requires a target.");
                }
                new AttackCommand(actor, target).execute();
            }

            case DEFEND -> {
                new DefendCommand(actor).execute();
            }

            case WAIT -> {
                if (!waitQueue.isEmpty()) {
                    return new BattleResult(false, 0, 0, actor.getName() + " already chose to wait.");
                }
                waitQueue.add(actor);
                state.nextTurn();
                return new BattleResult(false, 0, 0, actor.getName() + " is waiting.");
            }

            case SPECIAL -> {
                if (!actor.hasManaFor(action)) {
                    return new BattleResult(false, 0, 0,
                            actor.getName() + " does not have enough mana.");
                }
                actor.castSpecial(target, state.getPlayerParty(), state.getEnemyParty());
            }
        }

        if (!waitQueue.isEmpty() && actor.equals(waitQueue.peek())) {
            waitQueue.poll();
        }

        state.checkBattleEnd();
        if (state.isFinished()) {
            return buildFinalResult(state);
        }

        state.nextTurn();
        return new BattleResult(false, 0, 0, "Turn complete.");
    }

    private BattleResult buildFinalResult(BattleState state) {
        boolean playerWon = !state.getEnemyParty().hasLivingHeroes();

        if (playerWon) {
            int totalExp  = 0;
            int totalGold = 0;
            for (Hero enemy : state.getEnemyParty().getHeroes()) {
                totalExp  += 50 * enemy.getLevel();
                totalGold += 75 * enemy.getLevel();
            }

            long living = state.getPlayerParty().getHeroes().stream()
                    .filter(Hero::isAlive)
                    .count();
            int expPerHero = living > 0 ? totalExp / (int) living : 0;

            return new BattleResult(true, expPerHero, totalGold,
                    "Victory! Gained " + expPerHero + " exp each and " + totalGold + " gold.");
        } else {
            return new BattleResult(false, 0, 0,
                    "Defeat! You lose 10% gold and 30% of current level experience.");
        }
    }
}