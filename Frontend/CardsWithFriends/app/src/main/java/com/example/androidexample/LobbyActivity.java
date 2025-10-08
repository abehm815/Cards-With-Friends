package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LobbyActivity extends AppCompatActivity{

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

        gameText = findViewById(R.id.lobby_title);
        backButton = findViewById(R.id.lobby_back_btn);
        joinButton = findViewById(R.id.lobby_join_btn);
        hostButton = findViewById(R.id.lobby_host_btn);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        gameText.setText(gameType);

        View.OnClickListener backButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LobbyActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        };
        backButton.setOnClickListener(backButtonListener);

        View.OnClickListener joinButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LobbyActivity.this, JoinActivity.class);
                intent.putExtra("GAMETYPE", gameType);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        };
        joinButton.setOnClickListener(joinButtonListener);

        View.OnClickListener hostButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LobbyActivity.this, HostActivity.class);
                intent.putExtra("GAMETYPE", gameType);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        };
        hostButton.setOnClickListener(hostButtonListener);

    }
}
