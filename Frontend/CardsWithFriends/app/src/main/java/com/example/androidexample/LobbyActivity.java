package com.example.androidexample;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LobbyActivity extends AppCompatActivity {

    private TextView gameText;
    private Button backButton;
    private Button joinButton;
    private Button hostButton;
    private String gameType;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        // Link UI
        ConstraintLayout rootLayout = findViewById(R.id.lobby_root);
        gameText = findViewById(R.id.lobby_title);
        backButton = findViewById(R.id.lobby_back_btn);
        joinButton = findViewById(R.id.lobby_join_btn);
        hostButton = findViewById(R.id.lobby_host_btn);

        // Get intent data
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        gameText.setText(gameType);

        // Set dynamic background gradient
        setDynamicBackground(rootLayout, gameType);

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

        // Host button
        hostButton.setOnClickListener(v -> {
            Intent i = new Intent(LobbyActivity.this, HostActivity.class);
            i.putExtra("GAMETYPE", gameType);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });
    }

    /**
     * Dynamically sets gradient background based on game type.
     */
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
