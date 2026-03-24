package com.example.controllers;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.Room;
import com.example.domain.Score;
import com.example.domain.User;
import com.example.services.CampaignService;

public class CampaignController {

    private CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    public Campaign startCampaign(User user, Party party) {
        return campaignService.startCampaign(user, party);
    }

    public Room nextRoom(Campaign campaign) {
        return campaignService.nextRoom(campaign);
    }

    public void saveProgress(int userId, Campaign campaign) {
        campaignService.saveProgress(userId, campaign);
    }

    public Score endCampaign(User user, Campaign campaign) {
        return campaignService.endCampaign(user, campaign);
    }
}