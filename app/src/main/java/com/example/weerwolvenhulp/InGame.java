package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class InGame extends AppCompatActivity {

    LinearLayout container = null;
    int dayCounter = 0;
    public boolean isDay = false;
    int playerCount = 0;
    ArrayList<Player> players = new ArrayList<>();
    PriorityQueue<Event> events = new PriorityQueue<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        container = this.findViewById(R.id.day_night_container);
        Intent intent = getIntent();
        players = (ArrayList<Player>)intent.getSerializableExtra("players");
        playerCount = intent.getIntExtra("playerCount", 4);
        StartNewNight();
    }

    public void StartNewDay(){
        dayCounter++;
        isDay = true;
        TextView text = new TextView(this);
        text.setText("Dag " + dayCounter + " is begonnen!");
        container.addView(text);

        FillEventQueue();

    }

    public void StartNewNight(){
        isDay = false;
        TextView text = new TextView(this);
        text.setText("Nacht " + dayCounter + " is begonnen!");
        container.addView(text);

        FillEventQueue();

    }

    public void HandleDayNightSwitch(View v){
        if(isDay){
            StartNewNight();
        }
        else{
            StartNewDay();
        }
    }

    void HandleNextEvent(View view){

        Event currentEvent = events.poll();
        Event nextEvent = events.peek();

    }

    void FillEventQueue(){
        if(isDay){

            // 1. Announce the dead
            events.add(new Event(Event.EventType.AnnounceDead));

            // 2. Elect mayor (if there is none)
            if(!CheckForMayor()) events.add(new Event(Event.EventType.ElectMayor));

            // 3. Lynch
            events.add(new Event(Event.EventType.Lynch));

            // 4. Start the next night!
            events.add(new Event(Event.EventType.StartNextDayOrNight));
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

            events.add(new Event(Event.EventType.StartNextDayOrNight));

        }
    }

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
}
