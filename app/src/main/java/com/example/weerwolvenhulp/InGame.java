package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

public class InGame extends AppCompatActivity {

    LinearLayout container = null;
    int dayCounter = 0;
    public boolean isDay = true;
    int playerCount = 0;
    ArrayList<Player> players = new ArrayList<>();
    LinkedList<Event> events = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        container = this.findViewById(R.id.day_night_container);

        Intent intent = getIntent();
        players = (ArrayList<Player>)intent.getSerializableExtra("players");
        playerCount = intent.getIntExtra("playerCount", 4);

        HandleNextEvent(null);
    }

    void StartNewDay(){
        dayCounter++;
        isDay = true;
        FillEventQueue();
    }

    void StartNewNight(){
        isDay = false;
        FillEventQueue();
    }

    public void HandleNextEvent(View view){

        Event currentEvent = events.poll();



        // If we have no event
        if(currentEvent == null && isDay) StartNewNight();

        // If we have no event and it's night, start a new day!
        else if(currentEvent == null) StartNewDay();

        else{
            // The rest of the events should be handled here!
        }

        // Write the event.
        WriteEvent(currentEvent);

        Event nextEvent = events.peek();
        UpdateNextEventButton(nextEvent);
    }

    void UpdateNextEventButton(Event e){

        Button b = findViewById(R.id.next_event_button);
        if(e == null){
            if(isDay){
                b.setText(getResources().getString(R.string.start_new_night));
            }
            else{
                b.setText(getResources().getString(R.string.start_new_day));
            }
        }
        else{
            b.setText(getResources().getString(R.string.next_event));
        }
    }

    // Fill the queue with events during the Day or Night.
    void FillEventQueue(){
        if(isDay){

            if(dayCounter <= 1) events.add(new Event(Event.EventType.ElectMayor));

            else{
                // 1. Announce the dead
                events.add(new Event(Event.EventType.AnnounceDead));

                // 2. Elect mayor (if there is none)
                if(!CheckForMayor()) events.add(new Event(Event.EventType.ElectMayor));

                // 3. Lynch
                events.add(new Event(Event.EventType.Lynch));
            }
        }
        else{
            // The first night
            if(dayCounter == 0){

                // 1. Thief
                if(GetPlayersByRole(Card.Role.Thief) != null){
                    events.add(new Event(Event.EventType.HandleRole, Card.Role.Thief));
                }

                // 2. Cupid
                if(GetPlayersByRole(Card.Role.Cupid) != null){
                    events.add(new Event(Event.EventType.HandleRole, Card.Role.Cupid));
                }

            }
            else{

                // 1. Seer
                if(GetPlayersByRole(Card.Role.Seer) != null){
                    events.add(new Event(Event.EventType.HandleRole, Card.Role.Seer));
                }

                // 2. Werewolves
                events.add(new Event(Event.EventType.HandleRole, Card.Role.Werewolf));

                // 3. Witch
                if(GetPlayersByRole(Card.Role.Witch) != null){
                    events.add(new Event(Event.EventType.HandleRole, Card.Role.Witch));
                }
            }
        }
    }

    // Get the list of players, given a role
    ArrayList<Player> GetPlayersByRole(Card.Role role){
        ArrayList<Player> ps = new ArrayList<>();

        // Go through all players. If they have the role, add them to the list.
        for(Player p : players){ if(p.card.role == role) ps.add(p); }

        if(ps.size() == 0) return null;

        return ps;
    }

    // Return if there is a mayor
    boolean CheckForMayor(){
        for(Player p : players){ if(p.isMayor) return true; }
        return false;
    }

    void WriteEvent(Event event){

        String string = "";

        if(event == null){
            if(isDay) string = getResources().getString(R.string.day_started_event, dayCounter);
            else if(dayCounter == 0) string = getResources().getString(R.string.first_night_started);
            else string = getResources().getString(R.string.night_started_event, dayCounter);
        }
        else if (event.type == Event.EventType.HandleRole) {
            String roleString = getResources().getString(GetRoleStringID(event.relevantRole));
            string = getResources().getString(event.descrStringID, roleString);
        } else {
            string = getResources().getString(event.descrStringID);
        }

        WriteEvent(string);
    }

    void WriteEvent(String string){
        TextView text = new TextView(this);
        text.setGravity(Gravity.CENTER);
        text.setText(string);

        if(isDay){
            text.setBackgroundColor(Color.WHITE);
            text.setTextColor(Color.BLACK);
        }
        else{
            text.setBackgroundColor(Color.BLACK);
            text.setTextColor(0xD0FFFFFF);
        }

        container.addView(text);
    }

    public int GetRoleStringID(Card.Role role){
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
