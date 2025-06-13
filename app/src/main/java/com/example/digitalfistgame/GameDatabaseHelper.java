package com.example.digitalfistgame;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "GamesLog.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "GamesLog";

    public GameDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "gameDate TEXT, " +
                        "gameTime TEXT, " +
                        "opponentName TEXT, " +
                        "winOrLost TEXT, " +
                        "rounds INTEGER)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void insertGame(String date, String time, String opponentName, String result, int rounds) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO " + TABLE_NAME +
                        " (gameDate, gameTime, opponentName, winOrLost, rounds) VALUES (?, ?, ?, ?, ?)",
                new Object[]{date, time, opponentName, result, rounds});
        db.close();
    }
}