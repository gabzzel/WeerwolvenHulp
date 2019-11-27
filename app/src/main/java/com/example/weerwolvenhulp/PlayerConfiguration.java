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

import java.util.PriorityQueue;
import java.util.Queue;

public class PlayerConfiguration extends AppCompatActivity {

    Intent newGameIntent;
    int werewolves = 0;
    int citizens = 0;
    boolean[] singleCards;
    int playerCount = 0;
    int counter = 0;
    Queue<Card.Role> queue = new PriorityQueue<>();
    Card.Role current = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_configuration);
        Intent intent = getIntent();

        // Set variables
        werewolves = intent.getIntExtra("werewolves", 1);
        citizens = intent.getIntExtra("citizens", 1);
        singleCards = intent.getBooleanArrayExtra("singleCards");
        playerCount = werewolves + citizens;

        if(singleCards != null && singleCards.length > 0) {
            for (boolean card : singleCards) {
                if (card) playerCount++;
            }
        }

        UpdateCounter();
        FillQueue();

        // Create new Intent, so we can add players
        newGameIntent = new Intent(this, InGame.class);
        intent.putExtra("playerCount",playerCount);
        SetImage(queue.poll());

        EditText input = findViewById(R.id.player_name_input);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                NextPlayer(findViewById(R.id.next_player_button));
                return true;
            }
        });

    }

    void FillQueue(){
        for(int i = 0; i < werewolves; i++){
            queue.add(Card.Role.Werewolf);
        }
        for(int i = 0; i < citizens; i++){
            queue.add(Card.Role.Citizen);
        }
        for(int i = 0; i < singleCards.length; i++){
            if(singleCards[i]){
                switch (i){
                    case 0:
                        queue.add(Card.Role.Witch);
                        break;
                    case 1:
                        queue.add(Card.Role.Hunter);
                        break;
                    case 2:
                        queue.add(Card.Role.CheatingGirl);
                        break;
                    case 3:
                        queue.add(Card.Role.Seer);
                        break;
                    case 4:
                        queue.add(Card.Role.Thief);
                        break;
                    case 5:
                        queue.add(Card.Role.Cupid);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    void UpdateCounter(){
        counter++;

        TextView counterText = findViewById(R.id.player_config_counter_text);
        String s = getResources().getString(R.string.player_config_counter, counter, playerCount);
        counterText.setText(s);
    }

    void TransitionToInGame(){
        startActivity(newGameIntent);
    }

    public void NextPlayer(View view){

        if(counter >= playerCount){
            TransitionToInGame();
            return;
        }

        UpdateCounter();
        EditText nameInput = findViewById(R.id.player_name_input);
        String name = nameInput.getText().toString(); // Get the filled in name

        Player p = new Player(current, name); // Create a new player
        nameInput.setText(""); // Empty the edit box
        String s = Integer.toString(counter - 1);
        newGameIntent.putExtra(s, p); // Save the player in the intent

        if(counter < playerCount){
            SetImage(queue.poll());
        }
        else{
            ((Button)view).setText("Done. Start Game!");
        }
    }

    void Debug(String text){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    void SetImage(Card.Role role){
        current = role;
        ImageView image = findViewById(R.id.player_config_role_image);

        switch (role){

            case Werewolf:
                image.setImageDrawable(getResources().getDrawable(R.drawable.werewolfcard200));
                break;
            case Citizen:
                image.setImageDrawable(getResources().getDrawable(R.drawable.citizencard200));
                break;
            case Hunter:
                image.setImageDrawable(getResources().getDrawable(R.drawable.huntercard200));
                break;
            case Witch:
                image.setImageDrawable(getResources().getDrawable(R.drawable.witchcard200));
                break;
            case Cupid:
                image.setImageDrawable(getResources().getDrawable(R.drawable.cupidcard200));
                break;
            case Thief:
                image.setImageDrawable(getResources().getDrawable(R.drawable.thiefcard200));
                break;
            case Seer:
                image.setImageDrawable(getResources().getDrawable(R.drawable.seercard200));
                break;
            case CheatingGirl:
                image.setImageDrawable(getResources().getDrawable(R.drawable.cheatinggirlcard200));
                break;
        }
    }

}
