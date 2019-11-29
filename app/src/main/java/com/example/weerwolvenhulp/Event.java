package com.example.weerwolvenhulp;

public class Event {

    public enum EventType{
        ElectMayor,
        HandleRole,
        StartNextDayOrNight,
        Lynch,
        AnnounceDead
    }

    EventType type = null;
    Card.Role relevantRole = null;
    int descrStringID = 0;

    Event(EventType _type){
        type = _type;
        SetDescription();
    }

    Event(EventType _type, Card.Role _role){
        type = _type;
        relevantRole = _role;
        SetDescription();
    }

    void SetDescription(){
        switch (type){

            case ElectMayor:
                descrStringID = R.string.elect_mayor_event_description;
                break;
            case HandleRole:
                descrStringID = R.string.handle_role_event_description;
                break;
            case StartNextDayOrNight:
                descrStringID = R.string.start_next_DoN_event_description;
                break;
            case Lynch:
                descrStringID = R.string.lynch_event_description;
                break;
            case AnnounceDead:
                descrStringID = R.string.announce_dead_event_description;
                break;
        }
    }
}
