package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayerSetup extends AppCompatActivity {

    int playerCount = 0;
    ArrayList<String> playerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setup);

        TextView pnc = findViewById(R.id.player_names_counter);
        pnc.setText(getResources().getString(R.string.player_setup_counter, 0));

        EditText nameInput = findViewById(R.id.player_name_input);
        nameInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                AddPlayer();
                return true;
            }
        });
    }

    void AddPlayer(){

        EditText nameInput = findViewById(R.id.player_name_input);
        String name = nameInput.getText().toString();
        nameInput.setText("");

        if(name.equals("") || name.length() == 0 || playerNames.contains(name.trim())) return;

        playerNames.add(name.trim());
        playerCount++;
        UpdateCounter();

        // Create a new Textview
        TextView t = new TextView(this);
        t.setTextColor(0xD0FFFFFF);
        t.setGravity(Gravity.CENTER);
        String text = playerCount + ". " + name;
        t.setText(text);
        t.setTextSize(16);

        LinearLayout nameContainer = findViewById(R.id.player_names_container);
        nameContainer.addView(t);

        if(playerCount >= 4){
            Button b = findViewById(R.id.player_setup_done_button);
            b.setEnabled(true);
        }

    }

    public void TransitionToCardSetup(View v){
        Intent intent = new Intent(this, CardSetup.class);
        intent.putExtra("playerCount",playerCount);

        String[] names = new String[playerNames.size()];
        names = playerNames.toArray(names);
        intent.putExtra("playerNames", names);

        startActivity(intent);
    }

    void UpdateCounter(){
        TextView t = findViewById(R.id.player_names_counter);
        t.setText(getResources().getString(R.string.player_setup_counter, playerCount));
    }
}
