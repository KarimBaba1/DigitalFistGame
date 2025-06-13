package com.example.digitalfistgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Opponent’s guessing round:
 * • Player chooses ONLY their two hands (0 or 5 each).
 * • App fetches opponent’s two hands + opponent’s guess from the server.
 * • If opponent’s guess == combined total → game ends, otherwise next round.
 */
public class OpponentTurnActivity extends AppCompatActivity {

    // UI
    private ImageButton btnLeftFist, btnLeftOpen, btnRightFist, btnRightOpen;
    private Button      btnSubmitOpponent;
    private TextView    tvRound;

    // selection state (-1 = not chosen yet)
    private int leftHand  = -1;
    private int rightHand = -1;

    private static final String SERVER_URL =
            "https://assign-mobileasignment-ihudikcgpf.cn-hongkong.fcapp.run/";  // keep the URL that works for you

    private GameState state;    // travels through the whole game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opponent_turn);

        // ── restore / receive GameState ───────────────────────────
        if (savedInstanceState != null) {
            state = (GameState) savedInstanceState.getSerializable("state");
        }
        if (state == null) {
            state = (GameState) getIntent().getSerializableExtra("state");
        }
        if (state == null) state = new GameState();   // extreme fallback

        // ── view refs ─────────────────────────────────────────────
        btnLeftFist   = findViewById(R.id.btnLeftFist);
        btnLeftOpen   = findViewById(R.id.btnLeftOpen);
        btnRightFist  = findViewById(R.id.btnRightFist);
        btnRightOpen  = findViewById(R.id.btnRightOpen);
        btnSubmitOpponent = findViewById(R.id.btnSubmitOpponent);
        tvRound       = findViewById(R.id.tvRound);

        tvRound.setText("Round " + state.round);

        // start with Submit disabled / dimmed
        btnSubmitOpponent.setEnabled(false);
        btnSubmitOpponent.setAlpha(0.4f);

        // ── hand-selection listeners (fade + zoom) ───────────────
        btnLeftFist.setOnClickListener(v -> {
            leftHand = 0;
            markSelected(btnLeftFist, btnLeftOpen);
            updateSubmitEnabled();
            Toast.makeText(this, "Left: 0 fingers", Toast.LENGTH_SHORT).show();
        });

        btnLeftOpen.setOnClickListener(v -> {
            leftHand = 5;
            markSelected(btnLeftOpen, btnLeftFist);
            updateSubmitEnabled();
            Toast.makeText(this, "Left: 5 fingers", Toast.LENGTH_SHORT).show();
        });

        btnRightFist.setOnClickListener(v -> {
            rightHand = 0;
            markSelected(btnRightFist, btnRightOpen);
            updateSubmitEnabled();
            Toast.makeText(this, "Right: 0 fingers", Toast.LENGTH_SHORT).show();
        });

        btnRightOpen.setOnClickListener(v -> {
            rightHand = 5;
            markSelected(btnRightOpen, btnRightFist);
            updateSubmitEnabled();
            Toast.makeText(this, "Right: 5 fingers", Toast.LENGTH_SHORT).show();
        });

        btnSubmitOpponent.setOnClickListener(v -> playOpponentTurn());
    }

    // ── visual helper -------------------------------------------------
    private void markSelected(ImageButton picked, ImageButton other) {
        picked.setAlpha(1f);
        picked.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100);
        other.setAlpha(0.5f);
        other.animate().scaleX(1f).scaleY(1f).setDuration(100);
    }

    // ── enable Submit only when both hands selected ------------------
    private void updateSubmitEnabled() {
        boolean ready = leftHand >= 0 && rightHand >= 0;
        btnSubmitOpponent.setEnabled(ready);
        btnSubmitOpponent.animate()
                .alpha(ready ? 1f : 0.4f)
                .setDuration(150);
    }

    // ── main logic ----------------------------------------------------
    private void playOpponentTurn() {
        int playerTotal = leftHand + rightHand;

        MyThread fetcher = new MyThread(SERVER_URL);
        fetcher.fetchJSON();

        new Thread(() -> {
            while (!fetcher.done) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }

            int oppLeft   = fetcher.getLeft();
            int oppRight  = fetcher.getRight();
            int oppGuess  = fetcher.getGuess();
            int combined  = playerTotal + oppLeft + oppRight;   // true total

            boolean opponentWon = (oppGuess == combined);
            state.totalRoundsPlayed++;

            runOnUiThread(() -> {
                if (opponentWon) {
                    state.gameEnded = true;
                    showFinishDialog(
                            "😬 Opponent guessed correctly!\nGuess: "
                                    + oppGuess + "\nTotal: " + combined,
                            /*playerWon=*/false
                    );
                } else {
                    state.round++;
                    Intent i = new Intent(this, YourTurnActivity.class);
                    i.putExtra("state", state);
                    startActivity(i);
                    finish();
                }
            });
        }).start();
    }

    // ── finish dialog -------------------------------------------------
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

    // ── state persistence --------------------------------------------
    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putSerializable("state", state);
    }
}
