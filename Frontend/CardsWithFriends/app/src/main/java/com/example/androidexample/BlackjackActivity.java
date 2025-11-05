package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BlackjackActivity extends AppCompatActivity implements WebSocketListener {

    private String joinCode;
    private String username;
    private boolean isHost;
    private String gameType;

    private LinearLayout actionButtons;

    // Global game data
    private String selectedPlayer;
    private GameState gameState = new GameState();

    // UI
    private PlayerTurnBar playerTurnBar;
    private LinearLayout playerListLayout;
    private TextView balanceText;
    private EditText betInput;
    private Button betButton;

    private LinearLayout dealerCardContainer;

    private LinearLayout cardContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blackjack);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");
        isHost = intent.getBooleanExtra("HOST", false);

        selectedPlayer = username;

        playerListLayout = findViewById(R.id.playerList);
        playerTurnBar = new PlayerTurnBar(playerListLayout, this);
        balanceText = findViewById(R.id.balanceText);
        betInput = findViewById(R.id.betInput);
        betButton = findViewById(R.id.betButton);
        cardContainer = findViewById(R.id.cardContainer);
        actionButtons = findViewById(R.id.actionButtons);
        betButton.setOnClickListener(v -> sendBet());
        dealerCardContainer = findViewById(R.id.dealerCardContainer);

        findViewById(R.id.btnHit).setOnClickListener(v -> sendAction("HIT"));
        findViewById(R.id.btnStand).setOnClickListener(v -> sendAction("STAND"));
        findViewById(R.id.btnSplit).setOnClickListener(v -> sendAction("SPLIT"));
        findViewById(R.id.btnDouble).setOnClickListener(v -> sendAction("DOUBLE"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/blackjack/" + joinCode;
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
        if (isHost) {
            String startMsg = "{ \"action\": \"START\", \"player\": \"" + username + "\" }";
            String startRoundMsg = "{ \"action\": \"STARTROUND\", \"player\": \"" + username + "\" }";
            WebSocketManager.getInstance().sendMessage(startMsg);
            WebSocketManager.getInstance().sendMessage(startRoundMsg);
        }
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                gameState = parseGameState(json);
                playerTurnBar.updateFromGameState(gameState);
                updateBalanceUI();
                updateCardUI();
                updateActionButtonsUI();
                updateDealerCardsUI();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.e("WebSocket",
                "Closed → code=" + code +
                        ", reason=" + (reason == null ? "null" : reason) +
                        ", remote=" + remote);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e("WebSocket", "Error → " + ex.getMessage(), ex);
    }

    private void sendAction(String action) {
        if (!username.equals(gameState.currentTurn)) {
            Log.w("WebSocket", "Not your turn, ignoring " + action);
            return;
        }

        String msg = "{ \"action\": \"" + action + "\", \"player\": \"" + username + "\" }";
        Log.d("WebSocket", msg);
        WebSocketManager.getInstance().sendMessage(msg);
    }

    // -------------------------------------------------------------------
    // Handle Bets
    // -------------------------------------------------------------------
    private void sendBet() {
        if (!selectedPlayer.equals(username)) {
            betInput.setError("You can only bet as yourself!");
            return;
        }

        String betStr = betInput.getText().toString().trim();
        if (betStr.isEmpty()) {
            betInput.setError("Enter an amount");
            return;
        }

        int amount = Integer.parseInt(betStr);

        PlayerState self = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(username)) {
                self = p;
                break;
            }
        }

        if (self == null) {
            betInput.setError("Player not found.");
            return;
        }

        if (amount <= 0) {
            betInput.setError("Bet must be greater than 0");
            return;
        } else if (amount > self.chips) {
            betInput.setError("Not enough balance!");
            return;
        }

        // Send bet to server
        String betMsg = "{ \"action\": \"BET\", \"player\": \"" + username + "\", \"value\": " + amount + " }";
        Log.d("WebSocket", betMsg);
        WebSocketManager.getInstance().sendMessage(betMsg);

        betInput.setText("");
        betInput.setVisibility(View.GONE);
        betButton.setVisibility(View.GONE);
    }

    private void updateBalanceUI() {
        PlayerState viewedPlayer = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(selectedPlayer)) {
                viewedPlayer = p;
                break;
            }
        }

        if (viewedPlayer != null) {
            String label = viewedPlayer.username.equals(username)
                    ? "Your Balance"
                    : viewedPlayer.username + "'s Balance";
            balanceText.setText(label + ": $" + viewedPlayer.chips);
        } else {
            balanceText.setText("Balance: --");
        }

        // Only show bet input/button when it's your view and total bet == 0
        boolean isSelf = selectedPlayer.equals(username);
        int totalBet = 0;

        if (isSelf) {
            for (PlayerState p : gameState.players) {
                if (p.username.equals(username)) {
                    for (HandState h : p.hands) {
                        totalBet += h.bet;
                    }
                    break;
                }
            }
        }

        boolean canShowBetUI = isSelf && totalBet == 0;
        betInput.setVisibility(canShowBetUI ? View.VISIBLE : View.GONE);
        betButton.setVisibility(canShowBetUI ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------------------
    // JSON → GameState parsing
    // -------------------------------------------------------------------
    private GameState parseGameState(JSONObject json) throws JSONException {
        GameState state = new GameState();
        state.lobbyCode = json.optString("lobbyCode");
        state.roundInProgress = json.optBoolean("roundInProgress");
        state.currentTurn = json.optString("currentTurn");

        // Dealer
        JSONObject dealerObj = json.optJSONObject("dealer");
        if (dealerObj != null) {
            DealerState dealer = new DealerState();
            dealer.handValue = dealerObj.optInt("handValue");

            JSONArray cards = dealerObj.optJSONArray("hand");
            if (cards != null) {
                for (int i = 0; i < cards.length(); i++) {
                    JSONObject c = cards.getJSONObject(i);
                    CardState card = new CardState();
                    card.value = c.optInt("value");
                    card.suit = c.optString("suit");
                    card.isShowing = c.optBoolean("isShowing");
                    dealer.hand.add(card);
                }
            }
            state.dealer = dealer;
        }

        // Players
        JSONArray playersArray = json.optJSONArray("players");
        if (playersArray != null) {
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject p = playersArray.getJSONObject(i);
                PlayerState ps = new PlayerState();
                ps.username = p.optString("username");
                ps.chips = p.optInt("chips");
                ps.hasBet = p.optBoolean("hasBet");

                JSONArray hands = p.optJSONArray("hands");
                if (hands != null) {
                    for (int j = 0; j < hands.length(); j++) {
                        JSONObject h = hands.getJSONObject(j);
                        HandState hs = new HandState();
                        hs.handIndex = h.optInt("handIndex");
                        hs.handValue = h.optInt("handValue");
                        hs.bet = h.optInt("bet");
                        hs.hasStood = h.optBoolean("hasStood");

                        JSONArray cards = h.optJSONArray("hand");
                        if (cards != null) {
                            for (int k = 0; k < cards.length(); k++) {
                                JSONObject c = cards.getJSONObject(k);
                                CardState card = new CardState();
                                card.value = c.optInt("value");
                                card.suit = c.optString("suit");
                                card.isShowing = c.optBoolean("isShowing");
                                hs.hand.add(card);
                            }
                        }
                        ps.hands.add(hs);
                    }
                }
                state.players.add(ps);
            }
        }
        return state;
    }

    // -------------------------------------------------------------------
    // Player Turn Bar UI
    // -------------------------------------------------------------------
    private class PlayerTurnBar {
        private final LinearLayout container;
        private final AppCompatActivity activity;

        public PlayerTurnBar(LinearLayout container, AppCompatActivity activity) {
            this.container = container;
            this.activity = activity;
        }

        public void updateFromGameState(GameState state) {
            container.removeAllViews();

            for (PlayerState player : state.players) {
                LinearLayout playerContainer = new LinearLayout(activity);
                playerContainer.setOrientation(LinearLayout.VERTICAL);
                playerContainer.setGravity(Gravity.CENTER_HORIZONTAL);

                TextView playerBox = new TextView(activity);
                playerBox.setText(player.username);
                playerBox.setTextColor(Color.WHITE);
                playerBox.setTextSize(22);
                playerBox.setTypeface(ResourcesCompat.getFont(activity, R.font.inter_bold));
                playerBox.setPadding(48, 28, 48, 28);

                float radius = activity.getResources().getDisplayMetrics().density * 20;
                int bgColor = player.username.equals(selectedPlayer)
                        ? ContextCompat.getColor(activity, R.color.my_green)
                        : ContextCompat.getColor(activity, R.color.my_grey);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(radius);
                bg.setColor(bgColor);
                playerBox.setBackground(bg);

                View turnBar = new View(activity);
                int barHeight = (int) (4 * activity.getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, barHeight
                );
                barParams.setMargins(0, 6, 0, 0);
                turnBar.setLayoutParams(barParams);
                turnBar.setBackgroundColor(
                        player.username.equals(state.currentTurn)
                                ? ContextCompat.getColor(activity, R.color.my_green)
                                : Color.TRANSPARENT
                );

                playerBox.setOnClickListener(v -> {
                    selectedPlayer = player.username;
                    updateFromGameState(state);
                    updateBalanceUI();
                    updateCardUI();
                    updateDealerCardsUI();
                    updateActionButtonsUI();
                });

                LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                outerParams.setMargins(24, 0, 24, 0);
                playerContainer.setLayoutParams(outerParams);

                playerContainer.addView(playerBox);
                playerContainer.addView(turnBar);
                container.addView(playerContainer);
            }
        }
    }
    private void updateCardUI() {
        cardContainer.removeAllViews();

        // Find selected player
        PlayerState player = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(selectedPlayer)) {
                player = p;
                break;
            }
        }
        if (player == null) return;

        // Outer container: vertical stack (one row per hand)
        cardContainer.setOrientation(LinearLayout.VERTICAL);

        for (HandState hand : player.hands) {
            // Row for this hand
            LinearLayout handRow = new LinearLayout(this);
            handRow.setOrientation(LinearLayout.HORIZONTAL);
            handRow.setGravity(Gravity.CENTER_HORIZONTAL);
            handRow.setPadding(0, 12, 0, 12);

            for (int i = 0; i < hand.hand.size(); i++) {
                CardState card = hand.hand.get(i);

                com.example.androidexample.services.CardView cardView =
                        new com.example.androidexample.services.CardView(this);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) (70 * getResources().getDisplayMetrics().density),
                        (int) (105 * getResources().getDisplayMetrics().density)
                );

                // Slight overlap between cards
                if (i > 0) params.setMargins(-30, 0, 0, 0);
                cardView.setLayoutParams(params);
                cardView.setCard(
                        String.valueOf(card.value),
                        card.suit,
                        card.isShowing
                );
                handRow.addView(cardView);
            }

            // Add this hand row to main card container
            cardContainer.addView(handRow);
        }
    }


    private void updateDealerCardsUI() {
        dealerCardContainer.removeAllViews();
        if (gameState.dealer == null || gameState.dealer.hand.isEmpty()) return;

        for (CardState card : gameState.dealer.hand) {
            com.example.androidexample.services.CardView cardView =
                    new com.example.androidexample.services.CardView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (80 * getResources().getDisplayMetrics().density),
                    (int) (120 * getResources().getDisplayMetrics().density)
            );
            params.setMargins(6, 0, 6, 0);
            cardView.setLayoutParams(params);
            cardView.setCard(
                    String.valueOf(card.value),
                    card.suit,
                    card.isShowing
            );
            dealerCardContainer.addView(cardView);
        }
    }
    private void updateActionButtonsUI() {
        boolean showButtons = false;

        // Find the selected player
        PlayerState selected = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(selectedPlayer)) {
                selected = p;
                break;
            }
        }

        if (selected != null) {
            boolean selectedHasCards = !selected.hands.isEmpty()
                    && !selected.hands.get(0).hand.isEmpty()
                    && selected.hands.get(0).hand.get(0).isShowing;

            boolean isTheirTurn = gameState.currentTurn.equals(selected.username);
            boolean isMyTurn = username.equals(selected.username);

            showButtons = selectedHasCards && isTheirTurn && isMyTurn;
        }

        actionButtons.setVisibility(showButtons ? View.VISIBLE : View.GONE);
    }

    // -------------------------------------------------------------------
    // Data Models
    // -------------------------------------------------------------------
    private static class GameState {
        String lobbyCode;
        boolean roundInProgress;
        String currentTurn;
        DealerState dealer;
        List<PlayerState> players = new ArrayList<>();
    }

    private static class PlayerState {
        String username;
        int chips;
        boolean hasBet;
        List<HandState> hands = new ArrayList<>();
    }

    private static class DealerState {
        int handValue;
        List<CardState> hand = new ArrayList<>();
    }

    private static class HandState {
        int handIndex;
        int handValue;
        int bet;
        boolean hasStood;
        List<CardState> hand = new ArrayList<>();
    }

    private static class CardState {
        int value;
        String suit;
        boolean isShowing;
    }
}
