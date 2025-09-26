package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidexample.services.MusicService;

public class HomeActivity extends AppCompatActivity {
    private String username;
    private Button profileButton;

    private CardView blackjackCard;

    private CardView euchreCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Link UI Components
        profileButton = findViewById(R.id.home_profile_btn);
        blackjackCard = findViewById(R.id.home_blackjack_card);
        euchreCard = findViewById(R.id.home_euchre_card);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        //Profile Button
        View.OnClickListener profileListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        };
        profileButton.setOnClickListener(profileListener);

        //Euchre Button
        View.OnClickListener euchreListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, LobbyActivity.class);
                intent.putExtra("GAMETYPE", "Euchre");
                startActivity(intent);
            }
        };
        euchreCard.setOnClickListener(euchreListener);

        //Blackjack Button
        View.OnClickListener blackjackListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, LobbyActivity.class);
                intent.putExtra("GAMETYPE", "Blackjack");
                startActivity(intent);
            }
        };
        blackjackCard.setOnClickListener(blackjackListener);
    }
    }
