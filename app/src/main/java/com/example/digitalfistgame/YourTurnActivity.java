package com.example.digitalfistgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Player’s guessing round:
 *  • pick two hands (0 / 5 fingers each) 
 *  • choose a guess (0-20) 
 *  • ignores opponent’s guess; compares your guess to total 
 *  • on miss → OpponentTurnActivity, on hit → finish dialog
 */
public class YourTurnActivity extends AppCompatActivity {

    // UI
    private ImageButton btnLeftFist, btnLeftOpen, btnRightFist, btnRightOpen;
    private Spinner     spinnerGuess;
    private TextView    tvRound;

    // state
    private int leftHand  = -1;   // -1 means “not chosen yet”
    private int rightHand = -1;
    private GameState state;

    private static final String SERVER_URL =
            "https://assign-mobileasignment-ihudikcgpf.cn-hongkong.fcapp.run/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_turn);

        // ── restore or obtain GameState ─────────────────────────────
        if (savedInstanceState != null) {
            state = (GameState) savedInstanceState.getSerializable("state");
        }
        if (state == null) {
            state = (GameState) getIntent().getSerializableExtra("state");
        }
        if (state == null) state = new GameState();     // fallback

        // ── view refs ──────────────────────────────────────────────
        btnLeftFist   = findViewById(R.id.btnLeftFist);
        btnLeftOpen   = findViewById(R.id.btnLeftOpen);
        btnRightFist  = findViewById(R.id.btnRightFist);
        btnRightOpen  = findViewById(R.id.btnRightOpen);
        spinnerGuess  = findViewById(R.id.spinnerGuess);
        tvRound       = findViewById(R.id.tvRound);

        tvRound.setText("Round " + state.round);

        // spinner values 0-20
        Integer[] guesses = {0, 5, 10, 15, 20};
        ArrayAdapter<Integer> spinAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, guesses);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuess.setAdapter(spinAdapter);

        // ── hand selection listeners with visual feedback ──────────
        btnLeftFist.setOnClickListener(v -> {
            leftHand = 0;
            markSelected(btnLeftFist, btnLeftOpen);
            updateSubmitEnabled();
        });
        btnLeftOpen.setOnClickListener(v -> {
            leftHand = 5;
            markSelected(btnLeftOpen, btnLeftFist);
            updateSubmitEnabled();
        });
        btnRightFist.setOnClickListener(v -> {
            rightHand = 0;
            markSelected(btnRightFist, btnRightOpen);
            updateSubmitEnabled();
        });
        btnRightOpen.setOnClickListener(v -> {
            rightHand = 5;
            markSelected(btnRightOpen, btnRightFist);
            updateSubmitEnabled();
        });

        findViewById(R.id.btnSubmitTurn).setOnClickListener(v -> playPlayerTurn());
        updateSubmitEnabled();    // start disabled until both hands chosen
    }

    // ── helper: visual toggle + simple animation -------------------
    private void markSelected(ImageButton picked, ImageButton other) {
        picked.setAlpha(1f);
        picked.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start();

        other.setAlpha(0.5f);
        other.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
    }

    // ── helper: enable Submit only when ready ----------------------
    private void updateSubmitEnabled() {
        boolean ready = leftHand >= 0 && rightHand >= 0;
        findViewById(R.id.btnSubmitTurn).setEnabled(ready);
    }

    // ── main round logic -------------------------------------------
    private void playPlayerTurn() {
        int userGuess   = (Integer) spinnerGuess.getSelectedItem();
        int playerTotal = leftHand + rightHand;

        MyThread fetcher = new MyThread(SERVER_URL);
        fetcher.fetchJSON();

        new Thread(() -> {
            while (!fetcher.done) { try { Thread.sleep(100); } catch (InterruptedException ignored) {} }

            int oppLeft  = fetcher.getLeft();
            int oppRight = fetcher.getRight();
            int trueTotal = playerTotal + oppLeft + oppRight;

            boolean playerWon = (userGuess == trueTotal);
            state.totalRoundsPlayed++;

            runOnUiThread(() -> {
                String body = (playerWon
                        ? "🎉 You guessed right!"
                        : "❌ You guessed wrong.") +
                        "\nOpponent showed: " + (oppLeft + oppRight) +
                        "\nTotal: " + trueTotal;

                if (playerWon) {
                    state.gameEnded = true;
                    showFinishDialog(body, true);
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Round Result")
                            .setMessage(body)
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

    // ── finish dialog + DB insert ----------------------------------
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
                            playerWon ? "Win" : "Lost", state.totalRoundsPlayed);
                    finishAffinity();
                })
                .setCancelable(false)
                .show();
    }

    // ── state persistence ------------------------------------------
    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putSerializable("state", state);
    }
}
