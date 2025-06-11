package com.example.digitalfistgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class OpponentSelectActivity extends AppCompatActivity {

    ListView lvOpponents;
    String[] opponents = {"Alex", "Jamie", "Jordan", "Taylor", "Casey"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponent_select);

        lvOpponents = findViewById(R.id.lvOpponents);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, opponents);
        lvOpponents.setAdapter(adapter);

        lvOpponents.setOnItemClickListener((parent, view, position, id) -> {
            String selectedOpponent = opponents[position];

            // Go to next screen (YourTurnActivity)
            Intent intent = new Intent(OpponentSelectActivity.this, YourTurnActivity.class);
            intent.putExtra("opponentName", selectedOpponent);
            startActivity(intent);
        });
    }
}
