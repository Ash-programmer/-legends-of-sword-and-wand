package com.example.controllers;

import com.example.domain.User;
import com.example.services.AuthService;

public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public boolean register(String username, String password) {
        return authService.register(username, password);
    }

    public User login(String username, String password) {
        return authService.login(username, password);
    }

    public void logout() {
        authService.logout();
    }
}