package com.example.persistence.repositories;

import com.example.domain.User;
import com.example.persistence.GameDB;

public class UserRepo {

    private GameDB db = GameDB.getInstance();

    public void save(User user) {
        db.saveUser(user);
    }

    public User findByUsername(String username) {
        return db.loadUser(username);
    }
}