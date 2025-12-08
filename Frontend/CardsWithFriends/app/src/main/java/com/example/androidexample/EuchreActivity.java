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

    private CardView selectedCard = null;
    private ConstraintLayout rootLayout;
    private List<CardView> handCards;
    private List<CardView> trickCards;
    private GameState gameState = new GameState();
    private boolean gameStarted = false;

    // UI Elements
    private LinearLayout teamScoreLayout;
    private LinearLayout playerInfoLayout;
    private Button passButton;
    private Button pickUpButton;
    private Button orderUpButton;
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
    }

    private void setupListeners() {
        passButton.setOnClickListener(v -> handlePassButton());
        pickUpButton.setOnClickListener(v -> handlePickUpButton());
        orderUpButton.setOnClickListener(v -> handleOrderUpButton());
        startGameButton.setOnClickListener(v -> startGame());
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

            // Handle empty or null messages
            if (message == null || message.trim().isEmpty()) {
                Log.w(TAG, "Received null or empty message");
                return;
            }

            if (!message.trim().startsWith("{")) {
                Log.d(TAG, "Received plain text message: " + message);

                // Don't show error messages as toasts
                if (!message.toLowerCase().contains("no game running") &&
                        !message.toLowerCase().contains("error") &&
                        !message.toLowerCase().contains("null")) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }

                // Request updated state after game events
                if (message.toLowerCase().contains("started") ||
                        message.toLowerCase().contains("new game") ||
                        message.toLowerCase().contains("passed") ||
                        message.toLowerCase().contains("picked up") ||
                        message.toLowerCase().contains("pick up") ||
                        message.toLowerCase().contains("chose") ||
                        message.toLowerCase().contains("trump") ||
                        message.toLowerCase().contains("played") ||
                        message.toLowerCase().contains("trick")) {

                    Log.d(TAG, "Game event detected, requesting state update in 500ms...");
                    new Handler().postDelayed(() -> {
                        Log.d(TAG, "Executing delayed state request...");
                        requestGameState();
                    }, 500);
                }
                return;
            }

            try {
                JSONObject json = new JSONObject(message);
                Log.d(TAG, "Parsing JSON game state...");
                gameState = parseGameState(json);
                gameStarted = true;

                startGameButton.setVisibility(View.GONE);
                gameStatusText.setVisibility(View.GONE);

                Log.d(TAG, "=== GAME STATE PARSED ===");
                Log.d(TAG, "Phase: " + gameState.phase);
                Log.d(TAG, "Current Player: " + gameState.currentPlayer);
                Log.d(TAG, "Dealer: " + gameState.dealer);
                Log.d(TAG, "Trump: " + gameState.trump);
                Log.d(TAG, "Players count: " + gameState.players.size());
                Log.d(TAG, "Current trick size: " + gameState.currentTrick.size());
                Log.d(TAG, "========================");

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
    // WebSocket Message Sending
    // -------------------------------------------------------------------

    private void startGame() {
        Log.d(TAG, "Start button clicked!");

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
            Toast.makeText(this, "Error starting game", Toast.LENGTH_SHORT).show();
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

    private void handlePassButton() {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "pass");
            message.put("player", username);

            Log.d(TAG, "Sending pass: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            // Request state update after a delay
            new Handler().postDelayed(() -> {
                Log.d(TAG, "Requesting state after pass...");
                requestGameState();
            }, 500);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating pass message", e);
        }
    }

    private void handleOrderUpButton() {
        // In standard Euchre, "ordering up" means:
        // - Non-dealer tells dealer to pick up the kitty card
        // - The kitty card's suit becomes trump

        // Since backend doesn't have a separate "orderUp" action,
        // we send "chooseSuit" with the kitty card's suit

        if (gameState.kittyCard != null) {
            try {
                JSONObject message = new JSONObject();
                message.put("type", "chooseSuit");
                message.put("player", username);
                // Use the kitty card's suit as the trump
                message.put("suit", gameState.kittyCard.suit);

                Log.d(TAG, "Sending choose suit (order up): " + message.toString());
                WebSocketManager.getInstance().sendMessage(message.toString());

                Toast.makeText(this, "Ordering up " + getSuitSymbol(gameState.kittyCard.suit), Toast.LENGTH_SHORT).show();

                // Request state update after a delay
                new Handler().postDelayed(() -> {
                    Log.d(TAG, "Requesting state after order up...");
                    requestGameState();
                }, 500);

            } catch (JSONException e) {
                Log.e(TAG, "Error creating order up message", e);
            }
        } else {
            Toast.makeText(this, "No kitty card available", Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePickUpButton() {
        if (selectedCard == null) {
            Toast.makeText(this, "Select a card to discard", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject message = new JSONObject();
            message.put("type", "pickUp");
            message.put("dealer", username);

            JSONObject droppedCard = new JSONObject();
            droppedCard.put("value", convertStringToValue(selectedCard.getCardValue()));
            droppedCard.put("suit", selectedCard.getCardSuit());
            message.put("droppedCard", droppedCard);

            Log.d(TAG, "Sending pick up: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            selectedCard.setSelected(false);
            selectedCard = null;

            // Request state update after a delay to ensure backend has processed
            new Handler().postDelayed(() -> {
                Log.d(TAG, "Requesting state after pickup...");
                requestGameState();
            }, 800);

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

            // Request state update after a delay
            new Handler().postDelayed(() -> {
                Log.d(TAG, "Requesting state after choose suit...");
                requestGameState();
            }, 500);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating choose suit message", e);
        }
    }

    private void playCard(CardView card) {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "play");
            message.put("player", username);

            JSONObject cardObj = new JSONObject();
            cardObj.put("value", convertStringToValue(card.getCardValue()));
            cardObj.put("suit", card.getCardSuit());
            message.put("card", cardObj);

            Log.d(TAG, "Sending play card: " + message.toString());
            WebSocketManager.getInstance().sendMessage(message.toString());

            if (selectedCard != null) {
                selectedCard.setSelected(false);
            }
            selectedCard = null;

            // Request state update after a delay
            new Handler().postDelayed(() -> {
                Log.d(TAG, "Requesting state after playing card...");
                requestGameState();
            }, 500);

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

        // Parse phase - backend uses "isBidding" boolean, not a "phase" string
        // We need to determine phase from the isBidding field
        if (gameData.has("isBidding")) {
            boolean isBidding = gameData.getBoolean("isBidding");
            state.phase = isBidding ? "BIDDING" : "PLAYING";
            Log.d(TAG, "Parsed isBidding: " + isBidding + " -> phase: " + state.phase);
        } else if (gameData.has("phase")) {
            // Fallback in case backend sends phase string
            state.phase = gameData.getString("phase");
            Log.d(TAG, "Parsed phase from JSON: " + state.phase);
        } else {
            // Default to BIDDING if nothing found
            state.phase = "BIDDING";
            Log.d(TAG, "No phase info found, defaulting to BIDDING");
        }

        // Parse current player - try multiple field names
        if (gameData.has("currentPlayerUsername")) {
            state.currentPlayer = gameData.getString("currentPlayerUsername");
        } else if (gameData.has("currentPlayer")) {
            state.currentPlayer = gameData.getString("currentPlayer");
        } else {
            state.currentPlayer = "";
        }
        Log.d(TAG, "Parsed current player: " + state.currentPlayer);

        // Parse dealer - try multiple field names
        if (gameData.has("currentDealerUsername")) {
            state.dealer = gameData.getString("currentDealerUsername");
        } else if (gameData.has("dealer")) {
            state.dealer = gameData.getString("dealer");
        } else {
            state.dealer = "";
        }
        Log.d(TAG, "Parsed dealer: " + state.dealer);

        // Parse trump
        if (gameData.has("trumpSuit")) {
            // Backend sends single char, convert to string
            String trumpStr = gameData.getString("trumpSuit");
            state.trump = trumpStr.isEmpty() ? "" : String.valueOf(trumpStr.charAt(0));
            Log.d(TAG, "Parsed trump from trumpSuit: " + state.trump);
        } else if (gameData.has("trump")) {
            state.trump = gameData.getString("trump");
            Log.d(TAG, "Parsed trump: " + state.trump);
        } else {
            state.trump = "";
        }

        // Parse option card (the face-up card used for trump selection)
        if (gameData.has("optionCard") && !gameData.isNull("optionCard")) {
            JSONObject option = gameData.getJSONObject("optionCard");
            state.kittyCard = new CardState();
            state.kittyCard.value = convertValueToString(option.optInt("value"));
            state.kittyCard.suit = String.valueOf(option.optString("suit").charAt(0));
            Log.d(TAG, "Parsed kitty card: " + state.kittyCard.value + state.kittyCard.suit);
        } else {
            Log.d(TAG, "No kitty card in response (normal for PLAYING phase)");
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
            Log.d(TAG, "Parsed " + state.teams.size() + " teams");
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
                        // Handle suit as either string or char
                        String suitStr = c.optString("suit");
                        card.suit = suitStr.isEmpty() ? "" : String.valueOf(suitStr.charAt(0));
                        ps.hand.add(card);
                    }
                }

                ps.trickCount = p.optInt("trickCount", 0);
                state.players.add(ps);

                Log.d(TAG, "Player " + ps.username + " has " + ps.hand.size() + " cards, " + ps.trickCount + " tricks");
            }

            // If backend didn't provide teams, construct them manually
            if (state.teams.isEmpty() && state.players.size() == 4) {
                TeamState team1 = new TeamState();
                team1.members.add(state.players.get(0).username);
                team1.members.add(state.players.get(2).username);
                team1.score = 0;
                team1.tricksWon = state.players.get(0).trickCount + state.players.get(2).trickCount;

                TeamState team2 = new TeamState();
                team2.members.add(state.players.get(1).username);
                team2.members.add(state.players.get(3).username);
                team2.score = 0;
                team2.tricksWon = state.players.get(1).trickCount + state.players.get(3).trickCount;

                state.teams.add(team1);
                state.teams.add(team2);

                Log.d(TAG, "Constructed teams from players");
            }
        }

        // Parse current trick
        JSONArray trickArray = gameData.optJSONArray("currentTrick");
        if (trickArray != null) {
            for (int i = 0; i < trickArray.length(); i++) {
                JSONObject playedCardObj = trickArray.getJSONObject(i);
                PlayedCard pc = new PlayedCard();

                // The backend might send the card directly or in a nested structure
                if (playedCardObj.has("player")) {
                    pc.player = playedCardObj.getString("player");
                }

                // Try to get card from different possible locations
                JSONObject cardObj = null;
                if (playedCardObj.has("card")) {
                    cardObj = playedCardObj.getJSONObject("card");
                } else if (playedCardObj.has("value") && playedCardObj.has("suit")) {
                    // Card data is directly in the object
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
            Log.d(TAG, "Parsed current trick with " + state.currentTrick.size() + " cards");
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

            Log.d(TAG, "Updating team scores...");
            updateTeamScores();

            Log.d(TAG, "Updating player info...");
            updatePlayerInfo();

            Log.d(TAG, "Updating trump display...");
            updateTrumpDisplay();

            Log.d(TAG, "Updating hand cards...");
            updateHandCards();

            Log.d(TAG, "Updating trick display...");
            updateTrickDisplay();

            Log.d(TAG, "Updating action buttons...");
            updateActionButtons();

            Log.d(TAG, "UI updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
            Toast.makeText(this, "UI update error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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
        Log.d(TAG, "updateHandCards called");

        for (CardView card : handCards) {
            rootLayout.removeView(card);
        }
        handCards.clear();

        PlayerState currentPlayer = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(username)) {
                currentPlayer = p;
                Log.d(TAG, "Found current player: " + username + " with " + p.hand.size() + " cards");
                break;
            }
        }

        if (currentPlayer == null) {
            Log.w(TAG, "Current player not found in game state!");
            return;
        }

        if (currentPlayer.hand.isEmpty()) {
            Log.w(TAG, "Current player has no cards!");
            return;
        }

        for (CardState cardData : currentPlayer.hand) {
            Log.d(TAG, "Creating card: " + cardData.value + cardData.suit);
            CardView card = createCardFromData(cardData.value, cardData.suit);

            if (gameState.phase.equals("PLAYING") && username.equals(gameState.currentPlayer)) {
                card.setClickable(true);
                card.setFocusable(true);
                card.setOnClickListener(v -> playCard(card));
            } else if (gameState.phase.equals("BIDDING") && username.equals(gameState.dealer)) {
                card.setClickable(true);
                card.setFocusable(true);
                card.setOnClickListener(v -> {
                    if (selectedCard != null) {
                        selectedCard.setSelected(false);
                    }
                    selectedCard = card;
                    card.setSelected(true);
                });
            }

            rootLayout.addView(card);
            handCards.add(card);
        }

        Log.d(TAG, "Added " + handCards.size() + " cards to layout");

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

            set.connect(card.getId(), ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
            set.connect(card.getId(), ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);
            set.connect(card.getId(), ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 0);
            set.connect(card.getId(), ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM, 0);

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
        Log.d(TAG, "My username: " + username);
        Log.d(TAG, "Current player: " + gameState.currentPlayer);
        Log.d(TAG, "Is my turn: " + isMyTurn);
        Log.d(TAG, "Dealer: " + gameState.dealer);
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
            Log.d(TAG, "PLAYING phase - cards should be clickable if it's your turn");
            if (isMyTurn) {
                Log.d(TAG, "IT'S YOUR TURN TO PLAY A CARD!");
                // Visual feedback that it's the player's turn is already shown in player list
            }
        } else {
            Log.d(TAG, "Not showing buttons - wrong phase or not your turn");
        }
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