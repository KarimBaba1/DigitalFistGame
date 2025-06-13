package com.example.digitalfistgame;

import android.app.GameState;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Player’s guessing round.
 * • Player sets both hands (0 / 5 each) **and** selects a guess (0 – 20).
 * • App fetches opponent’s two hands *but ignores opponent’s guess*.
 * • If the player’s guess equals the combined total, the game ends.
 *   Otherwise control jumps to OpponentTurnActivity.
 */
public class YourTurnActivity extends AppCompatActivity {

    // --- UI ---
    private ImageButton btnLeftFist, btnLeftOpen, btnRightFist, btnRightOpen;
    private Spinner     spinnerGuess;
    private TextView    tvRound;
    private int leftHand = 0, rightHand = 0;

    private static final String SERVER_URL =
            "https://assign-mobileasignment-ihudikcgpf.cn-hongkong.fcapp.run";

    private GameState state;                // travels through the whole game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_turn);

        // ----- restore / get state -----
        if (savedInstanceState != null) {
            state = (GameState) savedInstanceState.getSerializable("state");
        }
        if (state == null) {
            state = (GameState) getIntent().getSerializableExtra("state");
        }
        if (state == null) state = new GameState();     // extreme fallback

        // ----- find views -----
        btnLeftFist  = findViewById(R.id.btnLeftFist);
        btnLeftOpen  = findViewById(R.id.btnLeftOpen);
        btnRightFist = findViewById(R.id.btnRightFist);
        btnRightOpen = findViewById(R.id.btnRightOpen);
        spinnerGuess = findViewById(R.id.spinnerGuess);
        tvRound      = findViewById(R.id.tvRound);

        // header
        if (tvRound != null) tvRound.setText("Round " + state.round);

        // ----- spinner -----
        Integer[] guesses = {0, 5, 10, 15, 20};
        ArrayAdapter<Integer> spinAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, guesses);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuess.setAdapter(spinAdapter);

        // ----- hand buttons -----
        btnLeftFist .setOnClickListener(v -> { leftHand = 0; Toast.makeText(this,"Left: 0",Toast.LENGTH_SHORT).show(); });
        btnLeftOpen .setOnClickListener(v -> { leftHand = 5; Toast.makeText(this,"Left: 5",Toast.LENGTH_SHORT).show(); });
        btnRightFist.setOnClickListener(v -> { rightHand = 0; Toast.makeText(this,"Right: 0",Toast.LENGTH_SHORT).show(); });
        btnRightOpen.setOnClickListener(v -> { rightHand = 5; Toast.makeText(this,"Right: 5",Toast.LENGTH_SHORT).show(); });

        findViewById(R.id.btnSubmitTurn).setOnClickListener(v -> playPlayerTurn());
    }

    private void playPlayerTurn() {
        int userGuess = (Integer) spinnerGuess.getSelectedItem();
        int playerTotal = leftHand + rightHand;

        MyThread fetcher = new MyThread(SERVER_URL);
        fetcher.fetchJSON();

        new Thread(() -> {
            while (!fetcher.done) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }

            int oppLeft  = fetcher.getLeft();
            int oppRight = fetcher.getRight();
            int trueTotal = playerTotal + oppLeft + oppRight;

            boolean playerWon = (userGuess == trueTotal);
            state.totalRoundsPlayed++;

            runOnUiThread(() -> {
                String msg;
                if (playerWon) {
                    msg = "🎉 You guessed right!\n"
                            + "Opponent showed: " + (oppLeft + oppRight)
                            + "\nTotal: " + trueTotal;
                    state.gameEnded = true;
                    showFinishDialog(msg, /*playerWon=*/true);
                } else {
                    msg = "❌ You guessed wrong.\n"
                            + "Opponent showed: " + (oppLeft + oppRight)
                            + "\nTotal: " + trueTotal;
                    new AlertDialog.Builder(this)
                            .setTitle("Round Result")
                            .setMessage(msg)
                            .setPositiveButton("Continue", (d, w) -> {
                                state.round++;
                                Intent i = new Intent(this, OpponentTurnActivity.class);
                                i.putExtra("state", state);
                                startActivity(i);
                                finish();
                            })
                            .setCancelable(false)
                            .show();
                }
            });
        }).start();
    }

    // ------------------------------------------------------------
    // common end-of-game dialog + DB insert
    private void showFinishDialog(String msg, boolean playerWon) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(msg + "\nRounds: " + state.totalRoundsPlayed)
                .setPositiveButton("Finish", (d, w) -> {
                    GameDatabaseHelper db = new GameDatabaseHelper(this);
                    String date = new java.text.SimpleDateFormat(
                            "yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                    String time = new java.text.SimpleDateFormat(
                            "HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
                    db.insertGame(date, time, state.opponentName,
                            playerWon ? "Win" : "Lost",
                            state.totalRoundsPlayed);
                    finishAffinity();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putSerializable("state", state);
    }
}