package com.example.weerwolvenhulp;

import java.util.ArrayList;

public class Event {

    public enum EventType{
        EndGame_WerewolfWin,
        EndGame_CitizenWin,
        EndGame_LoversWin,
        EndGame_FlutePlayerWin,
        ElectMayor,
        Lynch,
        AnnounceDead,
        WerewolfKill,
        WitchHeal,
        WitchKill,
        SeerLook,
        HunterKill,
        ThiefSwitch,
        CupidAffect,
        FlutePlayerBewitch,
        AwakeBewitched
    }

    EventType type;
    private int dayPart;
    ArrayList<Player> affectedPlayers = new ArrayList<>();

    Event(EventType _type, int _dayPart){
        type = _type;
        dayPart = _dayPart;
    }

    int getDayPart(){
        return dayPart;
    }
}
