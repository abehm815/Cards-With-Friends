package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HostActivity extends AppCompatActivity {

    private Button backButton;
    private String gameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");

        backButton = findViewById(R.id.host_back_btn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HostActivity.this, LobbyActivity.class);
                intent.putExtra("GAMETYPE", gameType);
                startActivity(intent);
            }
        });

    }

}
