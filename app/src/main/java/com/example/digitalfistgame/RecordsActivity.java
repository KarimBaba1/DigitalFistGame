package com.example.digitalfistgame;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Shows finished-game records (most recent at the top).
 * Refreshes every time the screen is opened / resumed.
 */
public class RecordsActivity extends AppCompatActivity {

    private ListView lvRecords;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> records = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_records);

        lvRecords = findViewById(R.id.lvRecords);

        // Empty-view message if there are no records yet
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        lvRecords.setEmptyView(tvEmpty);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, records);
        lvRecords.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
    }

    /** Pull latest rows into the ArrayList and notify adapter. */
    private void loadRecords() {
        records.clear();

        GameDatabaseHelper dbHelper = new GameDatabaseHelper(this);
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor c = db.rawQuery(
                     "SELECT gameDate, gameTime, opponentName, winOrLost, rounds " +
                             "FROM " + GameDatabaseHelper.TABLE_NAME +
                             " ORDER BY id DESC",
                     null)) {

            while (c.moveToNext()) {
                String date   = c.getString(0);
                String time   = c.getString(1);
                String name   = c.getString(2);
                String result = c.getString(3);
                int rounds    = c.getInt(4);

                records.add(date + " " + time +
                        " | " + name +
                        " | " + result +
                        " | Rounds: " + rounds);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // (rotation not a big deal here; ListView already handles its state)
}