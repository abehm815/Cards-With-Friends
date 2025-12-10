package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.example.androidexample.services.CardView;
import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;

import org.java_websocket.handshake.ServerHandshake;

public class EuchreActivity extends AppCompatActivity implements WebSocketListener {

    private static final String TAG = "EuchreActivity";

    private static final float CENTER_X_BIAS = 0.5f;
    private static final float CENTER_Y_BIAS = 0.75f;
    private static final float RADIUS_BIAS = 1.2f;
    private static final float TOTAL_SPREAD = 25f;

    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 200;

    private String joinCode;
    private String username;
    private boolean isHost;
    private String gameType;
    private ArrayList<String> playerList;

    private ConstraintLayout rootLayout;
    private List<CardView> handCards;
    private List<CardView> trickCards;
    private GameState gameState = new GameState();
    private boolean gameStarted = false;
    private boolean dialogShowing = false; // Prevent multiple dialogs
    private String lastKnownPhase = ""; // Track phase changes
    private String lastKnownDealer = ""; // Track round changes

    // UI Elements
    private LinearLayout teamScoreLayout;
    private LinearLayout playerInfoLayout;
    private Button passButton;
    private Button pickUpButton;
    private Button orderUpButton;

    // Game over screen elements
    private LinearLayout gameOverScreen;
    private TextView winningTeamText;
    private TextView winningMembersText;
    private TextView finalScoreText;
    private Button returnToLobbyButton;
    private Button startGameButton;
    private TextView gameStatusText;
    private TextView trumpText;
    private TextView kittyCardText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_euchre);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");
        isHost = intent.getBooleanExtra("HOST", false);
        playerList = intent.getStringArrayListExtra("PLAYERS");

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.rootLayout);
        playerInfoLayout = findViewById(R.id.playerList);

        // Get buttons from XML
        passButton = findViewById(R.id.euchre_pass_button);
        orderUpButton = findViewById(R.id.euchre_orderup_button);
        pickUpButton = findViewById(R.id.euchre_pickup_button);

        // Initialize game over screen elements
        gameOverScreen = findViewById(R.id.gameOverScreen);
        winningTeamText = findViewById(R.id.winningTeamText);
        winningMembersText = findViewById(R.id.winningMembersText);
        finalScoreText = findViewById(R.id.finalScoreText);
        returnToLobbyButton = findViewById(R.id.returnToLobbyButton);

        handCards = new ArrayList<>();
        trickCards = new ArrayList<>();

        // Create team score layout dynamically
        teamScoreLayout = new LinearLayout(this);
        teamScoreLayout.setId(View.generateViewId());
        teamScoreLayout.setOrientation(LinearLayout.HORIZONTAL);
        ConstraintLayout.LayoutParams scoreParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        teamScoreLayout.setLayoutParams(scoreParams);
        rootLayout.addView(teamScoreLayout);

        // Position team score at top
        ConstraintSet set = new ConstraintSet();
        set.clone(rootLayout);
        set.connect(teamScoreLayout.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START, 0);
        set.connect(teamScoreLayout.getId(), ConstraintSet.END, rootLayout.getId(), ConstraintSet.END, 0);
        set.connect(teamScoreLayout.getId(), ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 20);
        set.applyTo(rootLayout);

        // Create trump display
        trumpText = new TextView(this);
        trumpText.setId(View.generateViewId());
        trumpText.setTextSize(20);
        trumpText.setTextColor(0xFFFFFFFF);
        trumpText.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams trumpParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        trumpText.setLayoutParams(trumpParams);
        rootLayout.addView(trumpText);

        set.clone(rootLayout);
        set.connect(trumpText.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START, 0);
        set.connect(trumpText.getId(), ConstraintSet.END, rootLayout.getId(), ConstraintSet.END, 0);
        set.connect(trumpText.getId(), ConstraintSet.TOP, teamScoreLayout.getId(), ConstraintSet.BOTTOM, 20);
        set.applyTo(rootLayout);

        // Create kitty card display
        kittyCardText = new TextView(this);
        kittyCardText.setId(View.generateViewId());
        kittyCardText.setTextSize(16);
        kittyCardText.setTextColor(0xFFFFFFFF);
        kittyCardText.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams kittyParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        kittyCardText.setLayoutParams(kittyParams);
        rootLayout.addView(kittyCardText);

        set.clone(rootLayout);
        set.connect(kittyCardText.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START, 0);
        set.connect(kittyCardText.getId(), ConstraintSet.END, rootLayout.getId(), ConstraintSet.END, 0);
        set.connect(kittyCardText.getId(), ConstraintSet.TOP, trumpText.getId(), ConstraintSet.BOTTOM, 10);
        set.applyTo(rootLayout);

        // Create start game button dynamically
        startGameButton = new Button(this);
        startGameButton.setId(View.generateViewId());
        startGameButton.setText("Start Game");
        startGameButton.setVisibility(isHost ? View.VISIBLE : View.GONE);
        ConstraintLayout.LayoutParams startParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        startGameButton.setLayoutParams(startParams);
        rootLayout.addView(startGameButton);

        set.clone(rootLayout);
        set.connect(startGameButton.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START, 0);
        set.connect(startGameButton.getId(), ConstraintSet.END, rootLayout.getId(), ConstraintSet.END, 0);
        set.connect(startGameButton.getId(), ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 0);
        set.connect(startGameButton.getId(), ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM, 0);
        set.applyTo(rootLayout);

        // Create game status text
        gameStatusText = new TextView(this);
        gameStatusText.setId(View.generateViewId());
        gameStatusText.setTextSize(18);
        gameStatusText.setTextColor(0xFFFFFFFF);
        gameStatusText.setText("Waiting for host to start game...");
        gameStatusText.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams statusParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        gameStatusText.setLayoutParams(statusParams);
        rootLayout.addView(gameStatusText);

        set.clone(rootLayout);
        set.connect(gameStatusText.getId(), ConstraintSet.START, rootLayout.getId(), ConstraintSet.START, 0);
        set.connect(gameStatusText.getId(), ConstraintSet.END, rootLayout.getId(), ConstraintSet.END, 0);
        set.connect(gameStatusText.getId(), ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 50);
        set.applyTo(rootLayout);

        Log.d(TAG, "initializeViews completed");
    }

    private void setupListeners() {
        Log.d(TAG, "Setting up listeners...");

        passButton.setOnClickListener(v -> {
            Log.d(TAG, "Pass button clicked");
            handlePassButton();
        });

        pickUpButton.setOnClickListener(v -> {
            Log.d(TAG, "Pick up button clicked");
            showDiscardCardDialog();
        });

        orderUpButton.setOnClickListener(v -> {
            Log.d(TAG, "Order up button clicked");
            handleOrderUpButton();
        });

        startGameButton.setOnClickListener(v -> {
            Log.d(TAG, "Start game button clicked");
            startGame();
        });

        returnToLobbyButton.setOnClickListener(v -> {
            Log.d(TAG, "Return to lobby button clicked");
            exitToLobby();
        });

        Log.d(TAG, "All listeners set up successfully");
    }

    @Override
    protected void onStart() {
        super.onStart();
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/euchre/" + joinCode;
        Log.d(TAG, "Connecting to WebSocket: " + wsUrl);
        WebSocketManager.getInstance().connectWebSocket(wsUrl);
        WebSocketManager.getInstance().setWebSocketListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebSocketManager.getInstance().disconnectWebSocket();
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> {
            Log.d(TAG, "WebSocket connected");
            Toast.makeText(this, "Connected to game server", Toast.LENGTH_SHORT).show();
            requestGameState();
        });
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            Log.d(TAG, "========================================");
            Log.d(TAG, "Received message: " + message);
            Log.d(TAG, "========================================");

            if (message == null || message.trim().isEmpty()) {
                Log.w(TAG, "Received null or empty message");
                return;
            }

            if (!message.trim().startsWith("{")) {
                Log.d(TAG, "Received plain text message: " + message);

                // Check if game is over - MUST handle this BEFORE "no game running" check
                if (message.toLowerCase().contains("game over")) {
                    Log.d(TAG, "Game over detected from message: " + message);

                    // Capture current team data IMMEDIATELY before any delays
                    final List<TeamState> capturedTeams = new ArrayList<>();
                    for (TeamState team : gameState.teams) {
                        capturedTeams.add(team);
                    }
                    final String gameOverMessage = message;

                    Log.d(TAG, "Captured " + capturedTeams.size() + " teams at game over");

                    // Request state one final time to try to get updated team scores
                    requestGameState();

                    // Show game over screen after a delay to let state come in
                    new Handler().postDelayed(() -> {
                        // First check if we got new team data from the state request
                        List<TeamState> teamsToUse = gameState.teams.size() >= 2 ? gameState.teams : capturedTeams;

                        Log.d(TAG, "Showing game over screen. Teams available: " + teamsToUse.size());

                        // Check if we have team data (even with score 0)
                        if (teamsToUse.size() >= 2) {
                            // Determine winning team - either by highest score or by parsing message
                            int winningTeamIndex = -1;
                            int highestScore = -1;

                            // First try to find team with highest score
                            for (int i = 0; i < teamsToUse.size(); i++) {
                                if (teamsToUse.get(i).score > highestScore) {
                                    highestScore = teamsToUse.get(i).score;
                                    winningTeamIndex = i;
                                }
                            }

                            // If both teams have score 0, try to use tricksTaken
                            if (highestScore == 0) {
                                int mostTricks = -1;
                                for (int i = 0; i < teamsToUse.size(); i++) {
                                    if (teamsToUse.get(i).tricksWon > mostTricks) {
                                        mostTricks = teamsToUse.get(i).tricksWon;
                                        winningTeamIndex = i;
                                    }
                                }
                            }

                            // Show the winning screen with best available data
                            if (winningTeamIndex >= 0) {
                                final int finalWinningTeamIndex = winningTeamIndex;
                                final List<TeamState> finalTeams = teamsToUse;

                                runOnUiThread(() -> {
                                    // Hide start button and action buttons
                                    startGameButton.setVisibility(View.GONE);
                                    passButton.setVisibility(View.GONE);
                                    pickUpButton.setVisibility(View.GONE);
                                    orderUpButton.setVisibility(View.GONE);

                                    TeamState winningTeam = finalTeams.get(finalWinningTeamIndex);

                                    // Team number
                                    winningTeamText.setText("Team " + (finalWinningTeamIndex + 1) + " Wins!");

                                    // Team members
                                    StringBuilder membersText = new StringBuilder();
                                    for (String member : winningTeam.members) {
                                        if (membersText.length() > 0) membersText.append(" & ");
                                        membersText.append(member);
                                    }

                                    if (membersText.length() > 0) {
                                        winningMembersText.setText(membersText.toString() + " Have Won!");
                                    } else {
                                        winningMembersText.setText("Have Won!");
                                    }

                                    // Try to get score from message if state score is 0
                                    int displayScore = winningTeam.score;
                                    if (displayScore == 0 && gameOverMessage.contains("Winning team score:")) {
                                        try {
                                            String[] parts = gameOverMessage.split("Winning team score:");
                                            if (parts.length > 1) {
                                                String scoreStr = parts[1].trim().replaceAll("[^0-9]", "");
                                                if (!scoreStr.isEmpty()) {
                                                    displayScore = Integer.parseInt(scoreStr);
                                                }
                                            }
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error parsing score from message", e);
                                        }
                                    }

                                    finalScoreText.setText("Final Score: " + displayScore + " points");
                                    gameOverScreen.setVisibility(View.VISIBLE);

                                    Log.d(TAG, "Game over screen displayed - Team " + (finalWinningTeamIndex + 1) + " wins!");
                                });
                            } else {
                                showGenericGameOver(message);
                            }
                        } else {
                            showGenericGameOver(message);
                        }
                    }, 800);  // Increased delay to 800ms to give more time for state to arrive
                    return;
                }

                // Check if there's no game running - but don't reset if game over screen is showing
                if (message.toLowerCase().contains("no game running")) {
                    Log.d(TAG, "No game currently running");
                    // Only reset if game over screen is NOT showing
                    if (gameOverScreen.getVisibility() != View.VISIBLE) {
                        Log.d(TAG, "Resetting UI to start screen");
                        resetGameUI();
                    } else {
                        Log.d(TAG, "Game over screen is showing, not resetting UI");
                    }
                    return;
                }

                if (!message.toLowerCase().contains("error") &&
                        !message.toLowerCase().contains("null")) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }

                // Check for events that need state updates
                boolean isImportantEvent = message.toLowerCase().contains("started") ||
                        message.toLowerCase().contains("new game") ||
                        message.toLowerCase().contains("new round") ||
                        message.toLowerCase().contains("passed") ||
                        message.toLowerCase().contains("picked up") ||
                        message.toLowerCase().contains("pick up") ||
                        message.toLowerCase().contains("chose") ||
                        message.toLowerCase().contains("trump") ||
                        message.toLowerCase().contains("played") ||
                        message.toLowerCase().contains("trick");

                // Check for round-ending events that need faster updates
                boolean isRoundEnding = message.toLowerCase().contains("trick won") ||
                        message.toLowerCase().contains("wins the trick") ||
                        message.toLowerCase().contains("new round") ||
                        message.toLowerCase().contains("points");

                if (isImportantEvent) {
                    // Use shorter delay for round-ending events
                    int delay = isRoundEnding ? 800 : 500;
                    Log.d(TAG, "Game event detected, requesting state update in " + delay + "ms...");
                    new Handler().postDelayed(this::requestGameState, delay);
                }
                return;
            }

            try {
                JSONObject json = new JSONObject(message);
                Log.d(TAG, "Parsing JSON game state...");
                GameState newState = parseGameState(json);

                // Detect if a new round has started
                boolean newRoundStarted = false;
                if (!lastKnownDealer.isEmpty() && !newState.dealer.isEmpty() &&
                        !lastKnownDealer.equals(newState.dealer)) {
                    newRoundStarted = true;
                    Log.d(TAG, "NEW ROUND DETECTED! Dealer changed from " + lastKnownDealer + " to " + newState.dealer);
                }

                // Also detect new round if we went from PLAYING back to BIDDING with a new kitty card
                if (gameState.phase.equals("PLAYING") && newState.phase.equals("BIDDING") &&
                        newState.kittyCard != null) {
                    newRoundStarted = true;
                    Log.d(TAG, "NEW ROUND DETECTED! Phase reset to BIDDING with new kitty card");
                }

                // Update game state
                gameState = newState;
                gameStarted = true;

                startGameButton.setVisibility(View.GONE);
                gameStatusText.setVisibility(View.GONE);

                Log.d(TAG, "=== GAME STATE PARSED ===");
                Log.d(TAG, "Previous Phase: " + lastKnownPhase);
                Log.d(TAG, "New Phase: " + gameState.phase);
                Log.d(TAG, "Current Player: " + gameState.currentPlayer);
                Log.d(TAG, "My Username: " + username);
                Log.d(TAG, "Dealer: " + gameState.dealer);
                Log.d(TAG, "Trump: " + gameState.trump);
                Log.d(TAG, "Kitty Card: " + (gameState.kittyCard != null ? gameState.kittyCard.value + gameState.kittyCard.suit : "null"));
                Log.d(TAG, "Player count: " + gameState.players.size());
                Log.d(TAG, "New Round Started: " + newRoundStarted);
                Log.d(TAG, "========================");

                // If new round started, show a toast
                if (newRoundStarted) {
                    Toast.makeText(this, "New round! Dealer: " + gameState.dealer, Toast.LENGTH_LONG).show();
                    // Reset dialog flag for new round
                    dialogShowing = false;
                }

                updateUIFromState();

            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error: " + message, e);
                Toast.makeText(this, "Error parsing game state", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error processing message: " + message, e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.e(TAG, "Closed → code=" + code + ", reason=" + reason + ", remote=" + remote);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "Error → " + ex.getMessage(), ex);
    }

    // -------------------------------------------------------------------
    // Dialog Methods for Card Selection
    // -------------------------------------------------------------------

    private void showDiscardCardDialog() {
        PlayerState currentPlayer = gameState.players.stream().filter(p -> p.username.equals(username)).findFirst().orElse(null);

        if (currentPlayer == null || currentPlayer.hand.isEmpty()) {
            Toast.makeText(this, "No cards in hand", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create array of card display strings
        String[] cardOptions = new String[currentPlayer.hand.size()];
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            CardState card = currentPlayer.hand.get(i);
            String suitSymbol = getSuitSymbol(card.suit);
            cardOptions[i] = card.value + suitSymbol;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a card to discard");
        builder.setItems(cardOptions, (dialog, which) -> {
            CardState selectedCard = currentPlayer.hand.get(which);
            handlePickUpWithCard(selectedCard);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showPlayCardDialog() {
        // Prevent multiple dialogs
        if (dialogShowing) {
            Log.d(TAG, "Dialog already showing, skipping...");
            return;
        }

        // Verify it's actually our turn
        if (!username.equals(gameState.currentPlayer)) {
            Log.d(TAG, "Not our turn, current player: " + gameState.currentPlayer);
            return;
        }

        // Verify we're in PLAYING phase
        if (!gameState.phase.equals("PLAYING")) {
            Log.d(TAG, "Not in PLAYING phase, current phase: " + gameState.phase);
            return;
        }

        PlayerState currentPlayer = gameState.players.stream().filter(p -> p.username.equals(username)).findFirst().orElse(null);

        if (currentPlayer == null || currentPlayer.hand.isEmpty()) {
            Log.w(TAG, "No cards available to play");
            Toast.makeText(this, "No cards in hand", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create array of card display strings
        String[] cardOptions = new String[currentPlayer.hand.size()];
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            CardState card = currentPlayer.hand.get(i);
            String suitSymbol = getSuitSymbol(card.suit);
            cardOptions[i] = card.value + suitSymbol;
        }

        dialogShowing = true;
        Log.d(TAG, "Showing play card dialog with " + cardOptions.length + " cards");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your turn - Select a card to play");
        builder.setItems(cardOptions, (dialog, which) -> {
            dialogShowing = false;
            CardState selectedCard = currentPlayer.hand.get(which);
            playCard(selectedCard);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialogShowing = false;
        });
        builder.setOnCancelListener(dialog -> {
            dialogShowing = false;
        });
        builder.show();
    }

    // -------------------------------------------------------------------
    // WebSocket Message Sending
    // -------------------------------------------------------------------

    private void startGame() {
        Log.d(TAG, "START GAME BUTTON CLICKED!");

        try {
            JSONObject message = new JSONObject();
            message.put("type", "start");

            JSONArray playerArray = new JSONArray();
            if (playerList != null && !playerList.isEmpty()) {
                for (String player : playerList) {
                    playerArray.put(player);
                }
            } else {
                playerArray.put(username);
            }

            message.put("players", playerArray);

            String msg = message.toString();
            Log.d(TAG, "Sending start message: " + msg);

            WebSocketManager.getInstance().sendMessage(msg);
            Toast.makeText(this, "Starting game...", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            Log.e(TAG, "Error creating start message", e);
            Toast.makeText(this, "Error starting game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestGameState() {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "state");

            String msg = message.toString();
            Log.d(TAG, "Requesting state: " + msg);
            WebSocketManager.getInstance().sendMessage(msg);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating state request", e);
        }
    }

    private void resetGameUI() {
        runOnUiThread(() -> {
            // Hide game over screen if it's showing
            if (gameOverScreen != null) {
                gameOverScreen.setVisibility(View.GONE);
            }

            // Clear the game state
            gameState = new GameState();
            gameStarted = false;
            dialogShowing = false;
            lastKnownPhase = "";
            lastKnownDealer = "";

            // Show start button
            startGameButton.setVisibility(View.VISIBLE);
            startGameButton.setEnabled(true);

            // Show game status
            gameStatusText.setVisibility(View.VISIBLE);
            gameStatusText.setText("Game Ended - Start a new game!");

            // Hide all action buttons
            passButton.setVisibility(View.GONE);
            pickUpButton.setVisibility(View.GONE);
            orderUpButton.setVisibility(View.GONE);

            // Clear card displays by removing all views from layouts
            if (teamScoreLayout != null) {
                teamScoreLayout.removeAllViews();
            }
            if (playerInfoLayout != null) {
                playerInfoLayout.removeAllViews();
            }

            // Reset trump and kitty text
            trumpText.setText("Trump: -");
            kittyCardText.setText("Kitty: -");

            // Clear the constraint layout of any dynamically added cards
            if (rootLayout != null) {
                // Remove any card views that were added
                for (int i = rootLayout.getChildCount() - 1; i >= 0; i--) {
                    View child = rootLayout.getChildAt(i);
                    // Only remove CardViews, not the main UI elements
                    if (child instanceof CardView) {
                        rootLayout.removeView(child);
                    }
                }
            }

            Log.d(TAG, "Game UI reset - ready for new game");
        });
    }

    private void handlePassButton() {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "pass");
            message.put("player", username);

            Log.d(TAG, "Sending pass: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            new Handler().postDelayed(this::requestGameState, 500);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating pass message", e);
        }
    }

    private void handleOrderUpButton() {
        if (gameState.kittyCard != null) {
            try {
                JSONObject message = new JSONObject();
                message.put("type", "chooseSuit");
                message.put("player", username);
                message.put("suit", gameState.kittyCard.suit);

                Log.d(TAG, "Sending choose suit (order up): " + message.toString());
                WebSocketManager.getInstance().sendMessage(message.toString());

                Toast.makeText(this, "Ordering up " + getSuitSymbol(gameState.kittyCard.suit), Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(this::requestGameState, 500);

            } catch (JSONException e) {
                Log.e(TAG, "Error creating order up message", e);
            }
        } else {
            Toast.makeText(this, "No kitty card available", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePickUpWithCard(CardState card) {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "pickUp");
            message.put("dealer", username);

            JSONObject droppedCard = new JSONObject();
            droppedCard.put("value", convertStringToValue(card.value));
            droppedCard.put("suit", card.suit);
            message.put("droppedCard", droppedCard);

            Log.d(TAG, "Sending pick up: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            Toast.makeText(this, "Picking up, discarding " + card.value + getSuitSymbol(card.suit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(this::requestGameState, 800);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating pick up message", e);
        }
    }

    private void showSuitSelectionDialog() {
        String[] suits = {"Hearts ♥", "Diamonds ♦", "Clubs ♣", "Spades ♠"};
        String[] suitCodes = {"h", "d", "c", "s"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Trump Suit");
        builder.setItems(suits, (dialog, which) -> {
            sendChooseSuit(suitCodes[which]);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendChooseSuit(String suit) {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "chooseSuit");
            message.put("player", username);
            message.put("suit", suit);

            Log.d(TAG, "Sending choose suit: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            new Handler().postDelayed(this::requestGameState, 500);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating choose suit message", e);
        }
    }

    private void playCard(CardState card) {
        try {
            // Check if this is the last card in our hand
            PlayerState myPlayer = null;
            for (PlayerState p : gameState.players) {
                if (p.username.equals(username)) {
                    myPlayer = p;
                    break;
                }
            }
            boolean isLastCard = (myPlayer != null && myPlayer.hand.size() == 1);

            JSONObject message = new JSONObject();
            message.put("type", "play");
            message.put("player", username);

            JSONObject cardObj = new JSONObject();
            cardObj.put("value", convertStringToValue(card.value));
            cardObj.put("suit", card.suit);
            message.put("card", cardObj);

            Log.d(TAG, "Sending play card: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            Toast.makeText(this, "Playing " + card.value + getSuitSymbol(card.suit), Toast.LENGTH_SHORT).show();

            // Request state quickly to show the card being played
            new Handler().postDelayed(this::requestGameState, 300);

            // Request again after longer delay to catch potential round transition
            new Handler().postDelayed(this::requestGameState, 1200);

            // If this was our last card, request state again after even longer to ensure we catch game over
            if (isLastCard) {
                Log.d(TAG, "Last card played - scheduling additional state checks for game over");
                new Handler().postDelayed(this::requestGameState, 2000);
                new Handler().postDelayed(this::requestGameState, 3000);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error creating play message", e);
        }
    }

    // -------------------------------------------------------------------
    // JSON → GameState Parsing
    // -------------------------------------------------------------------

    private GameState parseGameState(JSONObject json) throws JSONException {
        GameState state = new GameState();

        JSONObject gameData = json.has("game") ? json.getJSONObject("game") : json;

        // Parse phase
        if (gameData.has("isBidding")) {
            boolean isBidding = gameData.getBoolean("isBidding");
            state.phase = isBidding ? "BIDDING" : "PLAYING";
            Log.d(TAG, "Parsed isBidding: " + isBidding + " -> phase: " + state.phase);
        } else if (gameData.has("phase")) {
            state.phase = gameData.getString("phase");
            Log.d(TAG, "Parsed phase from JSON: " + state.phase);
        } else {
            state.phase = "BIDDING";
            Log.d(TAG, "No phase info found, defaulting to BIDDING");
        }

        // WORKAROUND: Check match history for PICKED_UP or TRUMP_CHOSEN event to infer phase
        // If someone picked up or chose trump, we should be in PLAYING phase
        if (state.phase.equals("BIDDING") && gameData.has("matchHistory")) {
            try {
                JSONObject matchHistory = gameData.getJSONObject("matchHistory");
                if (matchHistory.has("events")) {
                    JSONArray events = matchHistory.getJSONArray("events");
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject event = events.getJSONObject(i);
                        String eventType = event.optString("eventType");

                        if (eventType.equals("PICKED_UP") || eventType.equals("TRUMP_CHOSEN")) {
                            state.phase = "PLAYING";
                            // Also extract trump from the event
                            String trumpFromEvent = event.optString("trumpSuit");
                            if (!trumpFromEvent.isEmpty()) {
                                state.trump = String.valueOf(trumpFromEvent.charAt(0));
                            }
                            Log.d(TAG, "WORKAROUND: Found " + eventType + " event, forcing phase to PLAYING with trump: " + state.trump);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                Log.w(TAG, "Error checking match history for phase inference", e);
            }
        }

        // Parse current player
        if (gameData.has("currentPlayerUsername")) {
            state.currentPlayer = gameData.getString("currentPlayerUsername");
        } else if (gameData.has("currentPlayer")) {
            state.currentPlayer = gameData.getString("currentPlayer");
        } else {
            state.currentPlayer = "";
        }

        // Parse dealer
        if (gameData.has("currentDealerUsername")) {
            state.dealer = gameData.getString("currentDealerUsername");
        } else if (gameData.has("dealer")) {
            state.dealer = gameData.getString("dealer");
        } else {
            state.dealer = "";
        }

        // Parse trump (but don't overwrite if already set by workaround)
        if (state.trump == null || state.trump.isEmpty()) {
            if (gameData.has("trumpSuit")) {
                String trumpStr = gameData.getString("trumpSuit");
                state.trump = trumpStr.isEmpty() ? "" : String.valueOf(trumpStr.charAt(0));
            } else if (gameData.has("trump")) {
                state.trump = gameData.getString("trump");
            } else {
                state.trump = "";
            }
        }

        // Parse option card (kitty card)
        if (gameData.has("optionCard") && !gameData.isNull("optionCard")) {
            JSONObject option = gameData.getJSONObject("optionCard");
            state.kittyCard = new CardState();
            state.kittyCard.value = convertValueToString(option.optInt("value"));
            state.kittyCard.suit = String.valueOf(option.optString("suit").charAt(0));
        }

        // Parse teams
        JSONArray teamsArray = gameData.optJSONArray("teams");
        if (teamsArray != null) {
            for (int i = 0; i < teamsArray.length(); i++) {
                JSONObject teamObj = teamsArray.getJSONObject(i);
                TeamState team = new TeamState();
                team.score = teamObj.optInt("score", 0);
                team.tricksWon = teamObj.optInt("tricksWon", 0);

                JSONArray membersArray = teamObj.optJSONArray("members");
                if (membersArray != null) {
                    for (int j = 0; j < membersArray.length(); j++) {
                        team.members.add(membersArray.getString(j));
                    }
                }
                state.teams.add(team);
            }
        } else {
            // Try parsing teamOne and teamTwo structure instead
            if (gameData.has("teamOne") && !gameData.isNull("teamOne")) {
                JSONObject teamOneObj = gameData.getJSONObject("teamOne");
                TeamState team1 = new TeamState();
                team1.score = teamOneObj.optInt("score", 0);
                team1.tricksWon = teamOneObj.optInt("tricksTaken", 0); // Note: backend uses "tricksTaken"

                // Parse team members from teamMembersAsStrings array
                JSONArray team1Members = teamOneObj.optJSONArray("teamMembersAsStrings");
                if (team1Members != null) {
                    for (int i = 0; i < team1Members.length(); i++) {
                        team1.members.add(team1Members.getString(i));
                    }
                }
                state.teams.add(team1);
            }

            if (gameData.has("teamTwo") && !gameData.isNull("teamTwo")) {
                JSONObject teamTwoObj = gameData.getJSONObject("teamTwo");
                TeamState team2 = new TeamState();
                team2.score = teamTwoObj.optInt("score", 0);
                team2.tricksWon = teamTwoObj.optInt("tricksTaken", 0); // Note: backend uses "tricksTaken"

                // Parse team members from teamMembersAsStrings array
                JSONArray team2Members = teamTwoObj.optJSONArray("teamMembersAsStrings");
                if (team2Members != null) {
                    for (int i = 0; i < team2Members.length(); i++) {
                        team2.members.add(team2Members.getString(i));
                    }
                }
                state.teams.add(team2);
            }
        }

        // Parse players
        JSONArray playersArray = gameData.optJSONArray("players");
        if (playersArray != null) {
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject p = playersArray.getJSONObject(i);
                PlayerState ps = new PlayerState();
                ps.username = p.optString("username");

                JSONArray hand = p.optJSONArray("hand");
                if (hand != null) {
                    for (int j = 0; j < hand.length(); j++) {
                        JSONObject c = hand.getJSONObject(j);
                        CardState card = new CardState();
                        card.value = convertValueToString(c.optInt("value"));
                        String suitStr = c.optString("suit");
                        card.suit = suitStr.isEmpty() ? "" : String.valueOf(suitStr.charAt(0));
                        ps.hand.add(card);
                    }
                }

                ps.trickCount = p.optInt("trickCount", 0);
                state.players.add(ps);
            }

            // Construct teams if not provided
            if (state.teams.isEmpty() && state.players.size() == 4) {
                TeamState team1 = new TeamState();
                team1.members.add(state.players.get(0).username);
                team1.members.add(state.players.get(2).username);
                team1.tricksWon = state.players.get(0).trickCount + state.players.get(2).trickCount;

                TeamState team2 = new TeamState();
                team2.members.add(state.players.get(1).username);
                team2.members.add(state.players.get(3).username);
                team2.tricksWon = state.players.get(1).trickCount + state.players.get(3).trickCount;

                state.teams.add(team1);
                state.teams.add(team2);
            }
        }

        // Parse current trick
        JSONArray trickArray = gameData.optJSONArray("currentTrick");
        if (trickArray != null) {
            for (int i = 0; i < trickArray.length(); i++) {
                JSONObject playedCardObj = trickArray.getJSONObject(i);
                PlayedCard pc = new PlayedCard();

                if (playedCardObj.has("player")) {
                    pc.player = playedCardObj.getString("player");
                }

                JSONObject cardObj = null;
                if (playedCardObj.has("card")) {
                    cardObj = playedCardObj.getJSONObject("card");
                } else if (playedCardObj.has("value") && playedCardObj.has("suit")) {
                    cardObj = playedCardObj;
                }

                if (cardObj != null) {
                    pc.card = new CardState();
                    pc.card.value = convertValueToString(cardObj.optInt("value"));
                    String suitStr = cardObj.optString("suit");
                    pc.card.suit = suitStr.isEmpty() ? "" : String.valueOf(suitStr.charAt(0));
                }

                state.currentTrick.add(pc);
            }
        }

        return state;
    }

    private String convertValueToString(int value) {
        switch (value) {
            case 14: return "A";
            case 13: return "K";
            case 12: return "Q";
            case 11: return "J";
            default: return String.valueOf(value);
        }
    }

    private int convertStringToValue(String valueStr) {
        switch (valueStr.toUpperCase()) {
            case "A": return 14;
            case "K": return 13;
            case "Q": return 12;
            case "J": return 11;
            default:
                try {
                    return Integer.parseInt(valueStr);
                } catch (NumberFormatException e) {
                    return 0;
                }
        }
    }

    // -------------------------------------------------------------------
    // UI Update Methods
    // -------------------------------------------------------------------

    private void updateUIFromState() {
        try {
            Log.d(TAG, "Starting UI update...");

            updateTeamScores();
            updatePlayerInfo();
            updateTrumpDisplay();
            updateHandCards();
            updateTrickDisplay();
            updateActionButtons();

            Log.d(TAG, "UI updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
            Toast.makeText(this, "UI update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showWinningScreen() {
        // Determine winning team
        TeamState winningTeam = null;
        int winningTeamNumber = 0;
        int highestScore = -1;

        for (int i = 0; i < gameState.teams.size(); i++) {
            TeamState team = gameState.teams.get(i);
            if (team.score > highestScore) {
                highestScore = team.score;
                winningTeam = team;
                winningTeamNumber = i + 1;
            }
        }

        if (winningTeam == null) {
            // Fallback if no team data
            exitToLobby();
            return;
        }

        // Update the game over screen with winning information
        final int finalWinningTeamNumber = winningTeamNumber;
        final int finalHighestScore = highestScore;
        final TeamState finalWinningTeam = winningTeam;

        runOnUiThread(() -> {
            // Hide the start button
            startGameButton.setVisibility(View.GONE);

            // Hide action buttons
            passButton.setVisibility(View.GONE);
            pickUpButton.setVisibility(View.GONE);
            orderUpButton.setVisibility(View.GONE);

            // Update text views
            winningTeamText.setText("Team " + finalWinningTeamNumber + " Wins!");

            // Build members string
            StringBuilder membersText = new StringBuilder();
            for (String member : finalWinningTeam.members) {
                if (membersText.length() > 0) membersText.append(" & ");
                membersText.append(member);
            }
            winningMembersText.setText(membersText.toString());

            finalScoreText.setText("Final Score: " + finalHighestScore + " points");

            // Show the game over screen
            gameOverScreen.setVisibility(View.VISIBLE);

            Log.d(TAG, "Game over screen displayed - Team " + finalWinningTeamNumber + " wins!");
        });
    }

    private void showGenericGameOver(String message) {
        runOnUiThread(() -> {
            // Hide start button and action buttons
            startGameButton.setVisibility(View.GONE);
            passButton.setVisibility(View.GONE);
            pickUpButton.setVisibility(View.GONE);
            orderUpButton.setVisibility(View.GONE);

            // Show game over screen with generic message
            winningTeamText.setText("Game Over!");
            winningMembersText.setText("A team has won!");

            // Try to extract score from message if present
            String scoreText = "Game has ended";
            if (message.contains("Winning team score:")) {
                try {
                    String[] parts = message.split("Winning team score:");
                    if (parts.length > 1) {
                        String scoreStr = parts[1].trim().replaceAll("[^0-9]", "");
                        if (!scoreStr.isEmpty()) {
                            scoreText = "Final Score: " + scoreStr + " points";
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing score from message", e);
                }
            }
            finalScoreText.setText(scoreText);

            gameOverScreen.setVisibility(View.VISIBLE);
            Log.d(TAG, "Generic game over screen shown");
        });
    }

    private void exitToLobby() {
        runOnUiThread(() -> {
            // Hide game over screen
            gameOverScreen.setVisibility(View.GONE);

            // Disconnect WebSocket
            WebSocketManager.getInstance().disconnectWebSocket();

            // Finish this activity to go back to the previous screen (lobby selection)
            finish();
        });
    }

    private boolean isGameOver() {
        // Game is over if we're in PLAYING phase and ALL players have empty hands
        // This means the last card of the last trick has been played
        if (!gameState.phase.equals("PLAYING")) {
            return false;
        }

        if (gameState.players.isEmpty()) {
            return false;
        }

        // Check if all players have no cards
        for (PlayerState player : gameState.players) {
            if (!player.hand.isEmpty()) {
                return false; // At least one player still has cards
            }
        }

        // All players have empty hands in PLAYING phase = game over
        Log.d(TAG, "Game over detected: all " + gameState.players.size() + " players have empty hands");
        return true;
    }

    private void updateTeamScores() {
        teamScoreLayout.removeAllViews();

        for (int i = 0; i < gameState.teams.size(); i++) {
            TeamState team = gameState.teams.get(i);
            TextView teamView = new TextView(this);

            String teamText = "Team " + (i + 1) + ": " + team.score + " pts";
            if (gameState.phase.equals("PLAYING")) {
                teamText += " (" + team.tricksWon + " tricks)";
            }

            teamView.setText(teamText);
            teamView.setTextSize(16);
            teamView.setTextColor(0xFFFFFFFF);
            teamView.setPadding(20, 10, 20, 10);

            // Highlight user's team
            boolean isMyTeam = false;
            for (String member : team.members) {
                if (member.equals(username)) {
                    isMyTeam = true;
                    break;
                }
            }
            if (isMyTeam) {
                teamView.setTextColor(0xFF00FF00);
                teamView.setTypeface(null, android.graphics.Typeface.BOLD);
            }

            teamScoreLayout.addView(teamView);
        }
    }

    private void updatePlayerInfo() {
        playerInfoLayout.removeAllViews();

        for (PlayerState player : gameState.players) {
            TextView playerView = new TextView(this);

            String displayText = player.username;

            if (player.username.equals(gameState.dealer)) {
                displayText += " (D)";
            }

            if (player.username.equals(gameState.currentPlayer)) {
                displayText = "▶ " + displayText;
                playerView.setTextColor(0xFFFFFF00);
            } else {
                playerView.setTextColor(0xFFFFFFFF);
            }

            if (player.username.equals(username)) {
                playerView.setTextSize(16);
                playerView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                playerView.setTextSize(14);
            }

            playerView.setText(displayText);
            playerView.setPadding(16, 8, 16, 8);
            playerInfoLayout.addView(playerView);
        }
    }

    private void updateTrumpDisplay() {
        if (gameState.trump != null && !gameState.trump.isEmpty()) {
            String suitSymbol = getSuitSymbol(gameState.trump.toLowerCase());
            trumpText.setText("Trump: " + suitSymbol);
            trumpText.setVisibility(View.VISIBLE);
        } else {
            trumpText.setVisibility(View.GONE);
        }

        if (gameState.kittyCard != null && gameState.phase.equals("BIDDING")) {
            String suitSymbol = getSuitSymbol(gameState.kittyCard.suit);
            kittyCardText.setText("Kitty: " + gameState.kittyCard.value + suitSymbol);
            kittyCardText.setVisibility(View.VISIBLE);
        } else {
            kittyCardText.setVisibility(View.GONE);
        }
    }

    private void updateHandCards() {
        for (CardView card : handCards) {
            rootLayout.removeView(card);
        }
        handCards.clear();

        PlayerState currentPlayer = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(username)) {
                currentPlayer = p;
                break;
            }
        }

        if (currentPlayer == null || currentPlayer.hand.isEmpty()) {
            return;
        }

        for (CardState cardData : currentPlayer.hand) {
            CardView card = createCardFromData(cardData.value, cardData.suit);

            // Make cards non-clickable - we'll use the dialog menu instead
            card.setClickable(false);
            card.setFocusable(false);

            rootLayout.addView(card);
            handCards.add(card);
        }

        setupCardLayout();
        animateCardsIntroduction();
    }

    private void updateTrickDisplay() {
        for (CardView card : trickCards) {
            rootLayout.removeView(card);
        }
        trickCards.clear();

        if (gameState.currentTrick.isEmpty()) {
            return;
        }

        ConstraintSet set = new ConstraintSet();
        set.clone(rootLayout);

        for (int i = 0; i < gameState.currentTrick.size(); i++) {
            PlayedCard pc = gameState.currentTrick.get(i);
            CardView card = createCardFromData(pc.card.value, pc.card.suit);
            card.setClickable(false);

            rootLayout.addView(card);
            trickCards.add(card);

            int col = i % 2;
            int row = i / 2;

            // Set fixed width and height instead of stretching
            set.constrainWidth(card.getId(), CARD_WIDTH);
            set.constrainHeight(card.getId(), CARD_HEIGHT);

            // Connect to parent for positioning
            set.connect(card.getId(), ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
            set.connect(card.getId(), ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);
            set.connect(card.getId(), ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 0);
            set.connect(card.getId(), ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM, 0);

            // Calculate position - center area with slight offsets for each card
            float xBias = 0.35f + (col * 0.3f);
            float yBias = 0.35f + (row * 0.2f);

            set.setHorizontalBias(card.getId(), xBias);
            set.setVerticalBias(card.getId(), yBias);
        }

        set.applyTo(rootLayout);
    }

    private void updateActionButtons() {
        boolean isMyTurn = username.equals(gameState.currentPlayer);
        boolean isDealer = username.equals(gameState.dealer);

        Log.d(TAG, "========================================");
        Log.d(TAG, "UPDATE ACTION BUTTONS:");
        Log.d(TAG, "Phase: " + gameState.phase);
        Log.d(TAG, "Last known phase: " + lastKnownPhase);
        Log.d(TAG, "Is my turn: " + isMyTurn);
        Log.d(TAG, "Is dealer: " + isDealer);
        Log.d(TAG, "========================================");

        passButton.setVisibility(View.GONE);
        pickUpButton.setVisibility(View.GONE);
        orderUpButton.setVisibility(View.GONE);

        if (gameState.phase.equals("BIDDING") && isMyTurn) {
            Log.d(TAG, "Showing BIDDING buttons");
            passButton.setVisibility(View.VISIBLE);

            if (isDealer) {
                Log.d(TAG, "Dealer - showing Pick Up button");
                pickUpButton.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "Non-dealer - showing Order Up button");
                orderUpButton.setVisibility(View.VISIBLE);
            }
        } else if (gameState.phase.equals("PLAYING")) {
            // Only show dialog if phase just changed to PLAYING AND it's our turn
            boolean phaseJustChangedToPlaying = !lastKnownPhase.equals("PLAYING") && gameState.phase.equals("PLAYING");

            if (phaseJustChangedToPlaying) {
                Log.d(TAG, "Phase changed to PLAYING!");
                if (isMyTurn) {
                    Log.d(TAG, "It's our turn - showing play card dialog in 500ms");
                    new Handler().postDelayed(this::showPlayCardDialog, 500);
                } else {
                    Log.d(TAG, "Not our turn yet, current player: " + gameState.currentPlayer);
                }
            } else if (isMyTurn && !dialogShowing) {
                // If we receive an update during PLAYING phase and it's our turn but dialog isn't showing
                Log.d(TAG, "PLAYING phase, our turn, no dialog - showing dialog");
                new Handler().postDelayed(this::showPlayCardDialog, 300);
            }
        }

        // Update last known phase and dealer
        lastKnownPhase = gameState.phase;
        lastKnownDealer = gameState.dealer;
    }

    private CardView createCardFromData(String value, String suit) {
        CardView card = new CardView(this);
        card.setId(View.generateViewId());
        card.setLayoutParams(new ConstraintLayout.LayoutParams(CARD_WIDTH, CARD_HEIGHT));
        card.setCard(value, suit, true);
        return card;
    }

    private void setupCardLayout() {
        ConstraintSet set = new ConstraintSet();
        set.clone(rootLayout);
        applyArcConstraints(set, handCards);
        set.applyTo(rootLayout);
    }

    private void applyArcConstraints(ConstraintSet set, List<CardView> cardList) {
        int n = cardList.size();
        if (n == 0) return;

        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            CardView card = cardList.get(i);
            int id = card.getId();

            set.connect(id, ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
            set.connect(id, ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);
            set.connect(id, ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 0);
            set.connect(id, ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM, 0);

            float angle = startAngle + i * angleStep;
            double rad = Math.toRadians(angle);
            float xBias = (float) (CENTER_X_BIAS + RADIUS_BIAS * Math.sin(rad));
            float yBias = (float) (CENTER_Y_BIAS + RADIUS_BIAS * (1 - Math.cos(rad)));

            xBias = Math.max(0f, Math.min(1f, xBias));
            yBias = Math.max(0f, Math.min(1f, yBias));

            set.setHorizontalBias(id, xBias);
            set.setVerticalBias(id, yBias);
        }
    }

    private void animateCardsIntroduction() {
        int n = handCards.size();
        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            final CardView card = handCards.get(i);
            final float targetAngle = startAngle + i * angleStep;
            final int index = i;

            card.post(() -> {
                card.setPivotX(card.getWidth() / 2f);
                card.setPivotY(card.getHeight());
                card.setRotation(0f);

                card.animate()
                        .rotation(targetAngle)
                        .setStartDelay(index * 80L)
                        .setDuration(350)
                        .start();
            });
        }
    }

    private String getSuitSymbol(String suit) {
        switch (suit.toLowerCase()) {
            case "h": return "♥";
            case "d": return "♦";
            case "c": return "♣";
            case "s": return "♠";
            default: return "?";
        }
    }

    // -------------------------------------------------------------------
    // Data Classes
    // -------------------------------------------------------------------

    private static class GameState {
        String phase = "BIDDING";
        String currentPlayer;
        String dealer;
        String trump;
        CardState kittyCard;
        List<TeamState> teams = new ArrayList<>();
        List<PlayerState> players = new ArrayList<>();
        List<PlayedCard> currentTrick = new ArrayList<>();
    }

    private static class TeamState {
        int score;
        int tricksWon;
        List<String> members = new ArrayList<>();
    }

    private static class PlayerState {
        String username;
        List<CardState> hand = new ArrayList<>();
        int trickCount = 0;
    }

    private static class CardState {
        String value;
        String suit;
    }

    private static class PlayedCard {
        String player;
        CardState card;
    }
}