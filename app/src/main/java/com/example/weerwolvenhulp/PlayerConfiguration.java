package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class PlayerConfiguration extends AppCompatActivity {

    int playerCount = 0;
    String[] playerNames;
    ArrayList<Card.Role> roles = new ArrayList<>();
    ArrayList<Player> players = new ArrayList<>();
    boolean containsThief = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_configuration);

        Intent intent = getIntent();

        // Set variables
        playerCount = intent.getIntExtra("playerCount", 4);
        roles = (ArrayList<Card.Role>) intent.getSerializableExtra("roles");
        playerNames = intent.getStringArrayExtra("playerNames");

        if(roles.contains(Card.Role.Thief)){
            roles.add(Card.Role.Citizen);
            roles.add(Card.Role.Citizen);
            containsThief = true;
        }

        NextPlayer(null);

    }

    void UpdateUI(Card.Role role){
        // Set the counter
        TextView counterText = findViewById(R.id.player_config_counter_text);
        String s = getResources().getString(R.string.player_config_counter, players.size() + 1, playerCount);

        if(containsThief) s += " (" + getResources().getString(R.string.two_extra_from_thief) +  ")";

        counterText.setText(s);

        // Set the next name
        String name = playerNames[players.size()];
        TextView text = findViewById(R.id.player_config_name);
        text.setText(name);

        // Sets the right image
        ImageView i = findViewById(R.id.player_config_role_image);
        switch (role){

            case Werewolf:
                i.setImageDrawable(getResources().getDrawable(R.drawable.werewolfcard200));
                break;
            case Citizen:
                i.setImageDrawable(getResources().getDrawable(R.drawable.citizencard200));
                break;
            case FlutePlayer:
                i.setImageDrawable(getResources().getDrawable(R.drawable.fluteplayercard200));
                break;
            case Hunter:
                i.setImageDrawable(getResources().getDrawable(R.drawable.huntercard200));
                break;
            case Witch:
                i.setImageDrawable(getResources().getDrawable(R.drawable.witchcard200));
                break;
            case Cupid:
                i.setImageDrawable(getResources().getDrawable(R.drawable.cupidcard200));
                break;
            case Thief:
                i.setImageDrawable(getResources().getDrawable(R.drawable.thiefcard200));
                break;
            case Savior:
                i.setImageDrawable(getResources().getDrawable(R.drawable.saviorcard200));
                break;
            case Scapegoat:
                i.setImageDrawable(getResources().getDrawable(R.drawable.scapegoatcard200));
                break;
            case Seer:
                i.setImageDrawable(getResources().getDrawable(R.drawable.seercard200));
                break;
            case CheatingGirl:
                i.setImageDrawable(getResources().getDrawable(R.drawable.cheatinggirlcard200));
                break;
            case VillageElder:
                i.setImageDrawable(getResources().getDrawable(R.drawable.villageeldercard200));
                break;
            case VillageIdiot:
                i.setImageDrawable(getResources().getDrawable(R.drawable.villageidiotcard200));
                break;
        }

    }

    public void NextPlayer(View view){

        if(players.size() == playerCount){
            TransitionToInGame();
            return;
        }

        Card.Role role = GetRandomRole(); // Choose a random role
        String name = playerNames[players.size()]; // Get the next name

        UpdateUI(role); // Update the UI to reflect our choices

        Player p = new Player(role, name); // Create a new player
        players.add(p); // Add the new player to the list

        if(players.size() == playerCount){
            Button b = (Button)view;
            b.setText(R.string.start_text);
        }
    }

    Card.Role GetRandomRole(){
        Random r = new Random();
        int i = r.nextInt(roles.size());
        Card.Role role = roles.get(i);
        roles.remove(i);
        return role;
    }

    void Debug(String text){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    void TransitionToInGame(){
        Intent intent = new Intent(this, InGame.class);
        intent.putExtras(getIntent());
        intent.putExtra("players", players);
        if(roles.size() == 2) intent.putExtra("extraRoles", roles);
        startActivity(intent);
    }

}
