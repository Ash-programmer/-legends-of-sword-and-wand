package com.example.services;

import com.example.domain.Campaign;
import com.example.domain.Party;
import com.example.domain.User;
import com.example.persistence.repositories.CampaignRepo;
import com.example.persistence.repositories.PartyRepo;
import com.example.persistence.repositories.UserRepo;

import java.util.List;

public class AuthService {

    private final UserRepo userRepo;
    private final CampaignRepo campaignRepo;
    private final PartyRepo partyRepo;

    private User currentUser;

    public AuthService(UserRepo userRepo, CampaignRepo campaignRepo, PartyRepo partyRepo) {
        this.userRepo = userRepo;
        this.campaignRepo = campaignRepo;
        this.partyRepo = partyRepo;
    }

    public boolean register(String username, String password) {
        if (username == null || username.isBlank()) return false;
        if (password == null || password.isBlank()) return false;

        User existing = userRepo.findByUsername(username);
        if (existing != null) return false;

        String hash = Integer.toString(password.hashCode());
        User user = new User(0, username, hash);
        userRepo.save(user);
        return true;
    }

    public User login(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user == null) return null;

        String hash = Integer.toString(password.hashCode());
        if (!user.getPasswordHash().equals(hash)) return null;

        user.getCampaigns().clear();
        user.getSavedParties().clear();

        Campaign savedCampaign = campaignRepo.loadByUserId(user.getUserId());
        if (savedCampaign != null) {
            user.addCampaign(savedCampaign);
        }

        List<Party> savedParties = partyRepo.loadForUser(user.getUserId());
        for (Party p : savedParties) {
            user.addParty(p);
        }

        currentUser = user;
        return user;
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}