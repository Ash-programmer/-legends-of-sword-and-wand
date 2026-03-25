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
    private int attack;
    private int defense;
    private int experience;
    private int shield = 0;
    private boolean stunned = false;

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

    public String getName()    { return name; }
    public String getType()    { return type; }
    public int getLevel()      { return level; }
    public int getHp()         { return hp; }
    public int getMaxHp()      { return maxHp; }
    public int getMana()       { return mana; }
    public int getAttack()     { return attack; }
    public int getDefense()    { return defense; }
    public boolean isAlive()   { return hp > 0; }
    public boolean isStunned() { return stunned; }

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

    public void replenishMana(int amount) {
        this.mana += amount;
    }

    public void addShield(int amount) {
        this.shield += amount;
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
        this.mana -= amount;
        if (this.mana < 0) this.mana = 0;
    }

    public void castSpecial(Hero target, Party playerParty, Party enemyParty) {
        switch (type) {
            case "Order"   -> castOrderSpecial(target, playerParty, enemyParty);
            case "Chaos"   -> castChaosSpecial(target, playerParty, enemyParty);
            case "Warrior" -> castWarriorSpecial(target, playerParty, enemyParty);
            case "Mage"    -> castMageSpecial(target, playerParty, enemyParty);
        }
    }

    private void castOrderSpecial(Hero target, Party playerParty, Party enemyParty) {
        if (target != null && mana >= 35) {
            spendMana(35);
            Hero lowest = playerParty.getHeroes().stream()
                    .filter(Hero::isAlive)
                    .min((a, b) -> Integer.compare(a.getHp(), b.getHp()))
                    .orElse(null);
            if (lowest != null) {
                lowest.heal((int)(lowest.getMaxHp() * 0.25));
            }
        } else {
            spendMana(25);
            for (Hero hero : playerParty.getHeroes()) {
                if (hero.isAlive()) {
                    hero.addShield((int)(hero.getMaxHp() * 0.10));
                }
            }
        }
    }

    private void castChaosSpecial(Hero target, Party playerParty, Party enemyParty) {
        if (mana >= 40) {
            spendMana(40);
            List<Hero> enemies = enemyParty.getHeroes().stream()
                    .filter(Hero::isAlive)
                    .collect(Collectors.toList());
            if (target != null) {
                enemies.remove(target);
                Collections.shuffle(enemies);
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
        } else {
            spendMana(30);
            List<Hero> enemies = enemyParty.getHeroes().stream()
                    .filter(Hero::isAlive)
                    .limit(3)
                    .collect(Collectors.toList());
            for (Hero enemy : enemies) {
                enemy.takeDamage(this.attack);
            }
        }
    }

    private void castWarriorSpecial(Hero target, Party playerParty, Party enemyParty) {
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

    private void castMageSpecial(Hero target, Party playerParty, Party enemyParty) {
        spendMana(80);
        for (Hero hero : playerParty.getHeroes()) {
            if (hero.isAlive()) {
                hero.addMana(30);
            }
        }
        this.addMana(30);
    }

    public void loseExperiencePercent(double percent) {
        int loss = (int)(experience * percent);
        experience -= loss;
        if (experience < 0) experience = 0;
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
}