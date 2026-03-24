package com.example.domain;

import java.util.ArrayList;
import java.util.List;

public class User {

    private int userId;
    private String username;
    private String passwordHash;
    private List<Campaign> campaigns;
    private List<Party> savedParties;

    public User(int userId, String username, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.campaigns = new ArrayList<>();
        this.savedParties = new ArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public List<Party> getSavedParties() {
        return savedParties;
    }

    public void addCampaign(Campaign campaign) {
        if (!campaigns.contains(campaign)) {
            campaigns.add(campaign);
        }
    }

    public void addParty(Party party) {
        if (savedParties.size() >= 5) {
            savedParties.remove(0);
        }
        savedParties.add(party);
    }

    public boolean hasSavedParty() {
        return !savedParties.isEmpty();
    }

    public boolean hasCampaign() {
        return !campaigns.isEmpty();
    }
}