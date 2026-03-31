package com.example.persistence.repositories;

import com.example.domain.User;
import com.example.persistence.sql.SqlGameDB;

import java.util.List;

public class UserRepo {

    private final SqlGameDB db = SqlGameDB.getInstance();

    public void save(User user) {
        db.saveUser(user);
    }

    public User findByUsername(String username) {
        return db.loadUser(username);
    }

    public User findById(int userId) {
        return db.loadUserById(userId);
    }

    public List<User> findAll() {
        return db.loadAllUsers();
    }
}
