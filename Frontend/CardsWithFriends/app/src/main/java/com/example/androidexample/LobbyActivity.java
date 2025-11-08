package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class LobbyActivity extends AppCompatActivity {

    private TextView gameText;
    private Button backButton;
    private Button joinButton;
    private Button hostButton;
    private Button findButton;
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
        gameText.setText(gameType);

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

    /**
     * Auto-creates a lobby and opens LobbyViewActivity as host.
     */
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
                response -> {}, // handled below
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
