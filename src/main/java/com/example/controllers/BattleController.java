package com.example.controllers;

import com.example.domain.Action;
import com.example.domain.BattleResult;
import com.example.domain.BattleState;
import com.example.domain.Party;
import com.example.services.BattleService;
import com.example.services.usecases.ExecuteTurnUseCase;
import com.example.services.usecases.StartBattleUseCase;

public class BattleController {

    private final StartBattleUseCase startBattleUseCase;
    private final ExecuteTurnUseCase executeTurnUseCase;

    public BattleController(BattleService battleService) {
        this.startBattleUseCase = new StartBattleUseCase();
        this.executeTurnUseCase = new ExecuteTurnUseCase();
    }

    public BattleState startBattle(Party player, Party enemy) {
        return startBattleUseCase.execute(player, enemy);
    }

    public BattleResult executeTurn(BattleState state, Action action) {
        BattleResult result = executeTurnUseCase.execute(state, action);

        if (state.isFinished()) {
            startBattleUseCase.clearBattle();
        }

        return result;
    }
}