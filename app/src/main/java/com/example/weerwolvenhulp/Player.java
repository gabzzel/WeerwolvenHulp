package com.example.weerwolvenhulp;

import java.io.Serializable;

public class Player implements Serializable {

    Card card;
    boolean isMayor = false;
    public String name;
    boolean alive = true;
    boolean markOfDeath = false;

    public enum Faction{
        Citizens,
        Werewolf,
        Lovers
    }

    Faction faction = Faction.Citizens;

    Player(Card.Role role, String _name){

        if (role == Card.Role.Werewolf) {
            card = new Card(role);
            faction = Faction.Werewolf;
        } else {
            card = new Card(role);
        }

        name = _name;
    }

    public void Reset(){
        card = null;
        isMayor = false;
        name = "";
        alive = true;
        markOfDeath = false;
    }
}
