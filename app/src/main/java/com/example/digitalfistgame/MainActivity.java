package com.example.digitalfistgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btnPlay, btnRecords, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnPlay = findViewById(R.id.btnPlay);
        btnRecords = findViewById(R.id.btnRecords);
        btnClose = findViewById(R.id.btnClose);

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OpponentSelectActivity.class);
            startActivity(intent);
        });

        btnRecords.setOnClickListener( v ->{
            //TODO: Replace RecordsActivity later on
            Intent intent = new Intent(MainActivity.this, RecordsActivity.class)
        });
        btnClose.setOnClickListener( v ->{
            //TODO: Replace CloseActivity later on
            finishAffinity(); //close app
        });
    }
}