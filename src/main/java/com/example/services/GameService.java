package com.example.services;

import com.example.domain.Campaign;
import com.example.domain.User;
import com.example.persistence.repositories.CampaignRepo;

public class GameService {
    private CampaignRepo campaignRepo;
    private AuthService authService;

    public GameService(CampaignRepo campaignRepo, AuthService authService) {
        this.campaignRepo = campaignRepo;
        this.authService = authService;
    }

    public void saveAndLogout() {
        User user = authService.getCurrentUser();
        if (user != null && user.hasCampaign()) {
            Campaign current = user.getCampaigns().get(0);
            campaignRepo.save(user.getUserId(), current);
        }
        authService.logout();
    }

    public void saveCurrentProgress(Campaign campaign) {
        User user = authService.getCurrentUser();
        if (user != null) {
            campaignRepo.save(user.getUserId(), campaign);
        }
    }
}