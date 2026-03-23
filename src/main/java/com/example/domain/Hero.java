package com.example.domain;


public class Hero {

    private String name;
    private String type;

    private int level;
    private int hp;
    private int mana;
    private int attack;
    private int defense;
    private int experience;

    public Hero(String name, String type) {
        this.name = name;
        this.type = type;

        this.level = 1;
        this.hp = 100;
        this.mana = 50;
        this.attack = 10;
        this.defense = 5;
        this.experience = 0;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getHp() {
        return hp;
    }

    public int getMana() {
        return mana;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void takeDamage(int damage) {
        int finalDamage = damage - defense;
        if (finalDamage < 0) finalDamage = 0;

        hp -= finalDamage;

        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        hp += amount;
    }

    public void gainExperience(int exp) {
        experience += exp;

        if (experience >= 100) {
            levelUp();
            experience = 0;
        }
    }

    public void levelUp() {
        level++;
        hp += 20;
        mana += 10;
        attack += 5;
        defense += 3;
    }
}