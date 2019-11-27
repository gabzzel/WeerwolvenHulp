package com.example.weerwolvenhulp;

import java.io.Serializable;

public class Card implements Serializable {

    public boolean awakes = false;

    public enum Role{
        Werewolf,
        Citizen,
        Hunter,
        Witch,
        Cupid,
        Thief,
        Seer,
        CheatingGirl
    }

    public Role role = Role.Citizen;

    public Card(){
    }

    public Card(Role _role){
        role = _role;
    }
}