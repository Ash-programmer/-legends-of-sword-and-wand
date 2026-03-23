package com.example.services;

import com.example.domain.User;
import com.example.persistence.repositories.UserRepo;

public class AuthService {

    private UserRepo userRepo;
    private User currentUser;

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public boolean register(String username, String password) {

        if (username == null || username.isEmpty()) {
            return false;
        }

        if (password == null || password.isEmpty()) {
            return false;
        }

        User existing = userRepo.findByUsername(username);

        if (existing != null) {
            return false;
        }

        String hash = Integer.toString(password.hashCode());

        User user = new User(0, username, hash);

        userRepo.save(user);

        return true;
    }

    public User login(String username, String password) {

        User user = userRepo.findByUsername(username);

        if (user == null) {
            return null;
        }

        String hash = Integer.toString(password.hashCode());

        if (!user.getPasswordHash().equals(hash)) {
            return null;
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