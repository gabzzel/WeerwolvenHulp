package com.example.weerwolvenhulp;

import java.util.ArrayList;

public class Event {

    public enum EventType{
        EndGame_WerewolfWin,
        EndGame_CitizenWin,
        EndGame_LoversWin,
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
