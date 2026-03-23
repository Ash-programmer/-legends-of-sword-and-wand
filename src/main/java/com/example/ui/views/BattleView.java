package com.example.ui.views;

import com.example.controllers.BattleController;
import com.example.domain.Action;
import com.example.domain.ActionType;
import com.example.domain.BattleResult;
import com.example.domain.BattleState;
import com.example.domain.Hero;
import com.example.domain.Party;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class BattleView extends JFrame implements UICommands {

    private BattleController controller;

    private JTextArea output;

    private BattleState state;

    public BattleView(BattleController controller) {
        this.controller = controller;
        init();
    }

    private void init() {

        setTitle("Battle");
        setSize(500,400);

        output = new JTextArea();

        JButton start = new JButton("Start");
        JButton attack = new JButton("Attack");

        JPanel p = new JPanel();

        p.add(start);
        p.add(attack);

        add(new JScrollPane(output),BorderLayout.CENTER);
        add(p, BorderLayout.SOUTH);

        start.addActionListener(e -> startBattle());
        attack.addActionListener(e -> attack());
    }

    private void startBattle() {

        Party a = new Party();
        a.addHero(new Hero("A","Warrior"));

        Party b = new Party();
        b.addHero(new Hero("B","Enemy"));

        state = controller.startBattle(a,b);

        output.append("Battle start\n");
    }

    private void attack() {

        if(state == null) return;

        Hero actor = state.getCurrentHero();

        Hero target =
                state.getEnemyParty().getHeroes().get(0);

        Action act =
                new Action(ActionType.ATTACK,actor,target);

        BattleResult r =
                controller.executeTurn(state,act);

        output.append(r.getMessage()+"\n");

    }

    public void start() {
        setVisible(true);
    }
}