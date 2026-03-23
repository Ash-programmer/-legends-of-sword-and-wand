package com.example.persistence.repositories;

import com.example.domain.Campaign;
import com.example.persistence.GameDB;

public class CampaignRepo {

    private GameDB db = GameDB.getInstance();

    public void save(Campaign c) {
        db.saveCampaign(c);
    }

    public Campaign loadByUserId(int id) {
        return db.loadCampaign(id);
    }
}