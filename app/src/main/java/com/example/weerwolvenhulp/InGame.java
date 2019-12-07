package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;

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
    int dayPartCounter = 0; // Nights are uneven, days are even.
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

    boolean villageElderLives = true;

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

    /**
     * Start a new day.
     */
    void StartNewDay(){
        dayPartCounter++;
        for(Player p : GetAlivePlayers()) p.daysSurvived++;
        isDay = true;
        AddBorder();
        FillEventQueue();
        WriteEvent(null);
    }

    /**
     * Start a new night.
     */
    void StartNewNight(){
        dayPartCounter++;
        isDay = false;
        for(Player p : GetAlivePlayers()) p.nightsSurvived++;
        AddBorder();
        FillEventQueue();
        WriteEvent(null);
    }

    /**
     * Handle the next event.
     * @param view The Button that called this Event.
     */
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

            // Write the event.
            WriteEvent(currentEvent);

            // The rest of the events should be handled here!
            switch (currentEvent.type){

                case EndGame_WerewolfWin:
                case EndGame_CitizenWin:
                case EndGame_LoversWin:
                    break;
                case ElectMayor:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_elect_mayor);
                    PrepareNameSelect(1, 1);
                    break;

                case Lynch:
                    selectNameHeaderText = getResources().getString(R.string.name_select_header_lynch);
                    PrepareNameSelect(1, 1);
                    break;

                case AnnounceDead:
                    AnnounceDead();
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

                case FlutePlayerBewitch:
                    selectNameHeaderText = getResources().getString(R.string.flute_player_bewitch_event_description);
                    PrepareNameSelect(2, 2);
                    break;

                case AwakeBewitched:
                    break;
            }
        }

        pastEvents.add(currentEvent);

        UpdateNextEventButton();
    }

    /**
     * Update the Next Event Button to show the correct string.
     */
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

    /**
     * Fill the event Queue, based on day/night, alive characters and past and current events
     */
    void FillEventQueue(){
        if(isDay){

            if(dayPartCounter == 2) events.add(new Event(Event.EventType.ElectMayor, dayPartCounter));

            else{
                // 1. Announce the dead
                events.add(new Event(Event.EventType.AnnounceDead, dayPartCounter));

                // 2. Elect mayor (if there is none)
                if(GetMayor() == null) events.add(new Event(Event.EventType.ElectMayor, dayPartCounter));

                // 3. Lynch
                events.add(new Event(Event.EventType.Lynch, dayPartCounter));
            }
        }
        else{
            // The first night
            if(dayPartCounter == 1){

                // 1. Thief
                if(GetPlayersByRole(Card.Role.Thief, true) != null){
                    events.add(new Event(Event.EventType.ThiefSwitch, dayPartCounter));
                }

                // 2. Cupid
                if(GetPlayersByRole(Card.Role.Cupid, true) != null){
                    events.add(new Event(Event.EventType.CupidAffect, dayPartCounter));
                }

            }
            else{

                // 1. Seer
                if(GetPlayersByRole(Card.Role.Seer, true) != null){
                    events.add(new Event(Event.EventType.SeerLook, dayPartCounter));
                }

                // 2. Werewolves
                events.add(new Event(Event.EventType.WerewolfKill, dayPartCounter));

                // 3. Witch
                if(GetPlayersByRole(Card.Role.Witch, true) != null){
                    if(WitchCanUsePotion(true)) events.add(new Event(Event.EventType.WitchHeal, dayPartCounter));
                    if(WitchCanUsePotion(false)) events.add(new Event(Event.EventType.WitchKill, dayPartCounter));
                }

                // 4. Flute Player
                if(GetPlayersByRole(Card.Role.FlutePlayer, true) != null){
                    events.add(new Event(Event.EventType.FlutePlayerBewitch, dayPartCounter));
                    events.add(new Event(Event.EventType.AwakeBewitched, dayPartCounter));
                }
            }
        }
    }

    void WriteEvent(Event event){

        String string = "";
        boolean isHeader = false;

        if(event == null){
            isHeader = true;
            if(isDay) string = getResources().getString(R.string.day_started_event, (int)(dayPartCounter/2));
            else if(dayPartCounter == 1) string = getResources().getString(R.string.first_night_started);
            else string = getResources().getString(R.string.night_started_event, (int)((dayPartCounter + 1) / 2));
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
                case EndGame_FlutePlayerWin:
                    string = getString(R.string.end_game_fluteplayer_win);
                    break;
                case ElectMayor:
                    string = getResources().getString(R.string.elect_mayor_event_description);
                    break;
                case Lynch:
                    string = getResources().getString(R.string.lynch_event_description);
                    break;
                case AnnounceDead:
                    ArrayList<Player> victims = GetPlayersKilledInNight(dayPartCounter - 1);
                    if(victims == null || victims.size() == 0) string = getString(R.string.no_dead_tonight);
                    else if(victims.size() == 1) getString(R.string.announce_dead_event_description_single);
                    else string = getResources().getString(R.string.announce_dead_event_description_plural, victims.size());
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
                case FlutePlayerBewitch:
                    string = getString(R.string.flute_player_bewitch_event_description);
                    break;
                case AwakeBewitched:
                    string = getString(R.string.awake_bewitched_event_description);
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
        String s = p.name + " (" + getResources().getString(p.getCard().GetRoleStringID()) + ")";
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
                KillPlayer(selectedPlayers.get(0), Player.DeathCause.Lynch);
                break;
            case WerewolfKill:
                if(selectedPlayers.size() == 1){
                    KillPlayer(selectedPlayers.get(0), Player.DeathCause.Werewolf);
                    WriteEvent("De weerwolf heeft " + selectedPlayers.get(0).name + " vermoord.", false);
                }

                break;
            case WitchHeal:
                if(selectedPlayers.size() == 1){
                    selectedPlayers.get(0).Heal();
                    WriteEvent("De heks heeft " + selectedPlayers.get(0).name + " levend gemaakt.", false);
                }
                break;
            case WitchKill:
                if(selectedPlayers.size() == 1) selectedPlayers.get(0).Kill(Player.DeathCause.Witch);
                break;
            case HunterKill:
                KillPlayer(selectedPlayers.get(0), Player.DeathCause.Hunter);
                break;
            case CupidAffect:
                selectedPlayers.get(0).faction = Player.Faction.Lovers;
                selectedPlayers.get(1).faction = Player.Faction.Lovers;
                break;
            case FlutePlayerBewitch:
                selectedPlayers.get(0).bewitched = true;
                selectedPlayers.get(1).bewitched = true;
                break;
        }

        UpdateNextEventButton();

    }

    public void SelectName(View view){
        Button b = (Button)view; // The button with the name that we clicked on
        String name = b.getText().toString(); // The name in that button
        Player p = GetPlayerByName(name); // Get the player with that name
        LinearLayout ll = findViewById(R.id.name_select_container);

        ArrayList<Player> causingPlayers = GetCausingPlayers(currentEvent);

        if(currentEvent.affectedPlayers.contains(p)){

            if(causingPlayers != null && causingPlayers.contains(p)){
                b.setBackgroundColor(Color.DKGRAY);
            }
            else b.setBackgroundColor(Color.GRAY);
            b.setTextColor(0xD0FFFFFF);
            currentEvent.affectedPlayers.remove(p);
        }
        else if(currentEvent.affectedPlayers.size() < maxPlayersToSelect){
            b.setBackgroundColor(Color.GREEN);
            b.setTextColor(0xFF2B2B2B);
            currentEvent.affectedPlayers.add(p);
        }
        // If we have already selected a player, but want to switch by selecting another...
        else if(maxPlayersToSelect == 1 && currentEvent.affectedPlayers.size() == 1 && !currentEvent.affectedPlayers.contains(p)){
            currentEvent.affectedPlayers.clear();
            currentEvent.affectedPlayers.add(p);

            for(int i = ll.getChildCount() - 1; i > 1; i--){
                View toClear = ll.getChildAt(i);
                toClear.setBackgroundColor(Color.GRAY);
                ((Button)toClear).setTextColor(0xD0FFFFFF);
            }

            b.setBackgroundColor(Color.GREEN);
        }

        UpdateNextEventButton();
    }

    /**
     * Prepare the environment for a name selection.
     * @param min The minimum number of players that should be selected
     * @param max The maximum number of players that should be selected
     */
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

        // Get all relevant players for this event
        ArrayList<Player> relevantPlayers = GetRelevantPlayers(currentEvent);

        // Get the causing player(s)
        ArrayList<Player> causingPlayers = GetCausingPlayers(currentEvent);

        // Create a new button for every player
        for(int i = 0; i < relevantPlayers.size(); i++){
            Player p = relevantPlayers.get(i);
            Button b = new Button(this);
            b.setText(p.name);
            b.setTextSize(14);
            b.setBackgroundColor(Color.GRAY);
            b.setTextColor(0xD0FFFFFF);

            // Set the text bold and italic for players that caused this event.
            if(causingPlayers != null && causingPlayers.contains(p)){
                b.setTypeface(b.getTypeface(), Typeface.BOLD_ITALIC);
                b.setBackgroundColor(Color.DKGRAY);
            }

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
            nobody.setTextColor(0xD0FFFFFF);
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
        thief.setRole(newRole, true);
        //WriteEvent("De dief heeft nu de rol " + newRole.toString(), false);
        LinearLayout ll = findViewById(R.id.card_select_container);
        ll.setVisibility(View.INVISIBLE);
        Button neb = findViewById(R.id.next_event_button);
        neb.setEnabled(true);
        shouldSelect = false;
        UpdateNextEventButton();
    }

    // --------- Getting Players --------- //

    /**
     * @param role = The role of the players to return
     * @param shouldBeAlive = If the returned array should only contain alive players
     * @return A ArrayList of all players matching the specified role. Null if none found.
     */
    ArrayList<Player> GetPlayersByRole(Card.Role role, boolean shouldBeAlive){

        ArrayList<Player> ps = new ArrayList<>();
        ArrayList<Player> searchSpace = players;

        if(shouldBeAlive) searchSpace = GetAlivePlayers();

        // Go through all players. If they have the role, add them to the list.
        for(Player p : searchSpace){ if(p.getRole() == role) ps.add(p); }

        if(ps.size() == 0) return null;

        return ps;
    }

    /**
     * @param faction The Faction of the players to return
     * @param shouldBeAlive If the players returned should all be alive
     * @return An ArrayList with the players in the specified faction, or null when there are none.
     */
    ArrayList<Player> GetPlayersByFaction(Player.Faction faction, boolean shouldBeAlive){

        // Create a new arraylist, which is going to be our return
        ArrayList<Player> ps = new ArrayList<>();

        // Set our search space as wide as possible, which is all players.
        ArrayList<Player> searchSpace = players;

        // If we should only return alive players, limit our search space to alive players.
        if(shouldBeAlive) searchSpace = GetAlivePlayers();

        for(Player p : searchSpace) if(p.faction == faction) ps.add(p);

        // Return null if we found nothing, return the ArrayList with relevant players
        if(ps.size() == 0) return null;
        return ps;
    }

    ArrayList<Player> GetRelevantPlayers(Event e){
        ArrayList<Player> ps = new ArrayList<>();

        switch (e.type){

            case ElectMayor:
            case Lynch:
                ps = GetAlivePlayers();

                // Get the village idiot if revealed...
                Player villageIdiot = VillageIdiotRevealed();

                // If the village idiot is revealed, he/she cannot be selected for mayor or be lynched
                if(villageIdiot != null) ps.remove(villageIdiot);

                break;

            case WitchHeal:
                Player p = GetWerewolfVictim();
                if(p != null) ps.add(p);
                break;

            case FlutePlayerBewitch:
                ps = GetBewitchedPlayers(false, true);
                break;

            default:
                ps = GetAlivePlayers();
                break;
        }

        return ps;
    }

    ArrayList<Player> GetAlivePlayers(){
        ArrayList<Player> ps = new ArrayList<>();
        for(Player p : players){
            if(p.IsAlive()) ps.add(p);
        }
        return ps;
    }

    ArrayList<Player> GetCausingPlayers(Event e){

        switch (e.type){
            case EndGame_WerewolfWin:
                return GetPlayersByFaction(Player.Faction.Werewolf, false);
            case EndGame_CitizenWin:
                return GetPlayersByFaction(Player.Faction.Citizens, false);
            case EndGame_LoversWin:
                return GetPlayersByFaction(Player.Faction.Lovers, false);
            case WerewolfKill:
                return GetPlayersByRole(Card.Role.Werewolf, true);
            case WitchHeal:
            case WitchKill:
                return GetPlayersByRole(Card.Role.Witch, true);
            case SeerLook:
                return GetPlayersByRole(Card.Role.Seer, true);
            case HunterKill:
                return GetPlayersByRole(Card.Role.Hunter, true);
            case CupidAffect:
                return GetPlayersByRole(Card.Role.Cupid, true);
        }

        return null;
    }

    ArrayList<Player> GetPlayersKilledInNight(int nightNumber){
        ArrayList<Player> ps = new ArrayList<>();

        for(Player p : players){
            if(p.nightsSurvived == nightNumber && p.IsDead()) ps.add(p);
        }

        if(ps.size() == 0) return null;

        return ps;
    }

    ArrayList<Player> GetBewitchedPlayers(boolean bewitched, boolean shouldBeAlive){
        ArrayList<Player> searchSpace = players;
        if(shouldBeAlive) searchSpace = GetAlivePlayers();

        ArrayList<Player> ps = new ArrayList<>();
        for(Player p : searchSpace){
            if(p.bewitched == bewitched) ps.add(p);
        }

        return ps;
    }

    Player GetPlayerByName(String name){
        for(Player p : players) if (p.name.equals(name)) return p;
        return null;
    }

    Player GetWerewolfVictim(){

        Player victim = null;

        if(pastEvents != null && pastEvents.size() > 0){
            for(Event e : pastEvents){
                if(e != null && e.type == Event.EventType.WerewolfKill && e.getDayPart() == dayPartCounter - 1 && e.affectedPlayers.size() > 0){
                    victim = e.affectedPlayers.get(0);
                }
            }
        }

        return victim;

    }

    Player GetMayor(){
        for(Player p : GetAlivePlayers()) {
            if (p.isMayor) {
                return p;
            }
        }
        return null;
    }

    Player GetOtherLover(Player other){

        for(Player p : players){
            if(p != other && p.faction == Player.Faction.Lovers){
                return p;
            }
        }

        return null;
    }

    Player VillageIdiotRevealed(){

        if(pastEvents == null || pastEvents.size() == 0) return null;

        // Go through all events...
        for(Event e : pastEvents){
            // If we found a lynching and there are affected players...
            if(e != null && e.type == Event.EventType.Lynch && e.affectedPlayers != null && e.affectedPlayers.size() > 0){
                // Go through all the affected players
                for(Player p : e.affectedPlayers){
                    if(p.getRole() == Card.Role.VillageIdiot) return p;
                }
            }
        }

        return null;
    }

    // --------- Killing players and ending the game --------- //

    void AnnounceDead(){

        // Get all players killed the night before
        ArrayList<Player> victims = GetPlayersKilledInNight(dayPartCounter - 1);

        if(victims != null && victims.size() > 1){
            for(Player p : victims) WriteDeath(p);
        }

        EndGameIfPossible();
    }

    void KillPlayer(Player p, Player.DeathCause cause){

        // If the player is not alive, we are literally pulling a dead horse here
        if(p.IsDead()) return;

        // Try to kill the player. If we succeed, continue. Else, do nothing
        if(!p.Kill(cause)) return;

        // We killed a player succesfully!
        // If it is day, we need to write the death immediately
        if(isDay) WriteDeath(p);

        if(EndGameIfPossible()) return;

        // If we are one of the lovers, kill the other lover as well
        if(p.faction == Player.Faction.Lovers){
            Player otherLover = GetOtherLover(p);
            KillPlayer(otherLover, Player.DeathCause.LoversSorrow);
        }

        // If we are killed and are Hunter, we may kill someone if we are not killed by Lovers Sorrow
        if(p.getRole() == Card.Role.Hunter && cause != Player.DeathCause.LoversSorrow) {
            // If it's day, the hunter can kill someone immediately
            if (isDay) events.add(0, new Event(Event.EventType.HunterKill, dayPartCounter));
            else events.add(new Event(Event.EventType.HunterKill, dayPartCounter));
        }
    }

    boolean EndGameIfPossible(){

        ArrayList<Player> werewolves = GetPlayersByRole(Card.Role.Werewolf, true);
        ArrayList<Player> alivePlayers = GetAlivePlayers();
        boolean done = false;

        // If there are no more werewolves, we are done.
        if(werewolves == null || werewolves.isEmpty()){
            WriteEvent(new Event(Event.EventType.EndGame_CitizenWin, dayPartCounter));
            done = true;
        }
        // If there are only werewolves, we are done.
        else if(alivePlayers.size() == werewolves.size()){
            WriteEvent(new Event(Event.EventType.EndGame_WerewolfWin, dayPartCounter));
            done = true;
        }
        // If there are only 2 players left and they are the lovers, they win!
        else if(alivePlayers.size() == 2 && alivePlayers.get(0).faction == Player.Faction.Lovers && alivePlayers.get(1).faction == Player.Faction.Lovers){
            WriteEvent(new Event(Event.EventType.EndGame_LoversWin, dayPartCounter));
            done = true;
        }
        // If everyone except the fluteplayer is bewitched and the fluteplayer is alive, he wins!
        else if(GetPlayersByRole(Card.Role.FlutePlayer, true) != null){
            boolean all = true;
            for(Player p : alivePlayers){
                if(!p.bewitched){
                    all = false;
                }
            }
            if(all){
                WriteEvent(new Event(Event.EventType.EndGame_FlutePlayerWin, dayPartCounter));
                done = true;
            }
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
