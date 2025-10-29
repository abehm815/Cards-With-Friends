package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class HostActivity extends AppCompatActivity {

    private Button backButton;

    private Button createLobbyButton;

    private Button delButton;
    private String gameType;

    private String username;

    private String code = "";

    private TextView codeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");

        backButton = findViewById(R.id.host_back_btn);
        createLobbyButton = findViewById(R.id.host_create_btn);
        codeText = findViewById(R.id.host_code_display);
        delButton = findViewById(R.id.host_backspace_btn);

        // Button IDs into an array
        int[] numberButtonIds = {
                R.id.host_0_btn,
                R.id.host_1_btn,
                R.id.host_2_btn,
                R.id.host_3_btn,
                R.id.host_4_btn,
                R.id.host_5_btn,
                R.id.host_6_btn,
                R.id.host_7_btn,
                R.id.host_8_btn,
                R.id.host_9_btn
        };

        // Create ONE click listener for all number buttons
        View.OnClickListener numberClickListener = v -> {
            Button b = (Button) v;
            handleCode(b);
        };
        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        // Delete button logic
        delButton.setOnClickListener(v -> {
            if (!code.isEmpty()) {
                code = code.substring(0, code.length() - 1);
                codeText.setText(code);
            }
        });

        //Back Button logic
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HostActivity.this, LobbyActivity.class);
                intent.putExtra("GAMETYPE", gameType);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        //TODO: Backend needs to change so that the code is auto generated
        //TODO: Backend needs to change so no two lobby of the same join code can be created

        createLobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + code + "/" + username;

                JSONObject body = new JSONObject();

                try {
                    body.put("gameType", gameType);
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
                                Intent intent = new Intent(HostActivity.this, LobbyViewActivity.class);
                                intent.putExtra("USERNAME", username);
                                intent.putExtra("GAMETYPE", gameType);
                                intent.putExtra("JOINCODE", code);
                                startActivity(intent);
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
    private void handleCode(Button b) {
        if (code.length() <= 3) {
            code += b.getText().toString();
            codeText.setText(code);
        }
    }

}
