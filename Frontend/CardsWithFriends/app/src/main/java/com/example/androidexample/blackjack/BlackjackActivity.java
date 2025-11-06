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
        if (isHost) {
            //Send START and STARTROUND commands to the backend
            String startMsg = "{ \"action\": \"START\", \"player\": \"" + username + "\" }";
            String startRoundMsg = "{ \"action\": \"STARTROUND\", \"player\": \"" + username + "\" }";
            Log.d("WebSocket", "Host started new round");
            WebSocketManager.getInstance().sendMessage(startMsg);
            WebSocketManager.getInstance().sendMessage(startRoundMsg);
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
                    updateDealerCardsUI();
                    updateActionButtonsUI();

                }, viewSwitchDelay);

                // Immediate refresh too
                playerTurnBar.updateFromGameState(gameState, selectedPlayer);
                updateBalanceUI();
                updateCardUI();
                updateDealerCardsUI();
                updateActionButtonsUI();

                //TODO change so that backend sends a notification when the round is over (This is a wierd check)
                boolean isRoundOver = gameState.currentTurn == null || gameState.currentTurn.equals("null");

                // -------------------------------------------------------------------
                // SHOW RESULT TEXT
                // -------------------------------------------------------------------
                long resultDelay = 3000;
                String turn = gameState.currentTurn;
                if (isRoundOver) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .postDelayed(() -> {
                                PlayerState me = null;
                                for (PlayerState p : gameState.players) {
                                    if (p.username.equals(username)) {
                                        me = p;
                                        break;
                                    }
                                }
                                if (me != null && me.hands != null && !me.hands.isEmpty()) {
                                    int dealerValue = gameState.dealer != null ? gameState.dealer.handValue : 0;
                                    int playerValue = me.hands.get(0).handValue;
                                    String result;
                                    //Determine what result text to show
                                    if (playerValue == 21 && me.hands.get(0).hand.size() == 2) {
                                        result = "Win";
                                    } else if (playerValue > 21 || (dealerValue <= 21 && dealerValue > playerValue)) {
                                        result = "Loss";
                                    } else if (playerValue == dealerValue) {
                                        result = "Push";
                                    } else {
                                        result = "Win";
                                    }
                                    showRoundResult(result);
                                }
                            }, resultDelay);

                    // -------------------------------------------------------------------
                    // Show the "NEW ROUND BUTTON" to host
                    // -------------------------------------------------------------------
                    long roundButtonDelay = 6000;
                    if (isHost) {
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .postDelayed(() -> {
                                    startNewRoundBtn.setVisibility(View.VISIBLE);
                                    startNewRoundBtn.setAlpha(0f);
                                    startNewRoundBtn.setTranslationY(100f);
                                    startNewRoundBtn.animate()
                                            .translationY(0f)
                                            .alpha(1f)
                                            .setDuration(500)
                                            .start();
                                }, roundButtonDelay);
                    }
                } else {
                    startNewRoundBtn.setVisibility(View.GONE);
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

    private void updateDealerCardsUI() {
        cardRenderer.renderDealer(dealerCardContainer, gameState.dealer);
    }

    // -------------------------------------------------------------------
    // Action buttons + round result
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

    /**
     * Show end-of-round result text (WIN / LOSS / PUSH)
     */
    private void showRoundResult(String resultType) {
        TextView resultText = new TextView(this);
        resultText.setText(resultType.toUpperCase());
        resultText.setTextSize(56);
        resultText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_black));
        resultText.setGravity(Gravity.CENTER);
        resultText.setAlpha(0f);
        resultText.setElevation(1000f); // render above everything
        resultText.setShadowLayer(20f, 0f, 0f, Color.BLACK);

        int color;
        switch (resultType.toLowerCase()) {
            case "win":
                color = ContextCompat.getColor(this, R.color.my_green);
                break;
            case "push":
                color = ContextCompat.getColor(this, R.color.my_orange);
                break;
            default:
                color = ContextCompat.getColor(this, R.color.my_red);
                break;
        }
        resultText.setTextColor(color);

        addContentView(resultText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        resultText.animate()
                .alpha(1f)
                .translationYBy(-40)
                .setDuration(700)
                .withEndAction(() -> {
                    resultText.animate()
                            .alpha(0f)
                            .setStartDelay(1800)
                            .setDuration(700)
                            .withEndAction(() -> {
                                ViewGroup parent = (ViewGroup) resultText.getParent();
                                if (parent != null) parent.removeView(resultText);
                            })
                            .start();
                })
                .start();
    }
}