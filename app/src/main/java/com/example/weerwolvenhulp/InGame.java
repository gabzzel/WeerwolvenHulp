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
    ArrayList<Card.Role> extraRoles = new ArrayList<>();
    ArrayList<Player> players = new ArrayList<>();
    ArrayList<Event> pastEvents = new ArrayList<>();
    LinkedList<Event> events = new LinkedList<>();

    Event currentEvent = null;

    String selectNameHeaderText;
    boolean selecting = false;
    boolean shouldSelect = false;
    int minPlayersToSelect = 0;
    int maxPlayersToSelect = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        container = this.findViewById(R.id.day_night_container);

        Intent intent = getIntent();
        players = (ArrayList<Player>)intent.getSerializableExtra("players");
        extraRoles = (ArrayList<Card.Role>)intent.getSerializableExtra("extraRoles");
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
        else if(shouldSelect){
            if(currentEvent.type == Event.EventType.ThiefSwitch) ShowCardSelect();
            else ShowNameSelect();
            return;
        }

        currentEvent = events.poll();
        UpdateNextEventButton();

        // If we have no event and it's day, start a new night!
        if(currentEvent == null && isDay) StartNewNight();

        // If we have no event and it's night, start a new day!
        else if(currentEvent == null) StartNewDay();

        else{

            // The rest of the events should be handled here!
            switch (currentEvent.type){

                case ElectMayor:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_elect_mayor);
                    PrepareNameSelect(1, 1);
                    break;

                case Lynch:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_lynch);
                    PrepareNameSelect(1, 1);
                    break;

                case AnnounceDead:
                    KillMarkedPlayers();
                    break;

                case WerewolfKill:
                    if(GetPlayersByRole(Card.Role.Werewolf, true).size() == 1){
                        selectNameHeaderText = getResources().getString(R.string.name_select_header_werewolf_kill_single);
                    }
                    else selectNameHeaderText = getResources().getString(R.string.name_select_header_werewolf_kill_plural);
                    PrepareNameSelect(0, 1);
                    break;

                case WitchHeal:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_witch_heal);
                    PrepareNameSelect(0, 1);
                    break;

                case WitchKill:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_witch_kill);
                    PrepareNameSelect(0, 1);
                    break;

                case SeerLook:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_seer_look);
                    PrepareNameSelect(1, 1);
                    break;

                case HunterKill:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_hunter_kill);
                    PrepareNameSelect(1, 1);
                    break;

                case ThiefSwitch:
                    shouldSelect = true;
                    break;

                case CupidAffect:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_cupid_affect);
                    PrepareNameSelect(2, 2);
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
        ScrollView sv = findViewById(R.id.game_progress_scrollview);
        sv.fullScroll(View.FOCUS_DOWN);

        if(selecting){
            int selectedCount = currentEvent.affectedPlayers.size();
            boolean set = (selectedCount >= minPlayersToSelect && selectedCount <= maxPlayersToSelect);
            b.setEnabled(set);
            b.setText(getResources().getString(R.string.confirm));
        }
        else if(shouldSelect){
            if(currentEvent.type == Event.EventType.ThiefSwitch) b.setText(getResources().getString(R.string.select_role_to_switch_thief));
            else b.setText(getResources().getString(R.string.next_event_button_select_players_command));
        }
        else if(e == null){
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
                if(GetPlayersByRole(Card.Role.Thief, true) != null){
                    events.add(new Event(Event.EventType.ThiefSwitch));
                }

                // 2. Cupid
                if(GetPlayersByRole(Card.Role.Cupid, true) != null){
                    events.add(new Event(Event.EventType.CupidAffect));
                }

            }
            else{

                // 1. Seer
                if(GetPlayersByRole(Card.Role.Seer, true) != null){
                    events.add(new Event(Event.EventType.SeerLook));
                }

                // 2. Werewolves
                events.add(new Event(Event.EventType.WerewolfKill));

                // 3. Witch
                if(GetPlayersByRole(Card.Role.Witch, true) != null){
                    if(WitchCanUsePotion(true)) events.add(new Event(Event.EventType.WitchHeal));
                    if(WitchCanUsePotion(false)) events.add(new Event(Event.EventType.WitchKill));
                }
            }
        }
    }

    // Get the list of players, given a role
    ArrayList<Player> GetPlayersByRole(Card.Role role, boolean shouldBeAlive){

        ArrayList<Player> ps = new ArrayList<>();
        ArrayList<Player> searchSpace = players;

        if(shouldBeAlive) searchSpace = GetAlivePlayers();

        // Go through all players. If they have the role, add them to the list.
        for(Player p : searchSpace){ if(p.GetRole() == role) ps.add(p); }

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

                case EndGame_CitizenWin:
                    string = getResources().getString(R.string.end_game_citizen_win);
                    break;
                case EndGame_LoversWin:
                    string = getResources().getString(R.string.end_game_lovers_win);
                    break;
                case EndGame_WerewolfWin:
                    string = getResources().getString(R.string.end_game_werewolf_win);
                    break;
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
                    if(GetPlayersByRole(Card.Role.Werewolf, true).size() == 1){
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
    }

    void WriteDeath(Player p){
        TextView text = new TextView(this);
        text.setGravity(Gravity.CENTER);
        String s = p.name + " (" + getResources().getString(p.GetCard().GetRoleStringID()) + ")";
        text.setText(s);
        text.setBackgroundColor(Color.RED);
        text.setTypeface(text.getTypeface(), Typeface.BOLD);
        text.setTextColor(0xD0FFFFFF);
        container.addView(text);
        ScrollView sv = findViewById(R.id.game_progress_scrollview);
        sv.fullScroll(View.FOCUS_DOWN);
    }

    void AddBorder(){
        ImageView border = new ImageView(this);
        border.setBackgroundColor(Color.GRAY);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4);
        lp.setMargins(10, 20, 10, 20);
        border.setLayoutParams(lp);
        container.addView(border);
    }

    void HandleSelection(){
        selecting = false;
        shouldSelect = false;

        ScrollView sv = findViewById(R.id.name_select_scrollview);
        sv.setVisibility(View.INVISIBLE);

        ArrayList<Player> selectedPlayers = currentEvent.affectedPlayers;

        switch (currentEvent.type){

            case ElectMayor:
                selectedPlayers.get(0).isMayor = true;
                break;
            case Lynch:
                KillPlayerImmediate(selectedPlayers.get(0));
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
                KillPlayerImmediate(selectedPlayers.get(0));
                break;
            case CupidAffect:
                selectedPlayers.get(0).faction = Player.Faction.Lovers;
                selectedPlayers.get(1).faction = Player.Faction.Lovers;
                break;
        }

        UpdateNextEventButton();

    }

    public void SelectName(View view){
        Button b = (Button)view;
        String name = b.getText().toString();
        Player p = GetPlayerByName(name);
        LinearLayout ll = findViewById(R.id.name_select_container);

        if(currentEvent.affectedPlayers.contains(p)){
            b.setBackgroundColor(Color.LTGRAY);
            currentEvent.affectedPlayers.remove(p);
        }
        else if(currentEvent.affectedPlayers.size() < maxPlayersToSelect){
            b.setBackgroundColor(Color.GREEN);
            currentEvent.affectedPlayers.add(p);
        }
        // If we have already selected a player, but want to switch by selecting another...
        else if(maxPlayersToSelect == 1 && currentEvent.affectedPlayers.size() == 1 && !currentEvent.affectedPlayers.contains(p)){
            currentEvent.affectedPlayers.clear();
            currentEvent.affectedPlayers.add(p);

            for(int i = ll.getChildCount() - 1; i > 1; i--){
                View toClear = ll.getChildAt(i);
                toClear.setBackgroundColor(Color.LTGRAY);
            }

            b.setBackgroundColor(Color.GREEN);
        }

        UpdateNextEventButton();
    }

    Player GetPlayerByName(String name){
        for(Player p : players) if (p.name.equals(name)) return p;
        return null;
    }

    void PrepareNameSelect(int min, int max){
        shouldSelect = true;
        minPlayersToSelect = min;
        maxPlayersToSelect = max;
    }

    void ShowNameSelect(){

        selecting = true;
        shouldSelect = false;

        ScrollView sv = findViewById(R.id.name_select_scrollview);
        sv.setVisibility(View.VISIBLE);
        LinearLayout ll = findViewById(R.id.name_select_container);
        TextView header = findViewById(R.id.name_select_header);
        header.setText(selectNameHeaderText);

        TextView selectPlayerDescr = findViewById(R.id.select_players_text);

        // Single select
        if(maxPlayersToSelect == minPlayersToSelect && maxPlayersToSelect != 1){
            selectPlayerDescr.setText(getResources().getString(R.string.select_player_fixed_number, maxPlayersToSelect));
        }
        // Multiple select
        else if(maxPlayersToSelect == minPlayersToSelect){
            selectPlayerDescr.setText((getResources().getString(R.string.select_player_fixed_number, maxPlayersToSelect)));
        }
        else{
            selectPlayerDescr.setText(getResources().getString(R.string.select_player_2_way_choice, minPlayersToSelect, maxPlayersToSelect));
        }

        // Remove all old buttons
        for(int i = ll.getChildCount() - 1; i > 1; i--){
            ll.removeViewAt(i);
        }

        ArrayList<Player> relevantPlayers = GetRelevantPlayers(currentEvent);
        for(int i = 0; i < relevantPlayers.size(); i++){
            Player p = relevantPlayers.get(i);
            Button b = new Button(this);
            b.setText(p.name);
            b.setTextSize(14);
            b.setBackgroundColor(Color.LTGRAY);
            ll.addView(b);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectName(v);
                }
            });
        }

        if(relevantPlayers.size() == 0){
            TextView nobody = new TextView(this);
            nobody.setText(getResources().getString(R.string.nobody_to_select));
            nobody.setTextSize(20);
            ll.addView(nobody);
        }

        UpdateNextEventButton();
    }

    void ShowCardSelect(){
        LinearLayout ll = findViewById(R.id.card_select_container);
        ll.setVisibility(View.VISIBLE);
        for(int i = 1; i < ll.getChildCount(); i++){
            Button b;
            if(i == 2) b = findViewById(R.id.thief_choose_1);
            else b = findViewById(R.id.thief_choose_2);
            Card.Role role = extraRoles.get(i - 1);

            switch (role){

                case Werewolf:
                    b.setBackgroundResource(R.drawable.werewolfcard200);
                    break;
                case Citizen:
                    b.setBackgroundResource(R.drawable.citizencard200);
                    break;
                case Hunter:
                    b.setBackgroundResource(R.drawable.huntercard200);
                    break;
                case Witch:
                    b.setBackgroundResource(R.drawable.witchcard200);
                    break;
                case Cupid:
                    b.setBackgroundResource(R.drawable.cupidcard200);
                    break;
                case Thief:
                    b.setBackgroundResource(R.drawable.thiefcard200);
                    break;
                case Seer:
                    b.setBackgroundResource(R.drawable.seercard200);
                    break;
                case CheatingGirl:
                    b.setBackgroundResource(R.drawable.cheatinggirlcard200);
                    break;
            }
        }

        UpdateNextEventButton();
    }

    public void HandleCardSelect(View view){
        int index;
        // Om een of andere reden is dit omgedraaid?
        if(view.getId() == R.id.thief_choose_1) index = 1;
        else index = 0;

        Card.Role newRole = extraRoles.get(index);
        Player thief = GetPlayersByRole(Card.Role.Thief, true).get(0);
        thief.SetRole(newRole, true);
        //WriteEvent("De dief heeft nu de rol " + newRole.toString(), false);
        LinearLayout ll = findViewById(R.id.card_select_container);
        ll.setVisibility(View.INVISIBLE);
        Button neb = findViewById(R.id.next_event_button);
        neb.setEnabled(true);
        shouldSelect = false;
        UpdateNextEventButton();
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

        for(Player p : GetAlivePlayers()) KillPlayer(p);

        // Remove death markers from the players
        for(Player p : GetAlivePlayers()){
            p.markOfDeath = false;
        }
    }

    // Kill a player, but only if they are marked for death!
    boolean KillPlayer(Player p){

        if(!p.markOfDeath) return false;

        KillPlayerImmediate(p);
        return true;

    }

    // Kill a player, regardless of their marks. This function also plays out the consequence (like lovers)
    void KillPlayerImmediate(Player p){
        p.alive = false;
        p.markOfDeath = true;

        WriteDeath(p);
        if(EndGameIfPossible()) return;

        // If the player is one of the lovers, kill the other lover.
        if(p.faction == Player.Faction.Lovers){
            Player otherLover = GetOtherLover(p);
            KillPlayerWithoutConsequence(otherLover);
        }

        // If the player is the hunter, add the relevant event to the queue
        if(p.GetRole() == Card.Role.Hunter){
            events.add(0, new Event(Event.EventType.HunterKill));
        }
    }

    // Kill a player, regardless of everything and without doing anything. Used for example when dying of lovers' sorrow.
    void KillPlayerWithoutConsequence(Player p){
        p.alive = false;
        WriteDeath(p);
        EndGameIfPossible();
    }

    Player GetOtherLover(Player other){

        for(Player p : players){
            if(p != other && p.faction == Player.Faction.Lovers){
                return p;
            }
        }

        return null;
    }

    boolean EndGameIfPossible(){

        ArrayList<Player> werewolves = GetPlayersByRole(Card.Role.Werewolf, true);
        ArrayList<Player> alivePlayers = GetAlivePlayers();
        boolean done = false;

        // If there are no more werewolves, we are done.
        if(werewolves == null || werewolves.isEmpty()){
            WriteEvent(new Event(Event.EventType.EndGame_CitizenWin));
            done = true;
        }
        // If there are only werewolves, we are done.
        else if(alivePlayers.size() == werewolves.size()){
            WriteEvent(new Event(Event.EventType.EndGame_WerewolfWin));
            done = true;
        }
        // If there are only 2 players left and they are the lovers, they win!
        else if(alivePlayers.size() == 2 && alivePlayers.get(0).faction == Player.Faction.Lovers && alivePlayers.get(1).faction == Player.Faction.Lovers){
            WriteEvent(new Event(Event.EventType.EndGame_LoversWin));
            done = true;
        }

        if(done){
            events.clear();
            findViewById(R.id.next_event_button).setEnabled(false);
        }

        return done;
    }

    boolean WitchCanUsePotion(boolean healing){

        if(pastEvents.size() == 0) return true;

        else if(healing){
            for(Event e : pastEvents){
                if(e != null && e.type == Event.EventType.WitchHeal && e.affectedPlayers.size() > 0) return false;
            }
            return true;
        }
        else{
            for(Event e : pastEvents){
                if(e != null && e.type == Event.EventType.WitchKill && e.affectedPlayers.size() > 0) return false;
            }
            return true;
        }
    }
}
