package com.example.persistence.repositories;

import com.example.domain.Party;
import com.example.persistence.GameDB;

public class PartyRepo {

    private GameDB db = GameDB.getInstance();

    public void save(int id, Party p) {
        db.saveParty(id, p);
    }

    public Party load(int id) {
        return db.loadParty(id);
    }
}