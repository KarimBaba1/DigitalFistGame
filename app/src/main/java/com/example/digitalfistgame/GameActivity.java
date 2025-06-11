package com.example.digitalfistgame;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    int playerFingers = 0; // Keep this accessible inside the class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);

        // Apply edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Now your actual game logic setup
        Button btnZero = findViewById(R.id.btnZero);
        Button btnFive = findViewById(R.id.btnFive);
        Button btnTen = findViewById(R.id.btnTen);
        Button btnSubmit = findViewById(R.id.btnSubmit);
        EditText etGuess = findViewById(R.id.etGuess);
        TextView tvResult = findViewById(R.id.tvResult);

        btnZero.setOnClickListener(v -> playerFingers = 0);
        btnFive.setOnClickListener(v -> playerFingers = 5);
        btnTen.setOnClickListener(v -> playerFingers = 10);

        btnSubmit.setOnClickListener(v -> {
            String guessStr = etGuess.getText().toString().trim();
            if (guessStr.isEmpty()) {
                tvResult.setText("Please enter your guess.");
                return;
            }

            int guess = Integer.parseInt(guessStr);
            int cpuFingers = new int[]{0, 5, 10}[new Random().nextInt(3)];
            int total = playerFingers + cpuFingers;

            if (guess == total) {
                tvResult.setText("🎉 You guessed it! Total = " + total);
            } else {
                tvResult.setText("❌ Wrong! CPU showed " + cpuFingers + ", Total = " + total);
            }
        });
    }
}
