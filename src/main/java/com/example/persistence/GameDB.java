package com.example.persistence;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.User;

import java.util.HashMap;
import java.util.Map;

public class GameDB {

    private static GameDB instance;

    private Map<String, User> users;
    private Map<Integer, Campaign> campaigns;
    private Map<Integer, Party> parties;

    private GameDB() {
        users = new HashMap<>();
        campaigns = new HashMap<>();
        parties = new HashMap<>();
    }

    public static GameDB getInstance() {

        if (instance == null) {
            instance = new GameDB();
        }

        return instance;
    }

    // ---------- USER ----------

    public void saveUser(User user) {
        users.put(user.getUsername(), user);
    }

    public User loadUser(String username) {
        return users.get(username);
    }

    // ---------- CAMPAIGN ----------

    public void saveCampaign(int userId, Campaign campaign) {
        campaigns.put(userId, campaign);
    }

    public Campaign loadCampaign(int userId) {
        return campaigns.get(userId);
    }

    // ---------- PARTY ----------

    public void saveParty(int id, Party party) {
        parties.put(id, party);
    }

    public Party loadParty(int id) {
        return parties.get(id);
    }
}