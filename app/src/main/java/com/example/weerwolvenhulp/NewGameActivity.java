package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class NewGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        Setup();

    }

    int werewolves = 0;
    int citizens = 0;

    // 0 = witch
    // 1 = hunter
    // 3 = cheating girl
    // 4 = seer
    // 5 = thief
    // 6 = cupid
    boolean[] singleCards = new boolean[6];

    public void Setup() {
        UpdateSelectedCounter();
        Button wwb = this.findViewById(R.id.pick_werewolf_button);
        wwb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                werewolves = 0;
                ((Button)v).setText("");
                v.setAlpha(0.5f);
                UpdateSelectedCounter();
                return true;
            }
        });

        Button cb = this.findViewById(R.id.pick_citizen_button);
        cb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                citizens = 0;
                ((Button)v).setText("");
                v.setAlpha(0.5f);
                UpdateSelectedCounter();
                return true;
            }
        });
    }

    public void AddSingleCard(View v){
        if(v.getAlpha() == 0.5){
            v.setAlpha(1);
        }
        else{
            v.setAlpha(0.5f);
        }

        switch (v.getId()){
            case R.id.pick_witch_button:
                singleCards[0] = !singleCards[0];
                break;

            case R.id.pick_hunter_button:
                singleCards[1] = !singleCards[1];
                break;

            case R.id.pick_cheating_girl_button:
                singleCards[2] = !singleCards[2];
                break;

            case R.id.pick_seer_button:
                singleCards[3] = !singleCards[3];
                break;

            case R.id.pick_thief_button:
                singleCards[4] = !singleCards[4];
                break;

            case R.id.pick_cupid_button:
                singleCards[5] = !singleCards[5];
                break;
        }

        UpdateSelectedCounter();
    }

    public void AddWerewolf(View v){
        werewolves = MathUtils.clamp(werewolves+1, 1, 10);
        Button b = (Button)v;
        b.setText("X"+werewolves);
        v.setAlpha(1);
        UpdateSelectedCounter();
    }

    public void AddCitizen(View v){
        citizens = MathUtils.clamp(citizens+1, 1, 20);
        Button b = (Button)v;
        b.setText("X"+citizens);
        v.setAlpha(1);
        UpdateSelectedCounter();
    }

    public void UpdateSelectedCounter(){
        int counter = werewolves + citizens;

        int i;
        for(i = 0; i < singleCards.length; i++) if (singleCards[i]) counter += 1;

        // Get the counter TextView and format the string correctly
        TextView v = this.findViewById(R.id.selected_counter_text);
        v.setText(getResources().getString(R.string.selected_text, counter));
    }

    public void StartGame(View view){
        Intent intent = new Intent(this, PlayerConfiguration.class);
        intent.putExtra("werewolves", werewolves);
        intent.putExtra("citizens", citizens);
        intent.putExtra("singleCards", singleCards);
        startActivity(intent);
    }
}
