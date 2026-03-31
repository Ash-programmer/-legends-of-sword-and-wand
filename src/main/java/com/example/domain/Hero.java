package com.example.domain;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Hero {

    private String name;
    private String type;

    private int level;
    private int hp;
    private int maxHp;
    private int mana;
    private int maxMana;
    private int attack;
    private int defense;
    private int experience;
    private int shield;
    private boolean stunned;

    public Hero(String name, String type) {
        this.name = name;
        this.type = type;

        this.level = 1;
        this.hp = 100;
        this.maxHp = 100;
        this.mana = 50;
        this.maxMana = 50;

        this.attack = 12;
        this.defense = 4;

        this.experience = 0;
        this.shield = 0;
        this.stunned = false;
    }

    public String getName()    { return name; }
    public String getType()    { return type; }
    public int getLevel()      { return level; }
    public int getHp()         { return hp; }
    public int getMaxHp()      { return maxHp; }
    public int getMana()       { return mana; }
    public int getMaxMana()    { return maxMana; }
    public int getAttack()     { return attack; }
    public int getDefense()    { return defense; }
    public int getExperience() { return experience; }
    public int getShield()     { return shield; }
    public boolean isAlive()   { return hp > 0; }
    public boolean isStunned() { return stunned; }


    public void takeDamage(int damage) {
        int finalDamage = damage - defense;

        if (damage > 0 && finalDamage < 1) {
            finalDamage = 1;
        }

        if (shield > 0) {
            int absorbed = Math.min(shield, finalDamage);
            shield -= absorbed;
            finalDamage -= absorbed;
        }

        hp -= finalDamage;
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        hp += amount;
        if (hp > maxHp) hp = maxHp;
    }

    public void addMana(int amount) {
        if (amount <= 0) return;
        mana += amount;
        if (mana > maxMana) mana = maxMana;
    }

    public void replenishMana(int amount) {
        addMana(amount);
    }

    public void addShield(int amount) {
        if (amount <= 0) return;
        shield += amount;
    }

    public void setStunned(boolean stunned) {
        this.stunned = stunned;
    }

    public void clearStun() {
        this.stunned = false;
    }

    public boolean hasManaFor(Action action) {
        return this.mana >= action.getManaCost();
    }

    public void spendMana(int amount) {
        mana -= amount;
        if (mana < 0) mana = 0;
    }

    public void castSpecial(Hero target, Party playerParty, Party enemyParty) {
        switch (type) {
            case "Order"   -> castOrderSpecial(playerParty);
            case "Chaos"   -> castChaosSpecial(target, enemyParty);
            case "Warrior" -> castWarriorSpecial(target, enemyParty);
            case "Mage"    -> castMageSpecial(playerParty);
        }
    }

    private void castOrderSpecial(Party playerParty) {
        spendMana(35);

        Hero lowest = playerParty.getHeroes().stream()
                .filter(Hero::isAlive)
                .min((a, b) -> Integer.compare(a.getHp(), b.getHp()))
                .orElse(null);

        if (lowest != null) {
            lowest.heal((int) (lowest.getMaxHp() * 0.25));
        }
    }

    private void castChaosSpecial(Hero target, Party enemyParty) {
        spendMana(40);

        List<Hero> enemies = enemyParty.getHeroes().stream()
                .filter(Hero::isAlive)
                .collect(Collectors.toList());

        if (enemies.isEmpty()) return;

        if (target != null) {
            enemies.remove(target);
            enemies.add(0, target);
        } else {
            Collections.shuffle(enemies);
        }

        double damage = this.attack;
        for (Hero enemy : enemies) {
            enemy.takeDamage((int) damage);
            damage *= 0.25;
            if (damage < 1) break;
        }
    }

    private void castWarriorSpecial(Hero target, Party enemyParty) {
        spendMana(60);

        if (target != null) {
            target.takeDamage(this.attack);
        }

        List<Hero> others = enemyParty.getHeroes().stream()
                .filter(h -> h.isAlive() && h != target)
                .limit(2)
                .collect(Collectors.toList());

        for (Hero h : others) {
            h.takeDamage((int) (this.attack * 0.25));
        }
    }

    private void castMageSpecial(Party playerParty) {
        spendMana(80);

        for (Hero hero : playerParty.getHeroes()) {
            if (hero.isAlive()) {
                hero.addMana(30);
            }
        }
    }

    public int loseCurrentLevelExperiencePercent(double percent) {
        if (percent <= 0) return 0;

        int lost = (int) Math.floor(experience * percent);
        experience -= lost;

        if (experience < 0) {
            experience = 0;
        }

        return lost;
    }

    public void gainExperience(int amount) {
        if (amount <= 0) return;

        experience += amount;

        while (experience >= expToNextLevel(level)) {
            experience -= expToNextLevel(level);
            levelUp();
        }
    }

    public int expToNextLevel(int currentLevel) {
        int nextLevel = currentLevel + 1;
        return 500 + 75 * nextLevel + 20 * nextLevel * nextLevel;
    }


    private void levelUp() {
        level++;

        maxHp += 20;
        hp = maxHp;

        maxMana += 10;
        mana = maxMana;

        attack += 5;
        defense += 3;
    }
}
