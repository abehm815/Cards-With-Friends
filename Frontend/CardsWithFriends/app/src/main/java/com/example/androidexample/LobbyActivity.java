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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameText = findViewById(R.id.lobby_title);
        backButton = findViewById(R.id.lobby_back_btn);

        Intent intent = getIntent();
        gameText.setText(intent.getStringExtra("GAMETYPE"));

        View.OnClickListener backButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LobbyActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        };
        backButton.setOnClickListener(backButtonListener);

    }
}
