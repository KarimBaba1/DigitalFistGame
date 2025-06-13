package com.example.digitalfistgame;

import java.io.Serializable;

/**
 * Lightweight container that keeps track of the current match.
 * Travels between Activities via Intent extras and survives
 * configuration changes via onSaveInstanceState / getSerializable().
 *
 *  • round               – the 1-based round number (1, 2, 3…)
 *  • totalRoundsPlayed   – increments every time a round finishes
 *  • opponentName        – whatever was picked in OpponentSelectActivity
 *  • gameEnded           – set true when someone guesses correctly
 *
 *   Implements java.io.Serializable so Android can bundle it
 *   without any extra boilerplate.
 */
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;   // good practice

    public int round = 1;
    public int totalRoundsPlayed = 0;
    public String opponentName = "";
    public boolean gameEnded = false;

    // --- optional convenience methods -----------------------------

    /** Call when a round is completed (regardless of who won). */
    public void incrementRoundCount() {
        totalRoundsPlayed++;
        round++;
    }

    /** Reset to brand-new match (useful from MainActivity). */
    public void reset() {
        round = 1;
        totalRoundsPlayed = 0;
        gameEnded = false;
        opponentName = "";
    }
}
