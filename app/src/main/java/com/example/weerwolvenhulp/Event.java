package com.example.weerwolvenhulp;

import java.util.ArrayList;

public class Event {

    public enum EventType{
        ElectMayor,
        Lynch,
        AnnounceDead,
        WerewolfKill,
        WitchHeal,
        WitchKill,
        SeerLook,
        HunterKill,
        ThiefSwitch,
        CupidAffect
    }

    EventType type = null;

    ArrayList<Player> affectedPlayers = new ArrayList<>();

    Event(EventType _type){
        type = _type;
    }
}
