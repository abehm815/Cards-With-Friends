package com.example.androidexample.blackjack;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized model definitions for all Blackjack-related game data.
 *
 * <p>This file defines the full hierarchy of objects used to represent
 * the current game state on the client side. These mirror the JSON
 * structures sent from the backend WebSocket server and are populated
 * by {@link GameStateParser}.</p>
 *
 * <p>Each nested class represents a logical entity:
 * <ul>
 *   <li>{@link GameState} — overall snapshot of the lobby</li>
 *   <li>{@link DealerState} — the dealer’s hand and value</li>
 *   <li>{@link PlayerState} — an individual player’s info and hands</li>
 *   <li>{@link HandState} — a single hand containing cards and bet</li>
 *   <li>{@link CardState} — a single playing card</li>
 * </ul>
 * </p>
 */
public class BlackjackModels {

    // ==========================================================
    // GameState (Root of all game data)
    // ==========================================================

    /**
     * Represents the full current state of a Blackjack game lobby.
     * This object encapsulates everything needed to render the game UI.
     */
    public static class GameState {
        /** Unique lobby code shared between players (used for joining). */
        public String lobbyCode;

        /** Whether a round of Blackjack is currently active. */
        public boolean roundInProgress;

        /** Username of the player whose turn it currently is. */
        public String currentTurn;

        /** The dealer's cards and total hand value. */
        public DealerState dealer;

        /** All players participating in the current lobby. */
        public List<PlayerState> players = new ArrayList<>();
    }

    // ==========================================================
    // PlayerState (Individual player info)
    // ==========================================================

    /**
     * Represents an individual player's status and hands within a game.
     */
    public static class PlayerState {
        /** The player's unique username (used as identifier). */
        public String username;

        /** The number of chips the player currently has. */
        public int chips;

        /** Whether the player has already placed a bet this round. */
        public boolean hasBet;

        /** All active hands that belong to this player. */
        public List<HandState> hands = new ArrayList<>();
    }

    // ==========================================================
    // DealerState (House hand info)
    // ==========================================================

    /**
     * Represents the dealer's cards and total hand value.
     */
    public static class DealerState {
        /** The current total value of the dealer’s hand. */
        public int handValue;

        /** All cards currently held by the dealer. */
        public List<CardState> hand = new ArrayList<>();
    }

    // ==========================================================
    // HandState (Individual hand)
    // ==========================================================

    /**
     * Represents a single Blackjack hand — for either a player or the dealer.
     */
    public static class HandState {
        /** Index of this hand (used for split-hand identification). */
        public int handIndex;

        /** Total numerical value of this hand (e.g. 18, 21, etc.). */
        public int handValue;

        /** The amount of chips wagered on this hand. */
        public int bet;

        /** Whether the player has stood on this hand (no more actions). */
        public boolean hasStood;

        /** Whether this hand can currently be split into two. */
        public boolean canSplit;

        /** List of cards that make up this hand. */
        public List<CardState> hand = new ArrayList<>();
    }

    // ==========================================================
    // CardState (Single playing card)
    // ==========================================================

    /**
     * Represents a single playing card in the deck or hand.
     */
    public static class CardState {
        /** Numeric value of the card (1–11 for Aces, 10 for face cards, etc.). */
        public int value;

        /** Suit of the card (e.g., "h" for hearts, "s" for spades). */
        public String suit;

        /** Whether the card is currently visible (face-up) to the player. */
        public boolean isShowing;
    }
}
