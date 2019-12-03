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

    int GetRoleStringID(){
        switch (role){
            case Werewolf:
                return R.string.werewolf_name;
            case Citizen:
                return R.string.citizen_name;
            case Hunter:
                return R.string.hunter_name;
            case Witch:
                return R.string.witch_name;
            case Cupid:
                return R.string.cupid_name;
            case Thief:
                return R.string.thief_name;
            case Seer:
                return R.string.seer_name;
            case CheatingGirl:
                return R.string.cheating_girl_name;
        }

        return R.string.citizen_name;
    }
}