package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class HostActivity extends AppCompatActivity {

    private Button backButton;

    private Button createLobbyButton;
    private String gameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");

        backButton = findViewById(R.id.host_back_btn);
        createLobbyButton = findViewById(R.id.host_create_btn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HostActivity.this, LobbyActivity.class);
                intent.putExtra("GAMETYPE", gameType);
                startActivity(intent);
            }
        });

        createLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby";

                JSONObject body = new JSONObject();

                try {
                    int enumedGameType = 0;
                    if (gameType.equals("Blackjack")){
                        enumedGameType = 1;
                    } else if (gameType.equals("Euchre")) {
                        enumedGameType = 2;
                    }
                    body.put("gameType", enumedGameType);
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Error Creating Request", Toast.LENGTH_LONG).show();
                    return;
                }

                JsonObjectRequest postRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        body,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(getApplicationContext(), "Lobby Created", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Lobby Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                );

                VolleySingleton.getInstance(HostActivity.this).addToRequestQueue(postRequest);
            }
        });

    }

}
