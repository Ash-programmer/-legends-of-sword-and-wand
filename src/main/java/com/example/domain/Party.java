package com.example.domain;

import java.util.ArrayList;
import java.util.List;

public class Party {

    private int id;
    private String name;
    private List<Hero> heroes;
    private int gold;
    private int maxSize;

    public Party() {
        this.heroes = new ArrayList<>();
        this.gold = 100;
        this.maxSize = 3;
    }

    public Party(int maxSize) {
        this.heroes = new ArrayList<>();
        this.gold = 100;
        this.maxSize = maxSize;
    }

    public int getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public List<Hero> getHeroes(){
        return heroes;
    }
    public int getGold()            { return gold; }
    public int getMaxSize()         { return maxSize; }
    public int getSize()            { return heroes.size(); }

    public void setId(int id)                   { this.id = id; }
    public void setName(String name)            { this.name = name; }
    public void setHeroes(List<Hero> heroes)    { this.heroes = heroes; }

    public boolean addHero(Hero hero) {
        if (heroes.size() >= maxSize) return false;
        heroes.add(hero);
        return true;
    }

    public boolean removeHero(Hero hero)    { return heroes.remove(hero); }
    public boolean isFull()                 { return heroes.size() >= maxSize; }

    public boolean hasLivingHeroes() {
        for (Hero h : heroes) {
            if (h.isAlive()) return true;
        }
        return false;
    }

    public void addGold(int amount) { gold += amount; }

    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public Hero getHighestLevelHero() {
        if (heroes.isEmpty()) return null;
        Hero best = heroes.get(0);
        for (Hero h : heroes) {
            if (h.getLevel() > best.getLevel()) best = h;
        }
        return best;
    }
}