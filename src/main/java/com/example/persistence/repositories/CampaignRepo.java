package com.example.persistence.repositories;

import com.example.domain.Campaign;
import com.example.persistence.GameDB;

public class CampaignRepo {

    private GameDB db = GameDB.getInstance();

    public void save(int userId, Campaign c) {
        db.saveCampaign(userId, c);
    }

    public Campaign loadByUserId(int userId) {
        return db.loadCampaign(userId);
    }
}