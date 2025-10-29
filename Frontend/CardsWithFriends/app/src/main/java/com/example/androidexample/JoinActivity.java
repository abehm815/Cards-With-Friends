package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class JoinActivity extends AppCompatActivity {

    private Button backButton, delButton, joinButton;
    private TextView codeText;
    private String code;

    private String gameType;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Intent incomingIntent = getIntent();
        gameType = incomingIntent.getStringExtra("GAMETYPE");
        username = incomingIntent.getStringExtra("USERNAME");

        code = "";

        backButton = findViewById(R.id.join_back_btn);
        delButton = findViewById(R.id.join_backspace_btn);
        codeText = findViewById(R.id.join_code_display);
        joinButton = findViewById(R.id.join_submit_btn);

        // Button IDs into an array
        int[] numberButtonIds = {
                R.id.join_0_btn,
                R.id.join_1_btn,
                R.id.join_2_btn,
                R.id.join_3_btn,
                R.id.join_4_btn,
                R.id.join_5_btn,
                R.id.join_6_btn,
                R.id.join_7_btn,
                R.id.join_8_btn,
                R.id.join_9_btn
        };

        // Create ONE click listener for all number buttons
        View.OnClickListener numberClickListener = v -> {
            Button b = (Button) v;
            handleCode(b);
        };

        // Attach the listener to all buttons using a loop
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

        // Back button logic
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(JoinActivity.this, LobbyActivity.class);
            intent.putExtra("GAMETYPE", gameType);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        //TODO: Backend needs to change so that

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + codeText.getText() + "/" + username;
                Log.d("ENDPOINT URL", url);
                JsonObjectRequest postRequest = new JsonObjectRequest(
                        Request.Method.PUT,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String message = response.getString("message");
                                    if (message.equals("success")){
                                        Toast.makeText(getApplicationContext(), "Lobby Joined", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(JoinActivity.this, LobbyViewActivity.class);
                                        intent.putExtra("USERNAME", username);
                                        intent.putExtra("GAMETYPE", gameType);
                                        intent.putExtra("JOINCODE", code);
                                        startActivity(intent);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Failed to find lobby.", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Lobby Join Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                );

                VolleySingleton.getInstance(JoinActivity.this).addToRequestQueue(postRequest);
            }
        });
    }
    // Helper function to set the code based on button
    private void handleCode(Button b) {
        if (code.length() <= 3) {
            code += b.getText().toString();
            codeText.setText(code);
        }
    }
}
