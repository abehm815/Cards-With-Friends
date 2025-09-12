package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PlayActivity extends AppCompatActivity {
    private Button euchreButton;

    private Button blackjackButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);// link to Main activity XML

        euchreButton = findViewById(R.id.euchre);
        blackjackButton = findViewById(R.id.blackjack);

        euchreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayActivity.this, EuchreActivity.class);
                startActivity(intent);
            }
        });

        blackjackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlayActivity.this, BlackjackActivity.class);
                startActivity(intent);
            }
        });
        /* initialize UI elements */

    }
}
