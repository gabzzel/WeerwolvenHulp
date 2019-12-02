package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

public class InGame extends AppCompatActivity {

    LinearLayout container = null;
    int dayCounter = 0;
    public boolean isDay = true;
    int playerCount = 0;
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Event> pastEvents = new ArrayList<>();
    LinkedList<Event> events = new LinkedList<>();

    Event currentEvent = null;
    boolean selecting = false;
    int minPlayersToSelect = 0;
    int maxPlayersToSelect = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        container = this.findViewById(R.id.day_night_container);

        Intent intent = getIntent();
        players = (ArrayList<Player>)intent.getSerializableExtra("players");
        playerCount = intent.getIntExtra("playerCount", 4);
        findViewById(R.id.name_select_scrollview).setVisibility(View.INVISIBLE);

        HandleNextEvent(null);
    }

    void StartNewDay(){
        dayCounter++;
        isDay = true;
        AddBorder();
        FillEventQueue();
    }

    void StartNewNight(){
        isDay = false;
        AddBorder();
        FillEventQueue();
    }

    public void HandleNextEvent(View view){

        if(selecting){
            HandleSelection();
            return;
        }

        currentEvent = events.poll();

        // If we have no event
        if(currentEvent == null && isDay) StartNewNight();

        // If we have no event and it's night, start a new day!
        else if(currentEvent == null) StartNewDay();

        else{

            String header;

            // The rest of the events should be handled here!
            switch (currentEvent.type){

                case ElectMayor:
                    header = getResources().getString(R.string.name_select_header_elect_mayor);
                    ShowNameSelect(1, 1, header);
                    break;
                case Lynch:
                    header = getResources().getString(R.string.name_select_header_lynch);
                    ShowNameSelect(1, 1, header);
                    break;
                case AnnounceDead:
                    KillMarkedPlayers();
                    break;
                case WerewolfKill:
                    if(GetPlayersByRole(Card.Role.Werewolf).size() == 1){
                        header = getResources().getString(R.string.name_select_header_werewolf_kill_single);
                    }
                    else header = getResources().getString(R.string.name_select_header_werewolf_kill_plural);
                    ShowNameSelect(0, 1, header);
                    break;
                case WitchHeal:
                    header = getResources().getString(R.string.name_select_header_witch_heal);
                    ShowNameSelect(0, 1, header);
                    break;
                case WitchKill:
                    header = getResources().getString(R.string.name_select_header_witch_kill);
                    ShowNameSelect(0, 1, header);
                    break;
                case SeerLook:
                    header = getResources().getString(R.string.name_select_header_seer_look);
                    ShowNameSelect(1, 1, header);
                    break;
                case HunterKill:
                    header = getResources().getString(R.string.name_select_header_hunter_kill);
                    ShowNameSelect(1, 1, header);
                    break;
                case ThiefSwitch:
                    // TODO Handle thief
                    break;
                case CupidAffect:
                    header = getResources().getString(R.string.name_select_header_cupid_affect);
                    ShowNameSelect(2, 2, header);
                    break;
            }

        }

        // Write the event.
        WriteEvent(currentEvent);
        pastEvents.add(currentEvent);

        UpdateNextEventButton();
    }

    void UpdateNextEventButton(){

        Event e = events.peek();

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
                    events.add(new Event(Event.EventType.ThiefSwitch));
                }

                // 2. Cupid
                if(GetPlayersByRole(Card.Role.Cupid) != null){
                    events.add(new Event(Event.EventType.CupidAffect));
                }

            }
            else{

                // 1. Seer
                if(GetPlayersByRole(Card.Role.Seer) != null){
                    events.add(new Event(Event.EventType.SeerLook));
                }

                // 2. Werewolves
                events.add(new Event(Event.EventType.WerewolfKill));

                // 3. Witch
                if(GetPlayersByRole(Card.Role.Witch) != null){
                    events.add(new Event(Event.EventType.WitchHeal));
                    events.add(new Event(Event.EventType.WitchKill));
                }
            }
        }
    }

    // Get the list of players, given a role
    ArrayList<Player> GetPlayersByRole(Card.Role role){
        ArrayList<Player> ps = new ArrayList<>();

        // Go through all players. If they have the role, add them to the list.
        for(Player p : GetAlivePlayers()){ if(p.card.role == role) ps.add(p); }

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
        boolean isHeader = false;

        if(event == null){
            isHeader = true;
            if(isDay) string = getResources().getString(R.string.day_started_event, dayCounter);
            else if(dayCounter == 0) string = getResources().getString(R.string.first_night_started);
            else string = getResources().getString(R.string.night_started_event, dayCounter);
        }
        else{
            switch (event.type){

                case ElectMayor:
                    string = getResources().getString(R.string.elect_mayor_event_description);
                    break;
                case Lynch:
                    string = getResources().getString(R.string.lynch_event_description);
                    break;
                case AnnounceDead:
                    // Hoeft niet aangeroepen te worden, want dat wordt al gedaan tijdens het handlen.
                    //string = getResources().getString(R.string.announce_dead_event_description);
                    break;
                case WerewolfKill:
                    if(GetPlayersByRole(Card.Role.Werewolf).size() == 1){
                        string = getResources().getString(R.string.werewolf_kill_event_description_single);
                    }
                    else string = getResources().getString(R.string.werewolf_kill_event_description_plural);
                    break;
                case WitchHeal:
                    string = getResources().getString(R.string.witch_heal_event_description);
                    break;
                case WitchKill:
                    string = getResources().getString(R.string.witch_kill_event_description);
                    break;
                case SeerLook:
                    string = getResources().getString(R.string.seer_look_event_description);
                    break;
                case HunterKill:
                    string = getResources().getString(R.string.hunter_kill_event_description);
                    break;
                case ThiefSwitch:
                    string = getResources().getString(R.string.thief_switch_event_description);
                    break;
                case CupidAffect:
                    string = getResources().getString(R.string.cupid_affect_event_description);
                    break;
            }
        }

        WriteEvent(string, isHeader);
    }

    void WriteEvent(String string, boolean isHeader){
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

        if(isHeader){
            text.setTextSize(24);
            text.setTypeface(text.getTypeface(), Typeface.BOLD_ITALIC);
        }
        else{
            text.setTextSize(18);
        }

        container.addView(text);
        ScrollView sv = findViewById(R.id.game_progress_scrollview);
        sv.fullScroll(View.FOCUS_DOWN);
    }

    void AddBorder(){
        ImageView border = new ImageView(this);
        border.setBackgroundColor(Color.GRAY);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4);
        lp.setMargins(10, 10, 10, 10);
        border.setLayoutParams(lp);
        container.addView(border);
    }

    void HandleSelection(){
        selecting = false;

        ScrollView sv = findViewById(R.id.name_select_scrollview);
        sv.setVisibility(View.INVISIBLE);

        ArrayList<Player> selectedPlayers = currentEvent.affectedPlayers;

        switch (currentEvent.type){

            case ElectMayor:
                selectedPlayers.get(0).isMayor = true;
                break;
            case Lynch:
                selectedPlayers.get(0).KillImmediate();
                break;
            case WerewolfKill:
                if(selectedPlayers.size() == 1) selectedPlayers.get(0).markOfDeath = true;
                break;
            case WitchHeal:
                if(selectedPlayers.size() == 1) selectedPlayers.get(0).markOfDeath = false;
                break;
            case WitchKill:
                if(selectedPlayers.size() == 1) selectedPlayers.get(0).markOfDeath = true;
                break;
            case HunterKill:
                selectedPlayers.get(0).KillImmediate();
                break;
            case CupidAffect:
                selectedPlayers.get(0).faction = Player.Faction.Lovers;
                selectedPlayers.get(1).faction = Player.Faction.Lovers;
                break;
        }

    }

    public void SelectName(View view){
        Button b = (Button)view;
        String name = b.getText().toString();
        Player p = GetPlayerByName(name);

        if(currentEvent.affectedPlayers.contains(p)){
            b.setBackgroundColor(Color.GRAY);
            currentEvent.affectedPlayers.remove(p);
        }
        else if(currentEvent.affectedPlayers.size() < maxPlayersToSelect){
            b.setBackgroundColor(Color.GREEN);
            currentEvent.affectedPlayers.add(p);
        }

        if(currentEvent.affectedPlayers.size() <= maxPlayersToSelect && currentEvent.affectedPlayers.size() >= minPlayersToSelect){
            Button neb = findViewById(R.id.next_event_button);
            neb.setEnabled(true);
        }
    }

    Player GetPlayerByName(String name){
        for(Player p : players) if (p.name.equals(name)) return p;
        return null;
    }

    void ShowNameSelect(int min, int max, String headerText){

        minPlayersToSelect = min;
        maxPlayersToSelect = max;
        selecting = true;

        ScrollView sv = findViewById(R.id.name_select_scrollview);
        sv.setVisibility(View.VISIBLE);
        LinearLayout ll = findViewById(R.id.name_select_container);
        TextView header = findViewById(R.id.name_select_header);
        header.setText(headerText);

        // Remove all old buttons
        for(int i = ll.getChildCount() - 1; i > 1; i--){
            ll.removeViewAt(i);
        }

        ArrayList<Player> relevantPlayers = GetRelevantPlayers(currentEvent);
        for(int i = 0; i < relevantPlayers.size(); i++){
            Player p = relevantPlayers.get(i);
            Button b = new Button(this);
            b.setText(p.name);
            ll.addView(b);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectName(v);
                }
            });
        }

        Button neb = findViewById(R.id.next_event_button);
        neb.setEnabled(false);
        neb.setText("Kies spelers");
    }

    ArrayList<Player> GetRelevantPlayers(Event e){
        ArrayList<Player> ps = new ArrayList<>();

        switch (e.type){

            case ElectMayor:
                ps = GetAlivePlayers();
                break;
            case Lynch:
                ps = GetAlivePlayers();
                break;
            case WerewolfKill:
                ps = GetAlivePlayers();
                break;
            case WitchHeal:
                Player p = GetWerewolfVictim();
                if(p != null) ps.add(p);
                break;
            case WitchKill:
                ps = GetAlivePlayers();
                break;
            case SeerLook:
                ps = GetAlivePlayers();
                break;
            case HunterKill:
                ps = GetAlivePlayers();
                break;
            case CupidAffect:
                ps = GetAlivePlayers();
                break;
        }

        return ps;
    }

    ArrayList<Player> GetAlivePlayers(){
        ArrayList<Player> ps = new ArrayList<>();
        for(Player p : players){
            if(p.alive) ps.add(p);
        }
        return ps;
    }

    Player GetWerewolfVictim(){
        for(Player p : players){
            if(p.markOfDeath) return p;
        }
        return null;
    }

    void KillMarkedPlayers(){
        String killed = null;

        for(Player p : GetAlivePlayers()){
            if(p.Kill()){
                if(killed == null) killed = p.name;
                else killed = killed + ", " + p.name;
            }
        }

        if(killed == null){
            String s = getResources().getString(R.string.no_dead_tonight);
            WriteEvent(s, false);
        }
        else{
            String s = getResources().getString(R.string.announce_dead_event_description, killed);
            WriteEvent(s, false);
        }
    }
}
