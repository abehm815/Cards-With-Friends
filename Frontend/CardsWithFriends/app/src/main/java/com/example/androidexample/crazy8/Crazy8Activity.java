package com.example.androidexample.crazy8;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidexample.R;
import com.example.androidexample.services.Crazy8CardView;
import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Crazy8Activity extends AppCompatActivity implements WebSocketListener {

    private static final String TAG = "Crazy8Activity";

    private String username;
    private String joinCode;
    private boolean isHost;

    // UI
    private TextView tvGameStatus;
    private TextView tvCurrentPlayer;
    private TextView tvDeckSize;
    private TextView tvCurrentColor;

    private TextView drawPenaltyBanner;
    private Button btnDrawPenalty;
    private LinearLayout normalButtons;

    private Crazy8CardView viewUpCard;
    private LinearLayout handContainer;

    private View rootLayout;

    private Button btnDraw;
    private Button btnRefresh;

    // Color overlay
    private View colorPickerOverlay;
    private Button btnRed, btnGreen, btnBlue, btnYellow;

    // State
    private boolean isMyTurn = false;
    private boolean waitingForColorChoice = false;
    private int drawStack = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crazy8);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");
        isHost = intent.getBooleanExtra("HOST", false);

        initViews();
        initButtonHandlers();
    }

    private void initViews() {
        tvGameStatus = findViewById(R.id.tvGameStatus);
        tvCurrentPlayer = findViewById(R.id.tvCurrentPlayer);
        tvDeckSize = findViewById(R.id.tvDeckSize);
        tvCurrentColor = findViewById(R.id.tvCurrentColor);
        rootLayout = findViewById(R.id.rootLayout);

        drawPenaltyBanner = findViewById(R.id.drawPenaltyBanner);
        btnDrawPenalty = findViewById(R.id.btnDrawPenalty);
        normalButtons = findViewById(R.id.normalButtons);

        viewUpCard = findViewById(R.id.viewUpCard);
        handContainer = findViewById(R.id.handContainer);

        btnDraw = findViewById(R.id.btnDraw);
        btnRefresh = findViewById(R.id.btnRefresh);

        colorPickerOverlay = findViewById(R.id.colorPickerOverlay);
        btnRed = findViewById(R.id.btnRed);
        btnGreen = findViewById(R.id.btnGreen);
        btnBlue = findViewById(R.id.btnBlue);
        btnYellow = findViewById(R.id.btnYellow);
    }

    private void initButtonHandlers() {
        btnDraw.setOnClickListener(v -> sendDraw());
        btnRefresh.setOnClickListener(v -> sendStatusRequest());

        btnDrawPenalty.setOnClickListener(v -> sendPenaltyDraw());

        btnRed.setOnClickListener(v -> chooseColor('R'));
        btnGreen.setOnClickListener(v -> chooseColor('G'));
        btnBlue.setOnClickListener(v -> chooseColor('B'));
        btnYellow.setOnClickListener(v -> chooseColor('Y'));
    }

    @Override
    protected void onStart() {
        super.onStart();
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/Crazy8/" + joinCode;
        Log.d(TAG, "Connecting to Crazy8 WS: " + wsUrl);
        WebSocketManager.getInstance().connectWebSocket(wsUrl);
        WebSocketManager.getInstance().setWebSocketListener(this);
        tvGameStatus.setText("Connecting...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            JSONObject leave = new JSONObject();
            leave.put("action", "LEAVE");
            leave.put("player", username);
            WebSocketManager.getInstance().sendMessage(leave.toString());
        } catch (JSONException ignored) {}

        WebSocketManager.getInstance().disconnectWebSocket();
    }

    // ----------------------- WebSocket Events ---------------------------- //

    @Override
    public void onWebSocketOpen(ServerHandshake handshake) {
        runOnUiThread(() -> tvGameStatus.setText("Connected. Waiting..."));

        if (isHost) {
            try {
                JSONObject start = new JSONObject();
                start.put("action", "START");
                start.put("player", username);
                WebSocketManager.getInstance().sendMessage(start.toString());

                JSONObject round = new JSONObject();
                round.put("action", "STARTROUND");
                round.put("player", username);
                WebSocketManager.getInstance().sendMessage(round.toString());

            } catch (JSONException ignored) {}
        } else {
            sendStatusRequest();
        }
    }

    @Override
    public void onWebSocketMessage(String msg) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(msg);
                String type = json.optString("type");

                switch (type) {
                    case "gameState": handleGameState(json); break;
                    case "colorChoiceRequest": handleColorChoiceRequest(json); break;
                    case "error":
                        Toast.makeText(this, json.optString("error"), Toast.LENGTH_SHORT).show();
                        break;
                }

            } catch (Exception e) {
                Log.e(TAG, "Parse error", e);
            }
        });
    }

    @Override public void onWebSocketClose(int code, String reason, boolean remote) {}
    @Override public void onWebSocketError(Exception ex) {}

    // ----------------------- Game Logic UI ---------------------------- //

    private void handleGameState(JSONObject json) throws JSONException {
        String currentPlayer = json.optString("currentPlayer");
        isMyTurn = currentPlayer.equals(username);

        String currentColor = json.optString("currentColor", "-");
        UIHelpers.applyColorTheme(rootLayout, currentColor, this);

        tvCurrentPlayer.setText("Current: " + currentPlayer);
        tvCurrentColor.setText("Color: " + currentColor);
        tvDeckSize.setText("Deck: " + json.optInt("deckSize"));

        drawStack = json.optInt("drawStack");
        waitingForColorChoice = json.optBoolean("waitingForColorChoice");

        if (drawStack > 0 && isMyTurn) {
            UIHelpers.enablePenaltyMode(
                    drawPenaltyBanner,
                    btnDrawPenalty,
                    normalButtons,
                    drawStack
            );
            tvGameStatus.setText("Penalty: draw or stack");
        } else {
            UIHelpers.disablePenaltyMode(drawPenaltyBanner, btnDrawPenalty, normalButtons);
            tvGameStatus.setText(isMyTurn ? "Your turn" : "Waiting on " + currentPlayer);
        }

        if (json.has("upCard")) {
            JSONObject up = json.getJSONObject("upCard");
            viewUpCard.setCard(up.optInt("value"), up.optString("color").charAt(0), true);
        }

        drawHand(json);
    }

    private void drawHand(JSONObject json) throws JSONException {
        handContainer.removeAllViews();

        JSONArray players = json.getJSONArray("players");
        for (int i = 0; i < players.length(); i++) {
            JSONObject p = players.getJSONObject(i);
            if (!p.getString("username").equals(username)) continue;

            JSONArray hand = p.getJSONArray("hand");
            for (int j = 0; j < hand.length(); j++) {
                JSONObject cardJson = hand.getJSONObject(j);

                int val = cardJson.optInt("value");
                char col = cardJson.optString("color").charAt(0);
                boolean playable = cardJson.optBoolean("isPlayable");

                Crazy8CardView card = new Crazy8CardView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(220, 320);
                lp.setMargins(12, 0, 12, 0);
                card.setLayoutParams(lp);

                card.setCard(val, col, true);

                boolean allowed =
                        isMyTurn &&
                                !waitingForColorChoice &&
                                playable &&
                                (drawStack == 0 || val == 13 || val == 14);

                card.setAlpha(allowed ? 1f : 0.4f);

                if (allowed) {
                    card.setOnClickListener(v -> sendPlayCard(col, val));
                }

                handContainer.addView(card);
            }
        }
    }

    // ----------------------- Color Picker ---------------------------- //

    private void handleColorChoiceRequest(JSONObject json) {
        String target = json.optString("username");

        if (!target.equals(username)) {
            tvGameStatus.setText(json.optString("message"));
            return;
        }

        waitingForColorChoice = true;
        tvGameStatus.setText("Choose a color...");
        UIHelpers.showColorPicker(colorPickerOverlay);
    }

    private void chooseColor(char c) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "CHOOSECOLOR");
            obj.put("player", username);
            obj.put("cardcolor", String.valueOf(c));
            WebSocketManager.getInstance().sendMessage(obj.toString());
        } catch (Exception ignored) {}

        waitingForColorChoice = false;
        UIHelpers.hideColorPicker(colorPickerOverlay);
    }

    // ----------------------- Outgoing ---------------------------- //

    private void sendPlayCard(char color, int value) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "PLAYCARD");
            obj.put("player", username);
            obj.put("cardcolor", String.valueOf(color));
            obj.put("cardvalue", value);
            WebSocketManager.getInstance().sendMessage(obj.toString());
        } catch (Exception ignored) {}
    }

    private void sendStatusRequest() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "STATUS");
            obj.put("player", username);
            WebSocketManager.getInstance().sendMessage(obj.toString());
        } catch (Exception ignored) {}
    }

    private void sendDraw() {
        if (drawStack > 0) {
            Toast.makeText(this, "You must draw penalty cards", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "DRAW");
            obj.put("player", username);
            WebSocketManager.getInstance().sendMessage(obj.toString());
        } catch (Exception ignored) {}
    }

    private void sendPenaltyDraw() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("action", "DRAW");
            obj.put("player", username);
            WebSocketManager.getInstance().sendMessage(obj.toString());
        } catch (Exception ignored) {}
    }
}