package com.example.domain;

import java.util.ArrayList;
import java.util.List;

public class StatusReport {

    private final List<String> healedDetails;
    private final List<String> revivedDetails;
    private String message;

    public StatusReport() {
        this.healedDetails = new ArrayList<>();
        this.revivedDetails = new ArrayList<>();
        this.message = "";
    }

    public void addHealedHero(Hero hero, int hpHealed, int manaRestored) {
        healedDetails.add(hero.getName() + " healed +" + hpHealed + " HP, +" + manaRestored + " mana");
    }

    public void addRevivedHero(Hero hero, int hpHealed, int manaRestored) {
        revivedDetails.add(hero.getName() + " revived with +" + hpHealed + " HP, +" + manaRestored + " mana");
    }

    public List<String> getHealedDetails() {
        return healedDetails;
    }

    public List<String> getRevivedDetails() {
        return revivedDetails;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
