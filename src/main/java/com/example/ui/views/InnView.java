package com.example.ui.views;

import com.example.controllers.InnController;
import com.example.domain.*;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class InnView extends JFrame implements UICommands {

    private InnController controller;

    private JTextArea output;

    private Party party;
    private Inventory inventory;

    public InnView(InnController controller) {
        this.controller = controller;
        init();
    }

    private void init() {

        setTitle("Inn");
        setSize(500,400);

        output = new JTextArea();

        JButton load = new JButton("Load");
        JButton status = new JButton("Status");
        JButton buy = new JButton("Buy");
        JButton recruit = new JButton("Recruit");

        JPanel p = new JPanel();

        p.add(load);
        p.add(status);
        p.add(buy);
        p.add(recruit);

        add(new JScrollPane(output),BorderLayout.CENTER);
        add(p,BorderLayout.SOUTH);

        load.addActionListener(e -> load());
        status.addActionListener(e -> status());
        buy.addActionListener(e -> buy());
        recruit.addActionListener(e -> recruit());
    }

    private void load() {

        party = new Party();
        party.addHero(new Hero("Hero","Warrior"));

        inventory = new Inventory();

        output.append("Loaded\n");
    }

    private void status() {

        StatusReport r =
                controller.getStatus(party);

        output.append(r.getMessage()+"\n");
    }

    private void buy() {

        Item i =
                new Item("Potion",10,"heal",20);

        ActionResult r =
                controller.purchaseItem(
                        party,inventory,i);

        output.append(r.getMessage()+"\n");
    }

    private void recruit() {

        Hero h =
                new Hero("Mage","Mage");

        ActionResult r =
                controller.recruitHero(party,h);

        output.append(r.getMessage()+"\n");
    }

    public void start() {
        setVisible(true);
    }
}