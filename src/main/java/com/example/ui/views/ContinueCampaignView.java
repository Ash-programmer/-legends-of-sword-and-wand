package com.example.ui.views;

import com.example.controllers.ContinueCampaignController;
import com.example.domain.Campaign;
import com.example.ui.UICommands;

import javax.swing.*;
import java.awt.*;

public class ContinueCampaignView extends JFrame implements UICommands {

    private ContinueCampaignController controller;

    private JTextField idField;
    private JTextArea output;

    public ContinueCampaignView(
            ContinueCampaignController controller) {

        this.controller = controller;
        init();
    }

    private void init() {

        setTitle("Continue");
        setSize(400,200);

        idField = new JTextField();
        output = new JTextArea();

        JButton load = new JButton("Load");

        add(idField,BorderLayout.NORTH);
        add(load,BorderLayout.CENTER);
        add(output,BorderLayout.SOUTH);

        load.addActionListener(e -> load());
    }

    private void load() {

        int id =
                Integer.parseInt(idField.getText());

        Campaign c =
                controller.loadCampaign(id);

        if(c == null)
            output.setText("None");
        else
            output.setText("Room "+c.getCurrentRoom());
    }

    public void start() {
        setVisible(true);
    }
}