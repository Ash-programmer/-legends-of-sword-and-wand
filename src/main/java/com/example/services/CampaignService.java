package com.example.services;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.RoomType;
import com.example.domain.Score;
import com.example.domain.User;
import com.example.persistence.repositories.CampaignRepo;
import com.example.persistence.repositories.PartyRepo;
import com.example.persistence.repositories.UserRepo;

import java.util.Comparator;
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
        campaign.setCurrentRoom(1);
        campaign.setLastRoomType(RoomType.INN);

        user.getCampaigns().clear();
        user.addCampaign(campaign);
        campaignRepo.save(user.getUserId(), campaign);
        return campaign;
    }

    public RoomType visitNextRoom(Campaign campaign) {
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign cannot be null");
        }

        campaign.advanceRoom();

        if (campaign.isComplete()) {
            return campaign.getLastRoomType();
        }

        RoomType nextType = roomTypeFor(campaign.getCurrentRoom());
        campaign.setLastRoomType(nextType);
        return nextType;
    }

    public RoomType previewNextRoomType(Campaign campaign) {
        if (campaign == null || campaign.isComplete()) {
            return null;
        }
        int nextRoom = campaign.getCurrentRoom() + 1;
        if (nextRoom > 30) {
            return null;
        }
        return roomTypeFor(nextRoom);
    }

    private RoomType roomTypeFor(int roomNumber) {
        if (roomNumber <= 1) {
            return RoomType.INN;
        }
        return roomNumber % 5 == 0 ? RoomType.INN : RoomType.BATTLE;
    }

    public void saveProgress(int userId, Campaign campaign) {
        campaignRepo.save(userId, campaign);
    }

    public Campaign loadProgress(int userId) {
        return campaignRepo.loadByUserId(userId);
    }

    public boolean canExitCampaign(boolean battleInProgress, Campaign campaign) {
        if (campaign == null || battleInProgress) {
            return false;
        }

        RoomType type = campaign.getLastRoomType();
        return type == RoomType.INN || type == RoomType.BATTLE;
    }

    public boolean isCampaignComplete(Campaign campaign) {
        return campaign != null && (campaign.getCurrentRoom() > 30 || campaign.isComplete());
    }

    public Score endCampaign(User user, Campaign campaign, boolean keepParty, Integer replacePartyId) {
        campaign.calculateFinalScore();
        Score score = Score.calculate(user, campaign);

        user.setScore(user.getScore() + score.getValue());
        userRepo.save(user);
        recalculateRankings();

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

    public void recalculateRankings() {
        List<User> users = userRepo.findAll();
        users.sort(Comparator.comparingInt(User::getScore).reversed().thenComparing(User::getUsername));

        int rank = 1;
        for (User entry : users) {
            entry.setRanking(rank++);
            userRepo.save(entry);
        }
    }
}
