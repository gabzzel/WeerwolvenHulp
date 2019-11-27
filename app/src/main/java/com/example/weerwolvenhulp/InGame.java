package com.example.weerwolvenhulp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InGame extends AppCompatActivity {

    LinearLayout container = null;
    int dayCounter = 0;
    public boolean isDay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        container = (LinearLayout)this.findViewById(R.id.day_night_container);
        StartNewNight();
    }

    public void StartNewDay(){
        dayCounter++;
        isDay = true;
        TextView text = new TextView(this);
        text.setText("Dag " + dayCounter + " is begonnen!");
        container.addView(text);
    }

    public void StartNewNight(){
        isDay = false;
        TextView text = new TextView(this);
        text.setText("Nacht " + dayCounter + " is begonnen!");
        container.addView(text);
    }

    public void HandleDayNightSwitch(View v){
        if(isDay){
            StartNewNight();
        }
        else{
            StartNewDay();
        }
    }
}
