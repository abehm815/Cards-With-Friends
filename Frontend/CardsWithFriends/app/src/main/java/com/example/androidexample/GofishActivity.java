package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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

public class GofishActivity extends AppCompatActivity implements WebSocketListener {

    private static final String TAG = "GofishActivity";

    private static final float CENTER_X_BIAS = 0.5f;
    private static final float CENTER_Y_BIAS = 0.75f;
    private static final float RADIUS_BIAS = 1.4f;
    private static final float TOTAL_SPREAD = 30f;

    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 200;

    private String joinCode;
    private String username;
    private boolean isHost;
    private String gameType;

    private CardView selectedCard = null;
    private ConstraintLayout rootLayout;
    private List<CardView> cards;
    private GameState gameState = new GameState();

    // UI Elements
    private LinearLayout playerListLayout;
    private Button askButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gofish);

        //Intent intent = getIntent();
        //gameType = intent.getStringExtra("GAMETYPE");
        //username = intent.getStringExtra("USERNAME");
        //joinCode = intent.getStringExtra("JOINCODE");
        //isHost = intent.getBooleanExtra("HOST", false);
        gameType = "gofish";
        username = "bigDon";
        joinCode = "1867";
        isHost = true;

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.rootLayout);
        playerListLayout = findViewById(R.id.playerList);
        //askButton = findViewById(R.id.askButton);
        cards = new ArrayList<>();
    }

    private void setupListeners() {
        askButton.setOnClickListener(v -> handleAskButtonClick());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/gofish/" + joinCode;
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

            // Just request the current state - don't auto-start
            // The host can start the game manually via a button later
            requestGameState();
        });
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            Log.d(TAG, "Received: " + message);

            try {
                JSONObject json = new JSONObject(message);
                gameState = parseGameState(json);

                updateUIFromState();

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing message", e);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.e(TAG, "Closed → code=" + code +
                ", reason=" + (reason == null ? "null" : reason) +
                ", remote=" + remote);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "Error → " + ex.getMessage(), ex);
    }

    // -------------------------------------------------------------------
    // WebSocket Message Sending
    // -------------------------------------------------------------------

    private void startGame() {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "start");

            // Get player list from gameState or intent
            JSONArray playerArray = new JSONArray();
            playerArray.put(username);
            // Add other players if available

            message.put("players", playerArray);

            String msg = message.toString();
            Log.d(TAG, "Starting game: " + msg);
            WebSocketManager.getInstance().sendMessage(msg);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating start message", e);
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

    private void handleAskButtonClick() {
        if (selectedCard == null) {
            Toast.makeText(this, "Please select a card first", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Show dialog to select target player
        // For now, just use a placeholder
        String targetPlayer = "opponent"; // Replace with actual player selection

        sendTurn(username, targetPlayer, selectedCard.getCardValue());
    }

    private void sendTurn(String askingPlayer, String targetPlayer, String cardValue) {
        try {
            JSONObject message = new JSONObject();
            message.put("type", "turn");
            message.put("askingPlayer", askingPlayer);
            message.put("targetPlayer", targetPlayer);

            // Convert card value to appropriate format
            if (cardValue.matches("\\d+")) {
                message.put("value", Integer.parseInt(cardValue));
            } else {
                message.put("value", cardValue);
            }

            String msg = message.toString();
            Log.d(TAG, "Sending turn: " + msg);
            WebSocketManager.getInstance().sendMessage(msg);

            selectedCard = null; // Clear selection after asking
        } catch (JSONException e) {
            Log.e(TAG, "Error creating turn message", e);
        }
    }

    // -------------------------------------------------------------------
    // JSON → GameState Parsing
    // -------------------------------------------------------------------

    private GameState parseGameState(JSONObject json) throws JSONException {
        GameState state = new GameState();
        state.lobbyCode = json.optString("lobbyCode");
        state.currentTurn = json.optString("currentTurn");

        // Parse players
        JSONArray playersArray = json.optJSONArray("players");
        if (playersArray != null) {
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject p = playersArray.getJSONObject(i);
                PlayerState ps = new PlayerState();
                ps.username = p.optString("username");
                ps.handSize = p.optInt("handSize");
                ps.books = p.optInt("books");

                // Parse hand if it's the current player
                JSONArray hand = p.optJSONArray("hand");
                if (hand != null) {
                    for (int j = 0; j < hand.length(); j++) {
                        JSONObject c = hand.getJSONObject(j);
                        CardState card = new CardState();
                        card.value = c.optString("value");
                        card.suit = c.optString("suit");
                        ps.hand.add(card);
                    }
                }

                state.players.add(ps);
            }
        }

        return state;
    }

    // -------------------------------------------------------------------
    // UI Update Methods
    // -------------------------------------------------------------------

    private void updateUIFromState() {
        updatePlayerList();
        updateCardsUI();
        updateAskButton();
    }

    private void updatePlayerList() {
        playerListLayout.removeAllViews();

        for (PlayerState player : gameState.players) {
            // Create player item view
            // TODO: Implement player list item similar to BlackjackActivity's PlayerTurnBar
            // Show username, book count, hand size
        }
    }

    private void updateCardsUI() {
        // Clear existing cards
        for (CardView card : cards) {
            rootLayout.removeView(card);
        }
        cards.clear();

        // Find current player's hand
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

        // Create card views from hand data
        for (CardState cardData : currentPlayer.hand) {
            CardView card = createCardFromData(cardData.value, cardData.suit);
            rootLayout.addView(card);
            cards.add(card);
        }

        // Layout cards in arc
        setupCardLayout();
        animateCardsIntroduction();
    }

    private CardView createCardFromData(String value, String suit) {
        CardView card = new CardView(this);
        card.setId(View.generateViewId());
        card.setLayoutParams(new ConstraintLayout.LayoutParams(CARD_WIDTH, CARD_HEIGHT));
        card.setClickable(true);
        card.setFocusable(true);
        card.setCard(value, suit, true);

        // Store value for later reference
        card.setTag(value);

        // Add click listener for card selection
        card.setOnClickListener(v -> {
            if (selectedCard != null) {
                selectedCard.setSelected(false);
            }
            selectedCard = card;
            card.setSelected(true);
            Toast.makeText(this, "Selected: " + value + suit, Toast.LENGTH_SHORT).show();
        });

        return card;
    }

    private void updateAskButton() {
        // Enable ask button only if it's the player's turn
        boolean isMyTurn = username.equals(gameState.currentTurn);
        askButton.setEnabled(isMyTurn);
        askButton.setVisibility(isMyTurn ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------------------
    // Card Layout Methods
    // -------------------------------------------------------------------

    private void setupCardLayout() {
        ConstraintSet set = new ConstraintSet();
        set.clone(rootLayout);
        applyArcConstraints(set, cards);
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
        int n = cards.size();
        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            final CardView card = cards.get(i);
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

    private void relayoutHand(List<CardView> cardList, ConstraintLayout root) {
        int n = cardList.size();
        if (n == 0) return;

        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        applyArcConstraints(set, cardList);

        androidx.transition.TransitionManager.beginDelayedTransition(root);
        set.applyTo(root);

        animateCardRotations(cardList);
    }

    private void animateCardRotations(List<CardView> cardList) {
        int n = cardList.size();
        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            final CardView card = cardList.get(i);
            final float targetAngle = startAngle + i * angleStep;
            final int index = i;

            card.post(() -> {
                card.setPivotX(card.getWidth() / 2f);
                card.setPivotY(card.getHeight());
                card.animate()
                        .rotation(targetAngle)
                        .setStartDelay(index * 60L)
                        .setDuration(250)
                        .start();
            });
        }
    }

    // -------------------------------------------------------------------
    // Data Models
    // -------------------------------------------------------------------

    private static class GameState {
        String lobbyCode;
        String currentTurn;
        List<PlayerState> players = new ArrayList<>();
    }

    private static class PlayerState {
        String username;
        int handSize;
        int books;
        List<CardState> hand = new ArrayList<>();
    }

    private static class CardState {
        String value;
        String suit;
    }
}