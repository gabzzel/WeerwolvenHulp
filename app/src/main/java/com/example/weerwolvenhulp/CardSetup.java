package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class CardSetup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_setup);

        playerCount = getIntent().getIntExtra("playerCount", 8);
        Setup();

    }

    ArrayList<Card.Role> roles = new ArrayList<>();

    int playerCount = 0;

    public void Setup() {
        UpdateSelectedCounter();
        Button wwb = this.findViewById(R.id.pick_werewolf_button);
        wwb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                RemoveAllOfRole(Card.Role.Werewolf);
                ((Button)v).setText("");
                v.setAlpha(0.5f);
                return true;
            }
        });

        Button cb = this.findViewById(R.id.pick_citizen_button);
        cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                RemoveAllOfRole(Card.Role.Citizen);
                ((Button)v).setText("");
                v.setAlpha(0.5f);
                return true;
            }
        });

    }

    // Add a card that can only be added once
    public void AddSingleCard(View v){

        // If we cannot add a card and we want to add one, do nothing
        if(!CanAdd() && v.getAlpha() == 0.5) return;

        if(v.getAlpha() == 0.5){
            v.setAlpha(1);
        }
        else{
            v.setAlpha(0.5f);
        }

        switch (v.getId()){
            case R.id.pick_witch_button:
                ToggleSingleCard(Card.Role.Witch);
                break;

            case R.id.pick_hunter_button:
                ToggleSingleCard(Card.Role.Hunter);
                break;

            case R.id.pick_cheating_girl_button:
                ToggleSingleCard(Card.Role.CheatingGirl);
                break;

            case R.id.pick_seer_button:
                ToggleSingleCard(Card.Role.Seer);
                break;

            case R.id.pick_thief_button:
                ToggleSingleCard(Card.Role.Thief);
                break;

            case R.id.pick_cupid_button:
                ToggleSingleCard(Card.Role.Cupid);
                break;

            case R.id.pick_flute_player_button:
                ToggleSingleCard(Card.Role.FlutePlayer);
                break;

            case R.id.pick_savior_button:
                ToggleSingleCard(Card.Role.Savior);
                break;

            case R.id.pick_scapegoat_button:
                ToggleSingleCard(Card.Role.Scapegoat);
                break;

            case R.id.pick_village_elder_button:
                ToggleSingleCard(Card.Role.VillageElder);
                break;

            case R.id.pick_village_idiot_button:
                ToggleSingleCard(Card.Role.VillageIdiot);
                break;
        }

        UpdateSelectedCounter();
        UpdateStartButton();
    }

    // Adds or removes a role from the list, depending on if it already exists in the list
    public void ToggleSingleCard(Card.Role role){

        if(roles.contains(role)){
            roles.remove(role);
        }
        else{
            roles.add(role);
        }

    }

    // Gets the number of times a certain role exists in the roles list
    public int GetRoleCount(Card.Role role){

        if(!roles.contains(role)){
            return 0;
        }

        int x = 0;
        for(int i = 0; i < roles.size();i++){
            if(roles.get(i) == role){
                x++;
            }
        }

        return x;

    }

    // Removes all the roles of a certain type that exist in the roles list
    public void RemoveAllOfRole(Card.Role role){
        int count = GetRoleCount(role);
        for(int i = 0; i<count; i++){
            roles.remove(role);
        }
        UpdateSelectedCounter();
        UpdateStartButton();
    }

    public void AddWerewolf(View v){

        if(!CanAdd()) return; // If we have reached the max number of cards, do nothing.

        int werewolves = GetRoleCount(Card.Role.Werewolf); // Get werewolf count
        if(werewolves >= 10) return; // If it's already at a max, do nothing.

        roles.add(Card.Role.Werewolf);
        Button b = (Button)v;
        b.setText("X"+(werewolves + 1));
        v.setAlpha(1);
        UpdateSelectedCounter();
        UpdateStartButton();
    }

    public void AddCitizen(View v){

        if(!CanAdd()) return;

        int citizens = GetRoleCount(Card.Role.Citizen);
        if(citizens >= 20) return;

        roles.add(Card.Role.Citizen);
        Button b = (Button)v;
        b.setText("X"+(citizens + 1));
        v.setAlpha(1);
        UpdateSelectedCounter();
        UpdateStartButton();
    }

    // Return true if we can add another role, else return false
    public boolean CanAdd(){

        if(roles.size() < playerCount) return true;

        return false;
    }

    void UpdateStartButton(){

        boolean enable = true;

        if(!roles.contains(Card.Role.Werewolf)) enable = false;
        else if(GetRoleCount(Card.Role.Werewolf) == roles.size()) enable = false;
        else if(roles.size() != playerCount) enable = false;
        Button b = findViewById(R.id.start_game_button);
        b.setEnabled(enable);

    }

    public void UpdateSelectedCounter(){

        // Get the counter TextView and format the string correctly
        TextView v = this.findViewById(R.id.selected_counter_text);
        v.setText(getResources().getString(R.string.selected_text, roles.size(), playerCount));
    }

    public void StartGame(View view){
        Intent intent = new Intent(this, PlayerConfiguration.class);
        intent.putExtras(getIntent()); // Copy playerNames and playerCount
        intent.putExtra("roles", roles);
        startActivity(intent);
    }
}
