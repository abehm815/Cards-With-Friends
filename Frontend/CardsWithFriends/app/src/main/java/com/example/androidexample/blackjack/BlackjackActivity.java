package com.example.androidexample.blackjack;

import com.example.androidexample.blackjack.BlackjackModels.*;
import android.view.Gravity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.R;
import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;
import com.google.android.material.button.MaterialButton;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * BlackjackActivity
 * Handles all blackjack game UI updates and WebSocket communications.
 * Includes player turn indicators, card rendering, betting logic, and dealer display.
 */
public class BlackjackActivity extends AppCompatActivity implements WebSocketListener {

    // Game session variables
    private String joinCode;
    private String username;
    private boolean isHost;
    private String gameType;

    // Layout references
    private LinearLayout actionButtons;
    private PlayerTurnBar playerTurnBar;
    private LinearLayout playerListLayout;
    private LinearLayout dealerCardContainer;
    private LinearLayout cardContainer;
    private TextView balanceText;
    private EditText betInput;
    private ImageButton betButton;
    private MaterialButton startNewRoundBtn;

    // Game data
    private String selectedPlayer;
    private GameState gameState = new GameState();

    private GameState previousGameState = null;

    // Helpers
    private CardRenderer cardRenderer;
    private BetHandler betHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blackjack);

        // Retrieve intent data
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");
        isHost = intent.getBooleanExtra("HOST", false);

        // Initialize views
        startNewRoundBtn = findViewById(R.id.start_new_round_btn);
        startNewRoundBtn.setVisibility(View.GONE);

        playerListLayout = findViewById(R.id.playerList);
        playerTurnBar = new PlayerTurnBar(playerListLayout, this);
        balanceText = findViewById(R.id.balanceText);
        betInput = findViewById(R.id.betInput);
        betButton = findViewById(R.id.betButton);
        actionButtons = findViewById(R.id.actionButtons);
        cardContainer = findViewById(R.id.cardContainer);
        dealerCardContainer = findViewById(R.id.dealerCardContainer);

        selectedPlayer = username;


        // Initialize helpers
        cardRenderer = new CardRenderer(this, getResources().getDisplayMetrics().density);
        betHandler = new BetHandler(betInput, betButton, balanceText, username);

        // Action button listeners
        betButton.setOnClickListener(v -> betHandler.sendBet(gameState, selectedPlayer));
        findViewById(R.id.btnHit).setOnClickListener(v -> sendAction("HIT"));
        findViewById(R.id.btnStand).setOnClickListener(v -> sendAction("STAND"));
        findViewById(R.id.btnSplit).setOnClickListener(v -> sendAction("SPLIT"));
        findViewById(R.id.btnDouble).setOnClickListener(v -> sendAction("DOUBLE"));

        // Start new round (host only)
        startNewRoundBtn.setOnClickListener(v -> {
            String startRoundMsg = "{ \"action\": \"STARTROUND\", \"player\": \"" + username + "\" }";
            WebSocketManager.getInstance().sendMessage(startRoundMsg);
            Log.d("WebSocket", "STARTROUND MESSAGE SENT");
            startNewRoundBtn.animate()
                    .alpha(0f)
                    .translationY(100f)
                    .setDuration(400)
                    .withEndAction(() -> startNewRoundBtn.setVisibility(View.GONE))
                    .start();
        });
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

    // -------------------------------------------------------------------
    // WebSocket event handling
    // -------------------------------------------------------------------
    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> showCountdownToNextRound(3));

        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> runOnUiThread(() -> findViewById(R.id.bettingPanel).setVisibility(View.VISIBLE)), 4000);

        if (isHost) {
            new android.os.Handler(android.os.Looper.getMainLooper())
                    .postDelayed(() -> {
                        String startMsg = "{ \"action\": \"START\", \"player\": \"" + username + "\" }";
                        String startRoundMsg = "{ \"action\": \"STARTROUND\", \"player\": \"" + username + "\" }";
                        Log.d("WebSocket", "Host started new round after countdown");
                        WebSocketManager.getInstance().sendMessage(startMsg);
                        WebSocketManager.getInstance().sendMessage(startRoundMsg);
                    }, 4000);
        }
    }

    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                gameState = GameStateParser.fromJson(json);

                // -------------------------------------------------------------------
                // Logic to switch views to different players
                // -------------------------------------------------------------------

                // --- Do the switch after the delay ---
                long viewSwitchDelay = 1500;
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {

                    //TODO change so that backend sends a notification when the round is over (This is a wierd check)
                    boolean isRoundOver = gameState.currentTurn == null || gameState.currentTurn.equals("null");

                    // Find this client's player state
                    PlayerState self = null;
                    for (PlayerState p : gameState.players) {
                        if (p.username.equals(username)) {
                            self = p;
                            break;
                        }
                    }

                    //Determine if all the bets are placed
                    boolean allBetsPlaced = true;
                    for (PlayerState p : gameState.players) {
                        if (!p.hasBet) {
                            allBetsPlaced = false;
                            break;
                        }
                    }

                    // While bets are being placed focus the client on themselves
                    if (!allBetsPlaced) {
                        selectedPlayer = username;
                    }

                    // If the round is over, show your own hand again
                    else if (isRoundOver) {
                        selectedPlayer = username;
                    }

                    // Otherwise, switch to whoever’s turn it is
                    else {
                        boolean switched = false;
                        for (PlayerState p : gameState.players) {
                            if (p.username.equals(gameState.currentTurn)) {
                                selectedPlayer = p.username;
                                switched = true;
                                break;
                            }
                        }
                        if (!switched) selectedPlayer = username;
                    }

                    playerTurnBar.updateFromGameState(gameState, selectedPlayer);
                    updateBalanceUI();
                    updateCardUI();
                    updateDealerCardsUI(isRoundOver);
                    updateActionButtonsUI();

                }, viewSwitchDelay);

                //TODO change so that backend sends a notification when the round is over (This is a wierd check)
                boolean isRoundOver = gameState.currentTurn == null || gameState.currentTurn.equals("null");

                // Immediate refresh too
                playerTurnBar.updateFromGameState(gameState, selectedPlayer);
                updateBalanceUI();
                updateCardUI();
                updateDealerCardsUI(isRoundOver);
                updateActionButtonsUI();

                long newRoundDelay = 3000;
                long countDownDelay = 3500;
                if (isRoundOver) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> {
                                showCountdownToNextRound((int) (newRoundDelay / 1000));
                                // Animate cards out of view
                                cardRenderer.animateAllCardsOut(cardContainer);
                                cardRenderer.animateAllCardsOut(dealerCardContainer);
                                // Host automatically restarts
                                if (isHost) {
                                    new android.os.Handler(android.os.Looper.getMainLooper())
                                            .postDelayed(() -> {
                                                String startRoundMsg = "{ \"action\": \"STARTROUND\", \"player\": \"" + username + "\" }";
                                                WebSocketManager.getInstance().sendMessage(startRoundMsg);
                                                Log.d("WebSocket", "Auto STARTROUND message sent after countdown");
                                            }, newRoundDelay);
                                }
                            }, countDownDelay);
                }
            } catch (JSONException e) {
                Log.e("WebSocket", "Failed to parse message: " + message, e);
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.e("WebSocket", "Closed → code=" + code + ", reason=" + reason + ", remote=" + remote);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e("WebSocket", "Error → " + ex.getMessage(), ex);
    }

    // -------------------------------------------------------------------
    // Player actions and betting
    // -------------------------------------------------------------------
    private void sendAction(String action) {
        if (!username.equals(gameState.currentTurn)) {
            return;
        }
        String msg = "{ \"action\": \"" + action + "\", \"player\": \"" + username + "\" }";
        WebSocketManager.getInstance().sendMessage(msg);
    }

    private void updateBalanceUI() {
        betHandler.updateBalanceUI(gameState, selectedPlayer);
    }

    // -------------------------------------------------------------------
    // Card and dealer rendering (via CardRenderer)
    // -------------------------------------------------------------------
    private void updateCardUI() {
        PlayerState player = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(selectedPlayer)) {
                player = p;
                break;
            }
        }
        cardRenderer.renderPlayerHands(cardContainer, player);
    }

    private void updateDealerCardsUI(boolean isRoundOver) {
        cardRenderer.renderDealer(dealerCardContainer, gameState.dealer, isRoundOver);
    }

    // -------------------------------------------------------------------
    // Action buttons
    // -------------------------------------------------------------------
    private void updateActionButtonsUI() {
        boolean showButtons = false;

        PlayerState selected = null;
        for (PlayerState p : gameState.players) {
            if (p.username.equals(selectedPlayer)) {
                selected = p;
                break;
            }
        }

        if (selected != null) {
            boolean isMyTurn = username.equals(gameState.currentTurn);
            boolean isViewingSelf = selectedPlayer.equals(username);

            boolean hasCards = false;
            if (selected.hands != null && !selected.hands.isEmpty()) {
                for (HandState hand : selected.hands) {
                    if (hand.hand != null && !hand.hand.isEmpty()) {
                        hasCards = true;
                        break;
                    }
                }
            }

            showButtons = isMyTurn && isViewingSelf && hasCards;
        }

        actionButtons.setVisibility(showButtons ? View.VISIBLE : View.GONE);

        View splitBtn = findViewById(R.id.btnSplit);
        boolean canSplit = false;

        if (selected != null && selected.hands != null && !selected.hands.isEmpty()) {
            for (HandState hand : selected.hands) {
                if (hand.canSplit) {
                    canSplit = true;
                    break;
                }
            }
        }

        splitBtn.setVisibility(canSplit ? View.VISIBLE : View.GONE);
    }
    private void showCountdownToNextRound(int seconds) {
        TextView countdownText = new TextView(this);
        countdownText.setTextSize(72);
        countdownText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_black));
        countdownText.setGravity(Gravity.CENTER);
        countdownText.setTextColor(Color.WHITE);
        countdownText.setShadowLayer(25f, 0f, 0f, Color.BLACK);
        countdownText.setAlpha(0f);
        countdownText.setElevation(9999f);

        addContentView(countdownText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        final int[] timeLeft = {seconds};
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (timeLeft[0] <= 0) {
                    // Show "BET" instead of removing the view
                    countdownText.setText("BET");
                    countdownText.setAlpha(0f);
                    countdownText.animate()
                            .alpha(1f)
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(500)
                            .withEndAction(() -> countdownText.animate()
                                    .alpha(0f)
                                    .setDuration(600)
                                    .setStartDelay(600)
                                    .withEndAction(() -> {
                                        ViewGroup parent = (ViewGroup) countdownText.getParent();
                                        if (parent != null) parent.removeView(countdownText);
                                    })
                                    .start())
                            .start();
                    return;
                }

                countdownText.setText(String.valueOf(timeLeft[0]));
                countdownText.setAlpha(1f);
                countdownText.setScaleX(1f);
                countdownText.setScaleY(1f);
                countdownText.animate()
                        .alpha(0f)
                        .scaleX(1.4f)
                        .scaleY(1.4f)
                        .setDuration(900)
                        .start();

                timeLeft[0]--;
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);
    }
}