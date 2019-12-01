package com.example.weerwolvenhulp;

import java.io.Serializable;

public class Card implements Serializable {

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

    Role role = Role.Citizen;

    Card(){
    }

    Card(Role _role){
        role = _role;
    }


}