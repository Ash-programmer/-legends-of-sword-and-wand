package com.example;

import com.example.controllers.*;
import com.example.persistence.repositories.*;
import com.example.services.*;
import com.example.ui.views.*;

public class Main {

    public static void main(String[] args) {

        // ---------- REPOS ----------

        UserRepo userRepo = new UserRepo();
        CampaignRepo campaignRepo = new CampaignRepo();
        PartyRepo partyRepo = new PartyRepo();

        // ---------- SERVICES ----------

        AuthService authService = new AuthService(userRepo);

        PartyService partyService = new PartyService();

        InventoryService inventoryService = new InventoryService();

        CampaignService campaignService =
                new CampaignService(campaignRepo);

        BattleService battleService = new BattleService();

        InnService innService =
                new InnService(inventoryService, partyService);

        InvitationService invitationService =
                new InvitationService(userRepo);


        // ---------- CONTROLLERS ----------

        AuthController authController =
                new AuthController(authService);

        CampaignController campaignController =
                new CampaignController(campaignService);

        BattleController battleController =
                new BattleController(battleService);

        InnController innController =
                new InnController(innService);

        PvPInviteController pvpController =
                new PvPInviteController(invitationService);

        ContinueCampaignController continueController =
                new ContinueCampaignController(campaignService);

        ExitCampaignController exitController =
                new ExitCampaignController(campaignService);


        // ---------- VIEWS ----------

        AuthView authView =
                new AuthView(authController);

        CampaignView campaignView =
                new CampaignView(campaignController);

        BattleView battleView =
                new BattleView(battleController);

        InnView innView =
                new InnView(innController);

        PvPInviteView pvpView =
                new PvPInviteView(pvpController);

        ContinueCampaignView continueView =
                new ContinueCampaignView(continueController);


        // ---------- START ----------

        authView.start();

    }
}