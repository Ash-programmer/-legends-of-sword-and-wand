package com.example;

import com.example.controllers.*;
import com.example.persistence.repositories.*;
import com.example.services.*;
import com.example.ui.views.AuthView;

public class Main {

    public static AuthController authController;
    public static CampaignController campaignController;
    public static BattleController battleController;
    public static InnController innController;
    public static PvPInviteController pvpController;
    public static ContinueCampaignController continueController;
    public static ExitCampaignController exitController;

    public static void main(String[] args) {

        UserRepo userRepo = new UserRepo();
        CampaignRepo campaignRepo = new CampaignRepo();
        PartyRepo partyRepo = new PartyRepo();

        AuthService authService = new AuthService(userRepo, campaignRepo, partyRepo);
        PartyService partyService = new PartyService();
        InventoryService inventoryService = new InventoryService();
        CampaignService campaignService = new CampaignService(campaignRepo, partyRepo, userRepo);
        BattleService battleService = new BattleService();
        InnService innService = new InnService(inventoryService, partyService);
        InvitationService invitationService = new InvitationService(userRepo, partyRepo);

        authController = new AuthController(authService);
        campaignController = new CampaignController(campaignService);
        battleController = new BattleController(battleService);
        innController = new InnController(innService);
        pvpController = new PvPInviteController(invitationService);
        continueController = new ContinueCampaignController(campaignService);
        exitController = new ExitCampaignController(campaignService);

        AuthView authView = new AuthView(authController);
        authView.start();
    }
}
