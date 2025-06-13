package com.example.digitalfistgame;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * App entry screen: Play, Records, Close.
 * Now initialises a fresh GameState and passes it into the game flow.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnPlay, btnRecords, btnClose;
    private GameState state;          // survives rotation via onSaveInstanceState

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Restore GameState if rotating back to this screen
        if (savedInstanceState != null) {
            state = (GameState) savedInstanceState.getSerializable("state");
        }
        if (state == null) {
            state = new GameState();      // brand-new session
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        btnPlay    = findViewById(R.id.btnPlay);
        btnRecords = findViewById(R.id.btnRecords);
        btnClose   = findViewById(R.id.btnClose);

        btnPlay.setOnClickListener(v -> {
            Intent i = new Intent(this, OpponentSelectActivity.class);
            i.putExtra("state", state);          // pass the fresh GameState
            startActivity(i);
        });

        btnRecords.setOnClickListener(v -> {
            startActivity(new Intent(this, RecordsActivity.class));
        });

        btnClose.setOnClickListener(v -> finishAffinity());   // exit app
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putSerializable("state", state);
    }
}