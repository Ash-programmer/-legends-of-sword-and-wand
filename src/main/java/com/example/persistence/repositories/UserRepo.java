package com.example.persistence.repositories;

import com.example.domain.User;
import com.example.persistence.sql.SqlGameDB;

public class UserRepo {

    private final SqlGameDB db = SqlGameDB.getInstance();

    public void save(User user) {
        db.saveUser(user);
    }

    public User findByUsername(String username) {
        return db.loadUser(username);
    }
}