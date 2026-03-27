package com.example.domain;

import java.util.ArrayList;
import java.util.List;

public class User {

    private int id;
    private String username;
    private String password;
    private int gold;
    private int score;
    private int ranking;
    private List<Campaign> campaigns;
    private List<Party> savedParties;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.gold = 0;
        this.score = 0;
        this.ranking = 0;
        this.campaigns = new ArrayList<>();
        this.savedParties = new ArrayList<>();
    }

    public User(int id, String username, String password) {
        this(username, password);
        this.id = id;
    }

    public int getId()                       { return id; }
    public String getUsername()              { return username; }
    public String getPassword()              { return password; }
    public String getPasswordHash()          { return password; }
    public int getUserId()                   { return id; }
    public int getGold()                     { return gold; }
    public int getScore()                    { return score; }
    public int getRanking()                  { return ranking; }
    public List<Campaign> getCampaigns()     { return campaigns; }
    public List<Party> getSavedParties()     { return savedParties; }

    public void setId(int id)               { this.id = id; }
    public void setGold(int gold)           { this.gold = gold; }
    public void setScore(int score)         { this.score = score; }
    public void setRanking(int ranking)     { this.ranking = ranking; }

    public void addCampaign(Campaign campaign) {
        if (!campaigns.contains(campaign)) {
            campaigns.add(campaign);
        }
    }

    public void addParty(Party party) {
        savedParties.add(party);
    }

    public boolean hasSavedParty()  { return !savedParties.isEmpty(); }
    public boolean hasCampaign()    { return !campaigns.isEmpty(); }
}