package com.example.weerwolvenhulp;

import java.io.Serializable;

public class Player implements Serializable {

    public Card card = null;
    public boolean isMayor = false;
    public String name = "Gabriel";

    public enum Faction{
        Citizens,
        Werewolf,
        Lovers
    }

    public Faction faction = Faction.Citizens;

    public Player(String _name){
        name = _name;
        isMayor = false;
        faction = Faction.Citizens;
    }

    public Player(Card.Role role, String _name){

        switch (role){

            // If we want to create a witch, create a new witch class. Witches need more info.
            case Witch:
                card = new Witch();
                break;
            case Werewolf:
                card = new Card(role);
                faction = Faction.Werewolf;
                break;
            default:
                card = new Card(role);
                break;
        }

        name = _name;
    }

}
