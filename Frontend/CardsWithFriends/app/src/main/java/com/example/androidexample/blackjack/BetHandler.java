package com.example.androidexample.blackjack;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.util.Log;

import com.example.androidexample.services.WebSocketManager;
import com.example.androidexample.blackjack.BlackjackModels.*;

import java.util.List;

/**
 * Handles betting functionality within the Blackjack game.
 * <p>
 * This class is responsible for validating user bets, sending bet actions
 * to the backend WebSocket server, and updating the balance and bet UI
 * visibility within the BlackjackActivity. It separates betting logic
 * from the main activity to improve modularity and maintainability.
 * </p>
 */
public class BetHandler {

    private final EditText betInput;
    private final ImageButton betButton;
    private final TextView balanceText;
    private final String username;

    /**
     * Constructs a BetHandler instance for managing bet actions and balance updates.
     *
     * @param betInput     The EditText input field used to enter bet amounts.
     * @param betButton    The ImageButton that triggers the bet action.
     * @param balanceText  The TextView displaying the player's chip balance.
     * @param username     The username of the current player.
     */
    public BetHandler(EditText betInput, ImageButton betButton, TextView balanceText, String username) {
        this.betInput = betInput;
        this.betButton = betButton;
        this.balanceText = balanceText;
        this.username = username;
    }

    /**
     * Sends a bet request to the backend WebSocket server after validating
     * the entered bet amount and ensuring the player has enough chips.
     *
     * @param gameState       The current state of the Blackjack game.
     * @param selectedPlayer  The username of the player currently being viewed.
     */
    public void sendBet(GameState gameState, String selectedPlayer) {
        //Checks if the request is coming from the this user
        if (!selectedPlayer.equals(username)) {
            betInput.setError("You can only bet as yourself!");
            return;
        }

        //Checks if the input box is empty
        String betStr = betInput.getText().toString().trim();
        if (betStr.isEmpty()) {
            betInput.setError("Enter an amount");
            return;
        }

        //Makes sure the player requesting the bet exists in the backend
        int amount = Integer.parseInt(betStr);
        PlayerState self = getPlayer(gameState.players, username);
        if (self == null) {
            betInput.setError("Player not found.");
            return;
        }

        //Makes sure the bet amount is positive
        if (amount <= 0) {
            betInput.setError("Bet must be greater than 0");
            return;
        } else if (amount > self.chips) {
            betInput.setError("Not enough balance!");
            return;
        }

        //Sends a request to the backend that a player has bet
        String betMsg = "{ \"action\": \"BET\", \"player\": \"" + username + "\", \"value\": " + amount + " }";
        WebSocketManager.getInstance().sendMessage(betMsg);
        Log.d("BetHandler", "Sent bet message: " + betMsg);

        //Removes the bet UI from view
        betInput.setText("");
        betInput.setVisibility(View.GONE);
        betButton.setVisibility(View.GONE);
    }

    /**
     * Updates the displayed chip balance and controls the visibility of
     * the betting UI (input and button) depending on the game state and
     * whether the current player has already placed a bet.
     *
     * @param gameState       The current game state.
     * @param selectedPlayer  The username of the player currently being viewed.
     */
    public void updateBalanceUI(GameState gameState, String selectedPlayer) {
        //Figure out who we are viewing
        PlayerState viewedPlayer = getPlayer(gameState.players, selectedPlayer);

        //Makes sure the player we are currently viewing is actually in the lobby
        if (viewedPlayer == null) {
            balanceText.setText("Balance: --");
            return;
        }

        //Shows the balance of either your or another person (Depending on who we are viewing)
        String label = viewedPlayer.username.equals(username)
                ? "Your Balance"
                : viewedPlayer.username + "'s Balance";
        balanceText.setText(label + ": $" + viewedPlayer.chips);

        boolean isSelf = selectedPlayer.equals(username);
        int totalBet = 0;

        //Change the amount we have bet locally
        if (isSelf) {
            for (HandState h : viewedPlayer.hands) totalBet += h.bet;
        }

        // Shows Bet UI only if we are viewing ourself and we have not made a bet
        boolean canShowBetUI = isSelf && totalBet == 0;
        betInput.setVisibility(canShowBetUI ? View.VISIBLE : View.GONE);
        betButton.setVisibility(canShowBetUI ? View.VISIBLE : View.GONE);
    }

    /**
     * Retrieves a PlayerState object matching a given username from the stored list of players.
     *
     * @param players  The list of all players in the current game.
     * @param name     The username to search for.
     * @return The PlayerState corresponding to the given username, or null if not found.
     */
    private PlayerState getPlayer(List<PlayerState> players, String name) {
        for (PlayerState p : players) {
            if (p.username.equals(name)) return p;
        }
        return null;
    }
}
