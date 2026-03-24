package com.example.domain;

import java.util.List;
import java.util.stream.Collectors;

public class Hero {

    private String name;
    private String type;

    private int level;
    private int hp;
    private int maxHp;
    private int mana;
    private int attack;
    private int defense;
    private int experience;
    private int shield = 0;

    public Hero(String name, String type) {
        this.name = name;
        this.type = type;

        this.level = 1;
        this.hp = 100;
        this.maxHp = 100;
        this.mana = 50;
        this.attack = 5;
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

    public int getMaxHp() {
        return maxHp;
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

        if (shield > 0) {
            int absorbed = Math.min(shield, finalDamage);
            shield -= absorbed;
            finalDamage -= absorbed;
        }

        hp -= finalDamage;
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        hp += amount;
        if (hp > maxHp) hp = maxHp;
    }

    public void addMana(int amount) {
        this.mana += amount;
    }

    public void addShield(int amount) {
        this.shield += amount;
    }

    public void gainExperience(int exp) {
        experience += exp;

        int expNeeded = expToLevelUp(level);
        if (experience >= expNeeded) {
            levelUp();
            experience -= expNeeded;
        }
    }

    private int expToLevelUp(int currentLevel) {
        int total = 0;
        for (int i = 1; i <= currentLevel; i++) {
            total += 500 + 75 * i + 20 * i * i;
        }
        return total;
    }

    public void levelUp() {
        level++;
        hp += 5;
        maxHp += 5;
        mana += 2;
        attack += 1;
        defense += 1;
    }

    public boolean hasManaFor(Action action) {
        return this.mana >= action.getManaCost();
    }

    public void spendMana(int amount) {
        this.mana -= amount;
        if (this.mana < 0) this.mana = 0;
    }

    public void castSpecial(Hero target, Party playerParty, Party enemyParty) {
        switch (type) {

            case "Order" -> {
                // Protect: shield all party members for 10% of their max HP. Costs 25 mana.
                spendMana(25);
                for (Hero hero : playerParty.getHeroes()) {
                    if (hero.isAlive()) {
                        hero.addShield((int)(hero.getMaxHp() * 0.10));
                    }
                }
            }

            case "Chaos" -> {
                // Fireball: hit up to 3 enemies. Costs 30 mana.
                spendMana(30);
                List<Hero> enemies = enemyParty.getHeroes().stream()
                        .filter(Hero::isAlive)
                        .limit(3)
                        .collect(Collectors.toList());
                for (Hero enemy : enemies) {
                    enemy.takeDamage(this.attack);
                }
            }

            case "Warrior" -> {
                // Berserker attack: hit target + 2 more for 25% damage. Costs 60 mana.
                spendMana(60);
                if (target != null) target.takeDamage(this.attack);
                List<Hero> others = enemyParty.getHeroes().stream()
                        .filter(h -> h.isAlive() && !h.equals(target))
                        .limit(2)
                        .collect(Collectors.toList());
                for (Hero h : others) {
                    h.takeDamage((int)(this.attack * 0.25));
                }
            }

            case "Mage" -> {
                // Replenish: +30 mana to all friendlies, +60 to self. Costs 80 mana.
                spendMana(80);
                for (Hero hero : playerParty.getHeroes()) {
                    if (hero.isAlive()) {
                        hero.addMana(30);
                    }
                }
                this.addMana(30); // self gets 60 total (30 from loop + 30 extra)
            }
        }
    }
}