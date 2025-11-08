package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class JoinActivity extends AppCompatActivity {

    private TextView delButton, joinButton, codeText, titleText;
    private View backButton;
    private String code;
    private String gameType;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // === Get intent extras ===
        Intent incomingIntent = getIntent();
        gameType = incomingIntent.getStringExtra("GAMETYPE");
        username = incomingIntent.getStringExtra("USERNAME");
        code = "";

        // === Find views ===
        backButton = findViewById(R.id.join_back_btn);
        delButton = findViewById(R.id.join_backspace_btn);
        codeText = findViewById(R.id.join_code_display);
        joinButton = findViewById(R.id.join_submit_btn);
        titleText = findViewById(R.id.join_code_title);

        // === Set title and JOIN button colors dynamically ===
        if (gameType != null) {
            int colorRes;
            String gameWord;

            switch (gameType) {
                case "BLACKJACK":
                    colorRes = R.color.my_red;
                    gameWord = "BLACKJACK";
                    joinButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_red));
                    break;
                case "EUCHRE":
                    colorRes = R.color.my_blue;
                    gameWord = "EUCHRE";
                    joinButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_blue));
                    break;
                case "GO_FISH":
                    colorRes = R.color.my_green;
                    gameWord = "GO FISH";
                    joinButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_green));
                    break;
                default:
                    colorRes = R.color.white;
                    gameWord = gameType;
                    joinButton.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_white));
                    break;
            }

            // Create styled title: ENTER {colored game} CODE
            String fullTitle = "ENTER " + gameWord + " CODE";
            SpannableString styled = new SpannableString(fullTitle);

            int start = fullTitle.indexOf(gameWord);
            int end = start + gameWord.length();

            styled.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(this, colorRes)),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            titleText.setText(styled);
        }

        // === Digit buttons ===
        int[] numberButtonIds = {
                R.id.join_0_btn, R.id.join_1_btn, R.id.join_2_btn, R.id.join_3_btn, R.id.join_4_btn,
                R.id.join_5_btn, R.id.join_6_btn, R.id.join_7_btn, R.id.join_8_btn, R.id.join_9_btn
        };

        View.OnClickListener numberClickListener = v -> {
            TextView t = (TextView) v;
            handleCode(t.getText().toString());
        };

        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        // === Delete button ===
        delButton.setOnClickListener(v -> {
            if (!code.isEmpty()) {
                code = code.substring(0, code.length() - 1);
                codeText.setText(code);
            }
        });

        // === Back button ===
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(JoinActivity.this, LobbyActivity.class);
            intent.putExtra("GAMETYPE", gameType);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
        });

        // === JOIN button ===
        joinButton.setOnClickListener(v -> {
            if (code.isEmpty()) {
                Toast.makeText(this, "Enter a code first", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/"
                    + code + "/" + username;
            Log.d("ENDPOINT URL", url);

            JsonObjectRequest postRequest = new JsonObjectRequest(
                    Request.Method.PUT,
                    url,
                    null,
                    response -> {
                        try {
                            String message = response.getString("message");
                            if (message.equals("success")) {
                                Intent intent = new Intent(JoinActivity.this, LobbyViewActivity.class);
                                intent.putExtra("USERNAME", username);
                                intent.putExtra("GAMETYPE", gameType);
                                intent.putExtra("JOINCODE", code);
                                intent.putExtra("HOST", false);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to find lobby.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(getApplicationContext(), "Lobby Join Failed", Toast.LENGTH_LONG).show()
            );

            VolleySingleton.getInstance(JoinActivity.this).addToRequestQueue(postRequest);
        });
    }

    // === Append digit ===
    private void handleCode(String digit) {
        if (code.length() <= 3) {
            code += digit;
            codeText.setText(code);
        }
    }
}
