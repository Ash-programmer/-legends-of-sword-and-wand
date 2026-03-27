package com.example.persistence.repositories;

import com.example.domain.Campaign;
import com.example.persistence.sql.SqlGameDB;

public class CampaignRepo {

    private final SqlGameDB db = SqlGameDB.getInstance();

    public void save(int userId, Campaign c) {
        db.saveCampaign(userId, c);
    }

    public Campaign loadByUserId(int userId) {
        return db.loadCampaign(userId);
    }

    public void deleteByUserId(int userId) {
        db.deleteCampaign(userId);
    }
}