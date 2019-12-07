package com.example.weerwolvenhulp;

import java.io.Serializable;

public class Player implements Serializable {

    private Card card;
    boolean isMayor = false;
    boolean bewitched = false; // A 'Flute Player' special
    public String name;
    private int lives = 1;
    int daysSurvived = 0;
    int nightsSurvived = 0;
    Faction faction = Faction.Citizens;

    public enum Faction{
        Citizens,
        Werewolf,
        Lovers
    }

    public enum DeathCause {
        Hunter,
        LoversSorrow,
        Lynch,
        Werewolf,
        Witch

    }

    Player(Card.Role role, String _name){

        card = new Card(role);

        if (role == Card.Role.Werewolf) {
            faction = Faction.Werewolf;
        }
        else if(role == Card.Role.VillageElder){
            lives = 2;
        }

        name = _name;
    }

    Card getCard(){
        return card;
    }

    Card.Role getRole(){
        return card.role;
    }

    void setRole(Card.Role role, boolean changeFaction){
        card.role = role;

        if (changeFaction){
            if (role == Card.Role.Werewolf) faction = Faction.Werewolf;
            else faction = Faction.Citizens;
        }
    }

    boolean IsAlive(){
        return lives > 0;
    }

    boolean IsDead(){
        return !IsAlive();
    }

    boolean Kill(DeathCause cause){

        // If we have 0 lives left (i.e. we are dead), return true
        if(lives == 0){
            return true;
        }

        // If we are the village idiot and they try to kill us by lynching, nothing happens
        else if(getRole() == Card.Role.VillageIdiot && cause == DeathCause.Lynch){
            isMayor = false;
            return false;
        }

        // If we are the village elder and we are killed by a werewolf, remove a live and return if we are dead
        else if(getRole() == Card.Role.VillageElder && cause == DeathCause.Werewolf){
            lives--;
            return IsDead();
        }

        else{
            lives--;
            return IsDead();
        }
    }

    void Heal(){
        if(getRole() == Card.Role.VillageElder && lives < 2) lives++;
        else if(lives < 1) lives++;
    }

    public void Reset(){
        card = null;
        isMayor = false;
        name = "";
        lives = 1;

        if(getRole() == Card.Role.VillageElder) lives = 2;
    }
}
