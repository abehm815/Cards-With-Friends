package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;
import com.google.android.material.card.MaterialCardView;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FindLobbyActivity extends AppCompatActivity implements WebSocketListener {

    private static final String TAG = "FindLobbyActivity";
    private LinearLayout lobbyListLayout;

    private Button backButton;
    private Button joinButton;
    private String gameType;
    private String username;
    private TextView gameTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findlobby);
        ConstraintLayout rootLayout = findViewById(R.id.find_lobby_root);

        //setDynamicBackground(rootLayout, gameType);

        lobbyListLayout = findViewById(R.id.lobby_list_layout);
        backButton = findViewById(R.id.find_lobby_back_btn);
        joinButton = findViewById(R.id.find_lobby_join_btn);
        gameTitle = findViewById(R.id.find_lobby_title);


        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");

        setDynamicBackground(rootLayout, gameType);

        gameTitle.setText(gameType + "\nlobbies");

        joinButton.setOnClickListener(v -> {
            Intent i = new Intent(FindLobbyActivity.this, JoinActivity.class);
            i.putExtra("GAMETYPE", gameType);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        backButton.setOnClickListener(v -> {
           Intent i = new Intent(FindLobbyActivity.this, LobbyActivity.class);
           i.putExtra("GAMETYPE", gameType);
           i.putExtra("USERNAME", username);
           startActivity(i);
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to your WebSocket that sends lobby updates
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/lobbies";
        WebSocketManager.getInstance().connectWebSocket(wsUrl);
        WebSocketManager.getInstance().setWebSocketListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebSocketManager.getInstance().disconnectWebSocket();
    }

    // ----------------------- WEBSOCKET CALLBACKS -----------------------

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "Connected to lobby WebSocket");
    }

    @Override
    public void onWebSocketMessage(String message) {
        Log.d(TAG, "Received message: " + message);

        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                if (!json.has("type") || !json.getString("type").equals("LOBBY_LIST")) {
                    Log.d(TAG, "Ignoring non-lobby message");
                    return;
                }

                JSONArray lobbies = json.getJSONArray("lobbies");
                lobbyListLayout.removeAllViews(); // clear before redrawing

                for (int i = 0; i < lobbies.length(); i++) {

                    JSONObject lobby = lobbies.getJSONObject(i);

                    String joinCode = lobby.getString("joinCode");
                    String game = lobby.getString("gameType");
                    JSONArray users = lobby.getJSONArray("usernames");
                    int playerCount = users.length();
                    if(gameType.equals(game) || (game.equals("GO_FISH") && gameType.equals("GOFISH"))){
                        addLobbyCard(joinCode, game, playerCount, users);
                    }
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON", e);
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.d(TAG, "WebSocket closed: " + reason);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "WebSocket error", ex);
    }

    // ----------------------- UI: LOBBY CARD CREATION -----------------------

    private void addLobbyCard(String joinCode, String gameType, int playerCount, JSONArray users) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 16);
        card.setLayoutParams(params);
        card.setRadius(20f);
        card.setCardElevation(8f);
        card.setCardBackgroundColor(Color.WHITE);
        card.setClickable(true);
        card.setFocusable(true);

        // Build player list text
        StringBuilder playerList = new StringBuilder();
        for (int i = 0; i < users.length(); i++) {
            try {
                playerList.append(users.getString(i));
                if (i < users.length() - 1) playerList.append(", ");
            } catch (JSONException ignored) {}
        }

        // Create info view
        TextView info = new TextView(this);
        info.setText(String.format(
                "Game: %s\nJoin Code: %s\nPlayers: %d\n[%s]",
                gameType,
                joinCode,
                playerCount,
                playerList
        ));
        info.setTextColor(Color.BLACK);
        info.setTextSize(18);
        info.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
        info.setPadding(40, 40, 40, 40);

        card.addView(info);

        // Click → Go to lobby
        card.setOnClickListener(v -> {
            Intent intent = new Intent(FindLobbyActivity.this, LobbyViewActivity.class);
            intent.putExtra("JOINCODE", joinCode);
            intent.putExtra("GAMETYPE", gameType);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        lobbyListLayout.addView(card);
    }

    private void setDynamicBackground(ConstraintLayout layout, String gameType) {
        int[] colors;

        switch (gameType.toUpperCase()) {
            case "BLACKJACK":
                colors = new int[]{
                        getColor(R.color.my_red),
                        getColor(R.color.my_dark_red)
                };
                break;
            case "GOFISH":
                colors = new int[]{
                        getColor(R.color.my_green),
                        getColor(R.color.my_dark_green)
                };
                break;
            case "EUCHRE":
                colors = new int[]{
                        getColor(R.color.my_blue),
                        getColor(R.color.my_dark_blue)
                };
                break;
            default:
                colors = new int[]{
                        getColor(R.color.my_grey),
                        getColor(R.color.black)
                };
                break;
        }

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                colors
        );
        gradient.setCornerRadius(0f);
        layout.setBackground(gradient);
    }
}
