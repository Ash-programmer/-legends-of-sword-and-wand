package com.example.services;

import com.example.domain.*;
import com.example.persistence.repositories.CampaignRepo;
import com.example.persistence.repositories.PartyRepo;
import com.example.persistence.repositories.UserRepo;

import java.util.List;

public class CampaignService {

    private final CampaignRepo campaignRepo;
    private final PartyRepo partyRepo;
    private final UserRepo userRepo;

    public CampaignService(CampaignRepo campaignRepo, PartyRepo partyRepo, UserRepo userRepo) {
        this.campaignRepo = campaignRepo;
        this.partyRepo = partyRepo;
        this.userRepo = userRepo;
    }

    public Campaign startCampaign(User user, Party party) {
        Campaign campaign = new Campaign(party);
        user.getCampaigns().clear();
        user.addCampaign(campaign);
        campaignRepo.save(user.getUserId(), campaign);
        return campaign;
    }

    public Room nextRoom(Campaign campaign) {
        campaign.advanceRoom();
        Room room = Room.randomRoom(campaign.getCurrentRoom());
        campaign.setLastRoomType(room.getType());
        return room;
    }

    public void saveProgress(int userId, Campaign campaign) {
        campaignRepo.save(userId, campaign);
    }

    public Campaign loadProgress(int userId) {
        return campaignRepo.loadByUserId(userId);
    }

    public boolean canExitCampaign(boolean battleInProgress, Campaign campaign) {
        return campaign != null && !battleInProgress;
    }

    public Score endCampaign(User user, Campaign campaign, boolean keepParty, Integer replacePartyId) {
        campaign.calculateFinalScore();
        Score score = Score.calculate(user, campaign);

        user.setScore(score.getValue());
        user.setRanking(score.getValue());
        userRepo.save(user);

        if (keepParty && campaign.getParty() != null) {
            List<Party> savedParties = partyRepo.loadForUser(user.getUserId());

            if (savedParties.size() < 5) {
                partyRepo.save(user.getUserId(), campaign.getParty());
            } else if (replacePartyId != null) {
                partyRepo.delete(replacePartyId);
                partyRepo.save(user.getUserId(), campaign.getParty());
            }
        }

        campaign.setComplete(true);
        campaignRepo.deleteByUserId(user.getUserId());
        return score;
    }
}