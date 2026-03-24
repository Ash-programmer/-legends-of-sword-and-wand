package com.example.services;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.Room;
import com.example.domain.Score;
import com.example.domain.User;
import com.example.persistence.repositories.CampaignRepo;

public class CampaignService {

    private CampaignRepo campaignRepo;

    public CampaignService(CampaignRepo campaignRepo) {
        this.campaignRepo = campaignRepo;
    }

    public Campaign startCampaign(User user, Party party) {
        Campaign campaign = new Campaign(party);
        user.addCampaign(campaign);
        campaignRepo.save(user.getUserId(), campaign);
        return campaign;
    }

    public Room nextRoom(Campaign campaign) {
        campaign.advanceRoom();
        int number = campaign.getCurrentRoom();
        return Room.randomRoom(number);
    }

    public void saveProgress(int userId, Campaign campaign) {
        campaignRepo.save(userId, campaign);
    }

    public Campaign loadProgress(int userId) {
        return campaignRepo.loadByUserId(userId);
    }

    public Score endCampaign(User user, Campaign campaign) {
        campaign.calculateFinalScore();
        Score score = Score.calculate(user, campaign);
        campaignRepo.save(user.getUserId(), campaign);
        return score;
    }
}