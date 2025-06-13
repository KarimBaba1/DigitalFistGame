package com.example.digitalfistgame;

import android.util.Log;
import com.example.digitalfistgame.GameState;   // <-- put this under the other imports
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Simple helper that fetches one JSON object of the form
 * {"left":5,"right":5,"guess":15} from the server.
 *
 * Usage:
 *     MyThread t = new MyThread(URL);
 *     t.fetchJSON();
 *     while (!t.done) { Thread.sleep(100); }
 *     int l = t.getLeft(); ...
 */
public class MyThread {

    /** Set to <code>true</code> the moment the network call finishes
     *  (whether it succeeded or errored).
     *  Activities poll on this flag. */
    public volatile boolean done = false;

    private int left, right, guess;
    private final String urlString;

    public MyThread(String urlString) {
        this.urlString = urlString;
    }

    public int getLeft()  { return left; }
    public int getRight() { return right; }
    public int getGuess() { return guess; }

    /** Fire-and-forget background download. */
    public void fetchJSON() {
        done = false;                                  // reset in case of reuse

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(urlString).openConnection();
                conn.setReadTimeout(10_000);
                conn.setConnectTimeout(15_000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();

                StringBuilder sb = new StringBuilder();
                try (InputStream in = conn.getInputStream();
                     BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }

                String json = sb.toString();
                Log.d("FetchJSON", json);

                JSONObject obj = new JSONObject(json);
                left  = obj.getInt("left");
                right = obj.getInt("right");
                guess = obj.getInt("guess");

            } catch (Exception e) {
                Log.e("FetchJSON", "Error fetching / parsing", e);
            } finally {
                if (conn != null) conn.disconnect();
                done = true;                            // signal completion
            }
        }).start();
    }
}