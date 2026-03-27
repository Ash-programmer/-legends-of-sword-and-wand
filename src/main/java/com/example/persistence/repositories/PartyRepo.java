package com.example.persistence.repositories;

import com.example.domain.Party;
import com.example.persistence.sql.SqlGameDB;

import java.util.List;

public class PartyRepo {

    private final SqlGameDB db = SqlGameDB.getInstance();

    public void save(int userId, Party p) {
        db.saveParty(userId, p);
    }

    public Party load(int id) {
        return db.loadParty(id);
    }

    public List<Party> loadForUser(int userId) {
        return db.loadPartiesForUser(userId);
    }

    public void delete(int partyId) {
        db.deleteParty(partyId);
    }
}