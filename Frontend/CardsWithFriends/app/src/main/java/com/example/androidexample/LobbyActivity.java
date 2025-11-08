package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class LobbyActivity extends AppCompatActivity {

    private TextView gameText;
    private TextView joinButton;
    private TextView hostButton;
    private TextView findButton;
    private TextView backButton;
    private String gameType;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Link UI
        gameText = findViewById(R.id.lobby_title);
        backButton = findViewById(R.id.lobby_back_btn);
        joinButton = findViewById(R.id.lobby_join_btn);
        hostButton = findViewById(R.id.lobby_host_btn);
        findButton = findViewById(R.id.lobby_find_btn);

        // Get intent data
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");

        // Normalize and color title (ALL CAPS)
        String normalized = gameType.replace("_", " ").toUpperCase();
        int accentColor;
        switch (gameType.toUpperCase()) {
            case "BLACKJACK":
                accentColor = ContextCompat.getColor(this, R.color.my_red);
                break;
            case "EUCHRE":
                accentColor = ContextCompat.getColor(this, R.color.my_blue);
                break;
            case "GO_FISH":
                accentColor = ContextCompat.getColor(this, R.color.my_green);
                break;
            default:
                accentColor = ContextCompat.getColor(this, android.R.color.white);
        }

        String titleText = "ENTER " + normalized + " LOBBY";
        SpannableString span = new SpannableString(titleText);
        int start = titleText.indexOf(normalized);
        int end = start + normalized.length();
        span.setSpan(new ForegroundColorSpan(accentColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        gameText.setText(span);

        // Back button → return to home
        backButton.setOnClickListener(v -> {
            Intent i = new Intent(LobbyActivity.this, HomeActivity.class);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        // Join button
        joinButton.setOnClickListener(v -> {
            Intent i = new Intent(LobbyActivity.this, JoinActivity.class);
            i.putExtra("GAMETYPE", gameType);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        // Host button → autogen and open LobbyViewActivity
        hostButton.setOnClickListener(v -> createAutoLobby());

        // Find button
        findButton.setOnClickListener(v -> {
            Intent i = new Intent(LobbyActivity.this, FindLobbyActivity.class);
            i.putExtra("GAMETYPE", gameType);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });
    }

    private void createAutoLobby() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/autogen/" + username;

        JSONObject body = new JSONObject();
        try {
            body.put("gameType", gameType.toUpperCase());
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {},
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected com.android.volley.Response<JSONObject> parseNetworkResponse(com.android.volley.NetworkResponse response) {
                try {
                    String joinCode = new String(response.data).replace("\"", "").trim();
                    runOnUiThread(() -> {
                        Intent i = new Intent(LobbyActivity.this, LobbyViewActivity.class);
                        i.putExtra("GAMETYPE", gameType);
                        i.putExtra("USERNAME", username);
                        i.putExtra("JOINCODE", joinCode);
                        i.putExtra("HOST", true);
                        startActivity(i);
                    });
                    return com.android.volley.Response.success(new JSONObject(), null);
                } catch (Exception e) {
                    return com.android.volley.Response.error(new com.android.volley.VolleyError(e));
                }
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
}