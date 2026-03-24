package com.example.services.usecases;

import com.example.domain.BattleState;
import com.example.domain.Party;

public class StartBattleUseCase {

    private BattleState activeBattle;

    public BattleState execute(Party player, Party enemy) {
        if (activeBattle != null && !activeBattle.isFinished()) {
            throw new IllegalStateException("A battle is already in progress.");
        }
        activeBattle = new BattleState(player, enemy);
        return activeBattle;
    }

    public BattleState getActiveBattle() {
        return activeBattle;
    }

    public void clearBattle() {
        activeBattle = null;
    }
}