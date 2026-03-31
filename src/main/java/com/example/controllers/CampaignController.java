package com.example.controllers;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.RoomType;
import com.example.domain.Score;
import com.example.domain.User;
import com.example.services.CampaignService;

public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    public Campaign startCampaign(User user, Party party) {
        return campaignService.startCampaign(user, party);
    }

    public RoomType visitNextRoom(Campaign campaign) {
        return campaignService.visitNextRoom(campaign);
    }

    public RoomType previewNextRoomType(Campaign campaign) {
        return campaignService.previewNextRoomType(campaign);
    }

    public void saveProgress(int userId, Campaign campaign) {
        campaignService.saveProgress(userId, campaign);
    }

    public Campaign loadCampaign(int userId) {
        return campaignService.loadProgress(userId);
    }

    public boolean canExitCampaign(boolean battleInProgress, Campaign campaign) {
        return campaignService.canExitCampaign(battleInProgress, campaign);
    }

    public boolean isCampaignComplete(Campaign campaign) {
        return campaignService.isCampaignComplete(campaign);
    }

    public Score endCampaign(User user, Campaign campaign, boolean keepParty, Integer replacePartyId) {
        return campaignService.endCampaign(user, campaign, keepParty, replacePartyId);
    }
}
