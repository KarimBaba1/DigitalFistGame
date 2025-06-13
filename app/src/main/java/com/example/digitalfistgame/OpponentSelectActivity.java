package com.example.digitalfistgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Lets the user choose an opponent name and kicks off the first turn.
 * Receives a fresh (or restored) GameState from MainActivity.
 */
public class OpponentSelectActivity extends AppCompatActivity {

    private ListView lvOpponents;
    private final String[] opponents = {"Alex", "Jamie", "Jordan", "Taylor", "Casey"};

    private GameState state;   // travels through the whole game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);                  // keep modern edge-to-edge look
        setContentView(R.layout.activity_opponent_select);

        // ---- restore or initialise state ----
        if (savedInstanceState != null) {
            state = (GameState) savedInstanceState.getSerializable("state");
        }
        if (state == null) {
            state = (GameState) getIntent().getSerializableExtra("state");
        }
        if (state == null) {
            state = new GameState();              // rare fallback
        }

        // ---- UI wiring ----
        lvOpponents = findViewById(R.id.lvOpponents);

        lvOpponents.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, opponents)
        );

        lvOpponents.setOnItemClickListener((p, v, pos, id) -> {
            state.opponentName = opponents[pos];
            state.round = 1;
            state.totalRoundsPlayed = 0;
            state.gameEnded = false;

            Intent i = new Intent(this, YourTurnActivity.class);
            i.putExtra("state", state);
            startActivity(i);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putSerializable("state", state);
    }
}