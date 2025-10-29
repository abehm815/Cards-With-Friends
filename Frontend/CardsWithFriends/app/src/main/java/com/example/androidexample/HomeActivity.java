package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidexample.services.BottomNavHelper;

public class HomeActivity extends AppCompatActivity {

    private String username;

    private CardView blackjackCard;
    private CardView euchreCard;
    private CardView goFishCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Link UI components
        blackjackCard = findViewById(R.id.home_blackjack_card);
        euchreCard = findViewById(R.id.home_euchre_card);
        goFishCard = findViewById(R.id.home_gofish_card);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        BottomNavHelper.setupBottomNav(this, username);

        // Blackjack listener
        blackjackCard.setOnClickListener(v -> {
            //TODO: Remove this temporary
            if (username.equals("offline"))
            {
                Intent i = new Intent(HomeActivity.this, BlackjackActivity.class);
                i.putExtra("GAMETYPE", "BLACKJACK");
                i.putExtra("USERNAME", username);
                startActivity(i);
            }
            else{
                Intent i = new Intent(HomeActivity.this, LobbyActivity.class);
                i.putExtra("GAMETYPE", "BLACKJACK");
                i.putExtra("USERNAME", username);
                startActivity(i);
            }
        });

        // Go Fish listener
        goFishCard.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, LobbyActivity.class);
            i.putExtra("GAMETYPE", "GOFISH");
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        // Euchre listener
        euchreCard.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, LobbyActivity.class);
            i.putExtra("GAMETYPE", "EUCHRE");
            i.putExtra("USERNAME", username);
            startActivity(i);
        });
    }
}
