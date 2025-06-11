package com.example.digitalfistgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class YourTurnActivity extends AppCompatActivity {

    ImageButton btnLeftFist, btnLeftOpen, btnRightFist, btnRightOpen;
    Spinner spinnerGuess;
    Button btnSubmitTurn;

    int leftHand = 0;
    int rightHand = 0;
    int userGuess = 0;

    final String serverUrl = "https://assign-mobileasignment-ihudikcgpf.cn-hongkong.fcapp.run";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_turn);

        // Setup image buttons
        btnLeftFist = findViewById(R.id.btnLeftFist);
        btnLeftOpen = findViewById(R.id.btnLeftOpen);
        btnRightFist = findViewById(R.id.btnRightFist);
        btnRightOpen = findViewById(R.id.btnRightOpen);
        spinnerGuess = findViewById(R.id.spinnerGuess);
        btnSubmitTurn = findViewById(R.id.btnSubmitTurn);

        // Populate guess spinner
        Integer[] guessOptions = {0, 5, 10, 15, 20};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, guessOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGuess.setAdapter(adapter);

        // Listeners for hand selection
        btnLeftFist.setOnClickListener(v -> {
            leftHand = 0;
            Toast.makeText(this, "Left: 0 fingers", Toast.LENGTH_SHORT).show();
        });

        btnLeftOpen.setOnClickListener(v -> {
            leftHand = 5;
            Toast.makeText(this, "Left: 5 fingers", Toast.LENGTH_SHORT).show();
        });

        btnRightFist.setOnClickListener(v -> {
            rightHand = 0;
            Toast.makeText(this, "Right: 0 fingers", Toast.LENGTH_SHORT).show();
        });

        btnRightOpen.setOnClickListener(v -> {
            rightHand = 5;
            Toast.makeText(this, "Right: 5 fingers", Toast.LENGTH_SHORT).show();
        });

        btnSubmitTurn.setOnClickListener(v -> {
            userGuess = Integer.parseInt(spinnerGuess.getSelectedItem().toString());

            int playerTotal = leftHand + rightHand;

            // Fetch opponent move from server
            MyThread fetcher = new MyThread(serverUrl);
            fetcher.fetchJSON();

            // Wait until server response is ready (this is a simplified busy-wait — we can improve later)
            new Thread(() -> {
                while (fetcher.parsingComplete) {
                    try {
                        Thread.sleep(100); // Wait for server response
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int opponentLeft = fetcher.getLeft();
                int opponentRight = fetcher.getRight();
                // IGNORE opponent's guess during player's turn

                int total = playerTotal + opponentLeft + opponentRight;

                // Show result on UI thread
                runOnUiThread(() -> {
                    String message;
                    if (userGuess == total) {
                        message = "🎉 You guessed right!\nOpponent showed: " + (opponentLeft + opponentRight) + "\nTotal: " + total;
                    } else {
                        message = "❌ You guessed wrong.\nOpponent showed: " + (opponentLeft + opponentRight) + "\nTotal: " + total;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Round Result")
                            .setMessage(message)
                            .setPositiveButton("Continue", (dialog, which) -> {
                                // TODO: Go to opponent turn screen
                                finish(); // for now, just close
                            })
                            .show();
                });
            }).start();
        });
    }
}
