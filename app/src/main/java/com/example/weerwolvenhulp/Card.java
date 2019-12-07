package com.example.weerwolvenhulp;

import java.io.Serializable;

class Card implements Serializable {

    public enum Role {
        CheatingGirl,
        Citizen,
        Cupid,
        FlutePlayer,
        Hunter,
        Savior,
        Scapegoat,
        Seer,
        Thief,
        VillageElder,
        VillageIdiot,
        Werewolf,
        Witch
    }

    Role role;

    Card(Role _role){
        role = _role;
    }

    int GetRoleStringID(){
        switch (role){
            case Werewolf:
                return R.string.werewolf_name;
            case Citizen:
                return R.string.citizen_name;
            case FlutePlayer:
                return R.string.flute_player_name;
            case Hunter:
                return R.string.hunter_name;
            case Witch:
                return R.string.witch_name;
            case Cupid:
                return R.string.cupid_name;
            case Thief:
                return R.string.thief_name;
            case Savior:
                return R.string.savior_name;
            case Scapegoat:
                return R.string.scapegoat_name;
            case Seer:
                return R.string.seer_name;
            case CheatingGirl:
                return R.string.cheating_girl_name;
            case VillageElder:
                return R.string.village_elder_name;
            case VillageIdiot:
                return R.string.village_idiot_name;
        }

        return R.string.citizen_name;
    }
}