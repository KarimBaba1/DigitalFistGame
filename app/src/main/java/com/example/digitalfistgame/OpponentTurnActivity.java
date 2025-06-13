package com.example.digitalfistgame;

import android.app.GameState;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Opponent’s guessing round.
 * • Player chooses ONLY their two hands (0 or 5 each).
 * • App fetches opponent’s two hands + opponent’s guess from the server.
 * • If opponent’s guess equals the combined total, the game ends.
 *   Otherwise control returns to YourTurnActivity for the next round.
 */
public class OpponentTurnActivity extends AppCompatActivity {

    private ImageButton btnLeftFist, btnLeftOpen, btnRightFist, btnRightOpen;
    private Button      btnSubmitOpponent;
    private TextView    tvRound;            // (optional - add to XML)

    private int leftHand  = 0;
    private int rightHand = 0;

    private static final String SERVER_URL =
            "https://assign-mobileasignment-ihudikcgpf.cn-hongkong.fcapp.run";

    private GameState state;                // travels through the whole game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opponent_turn);

        // ---------- restore / receive GameState ----------
        if (savedInstanceState != null) {
            state = (GameState) savedInstanceState.getSerializable("state");
        }
        if (state == null) {
            state = (GameState) getIntent().getSerializableExtra("state");
        }
        if (state == null) {
            state = new GameState();                    // extreme fallback
        }

        // ---------- UI references ----------
        btnLeftFist      = findViewById(R.id.btnLeftFist);
        btnLeftOpen      = findViewById(R.id.btnLeftOpen);
        btnRightFist     = findViewById(R.id.btnRightFist);
        btnRightOpen     = findViewById(R.id.btnRightOpen);
        btnSubmitOpponent = findViewById(R.id.btnSubmitOpponent);
        tvRound          = findViewById(R.id.tvRound);  // add to XML if you want

        if (tvRound != null) tvRound.setText("Round " + state.round);

        // ---------- hand-selection listeners ----------
        btnLeftFist .setOnClickListener(v -> {
            leftHand = 0;  Toast.makeText(this, "Left: 0 fingers", Toast.LENGTH_SHORT).show();
        });
        btnLeftOpen .setOnClickListener(v -> {
            leftHand = 5;  Toast.makeText(this, "Left: 5 fingers", Toast.LENGTH_SHORT).show();
        });
        btnRightFist.setOnClickListener(v -> {
            rightHand = 0; Toast.makeText(this, "Right: 0 fingers", Toast.LENGTH_SHORT).show();
        });
        btnRightOpen.setOnClickListener(v -> {
            rightHand = 5; Toast.makeText(this, "Right: 5 fingers", Toast.LENGTH_SHORT).show();
        });

        // ---------- submit ----------
        btnSubmitOpponent.setOnClickListener(v -> playOpponentTurn());
    }

    private void playOpponentTurn() {
        int playerTotal = leftHand + rightHand;

        MyThread fetcher = new MyThread(SERVER_URL);
        fetcher.fetchJSON();

        // simple busy-wait (ok for this coursework scale)
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
                    // Continue to next round (back to player's turn)
                    state.round++;
                    Intent i = new Intent(this, YourTurnActivity.class);
                    i.putExtra("state", state);
                    startActivity(i);
                    finish();   // prevent back-stack pile-up
                }
            });
        }).start();
    }

    // ---------- common end-of-game flow ----------
    private void showFinishDialog(String msg, boolean playerWon) {
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage(msg + "\nRounds: " + state.totalRoundsPlayed)
                .setPositiveButton("Finish", (d, w) -> {
                    // record in SQLite
                    GameDatabaseHelper db = new GameDatabaseHelper(this);
                    String date = new java.text.SimpleDateFormat(
                            "yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
                    String time = new java.text.SimpleDateFormat(
                            "HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
                    db.insertGame(date, time, state.opponentName,
                            playerWon ? "Win" : "Lost",
                            state.totalRoundsPlayed);
                    finishAffinity();            // back to main menu
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