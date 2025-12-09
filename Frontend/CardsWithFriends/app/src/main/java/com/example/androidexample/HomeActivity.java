package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.androidexample.blackjack.BlackjackActivity;
import com.example.androidexample.services.BottomNavHelper;

public class HomeActivity extends AppCompatActivity {

    private String username;

    private CardView blackjackCard;
    private CardView euchreCard;
    private CardView goFishCard;
    private CardView crazy8sCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        blackjackCard = findViewById(R.id.home_blackjack_card);
        euchreCard = findViewById(R.id.home_euchre_card);
        goFishCard = findViewById(R.id.home_gofish_card);
        crazy8sCard = findViewById(R.id.home_crazy8s_card);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        BottomNavHelper.setupBottomNav(this, username);

        blackjackCard.setOnClickListener(v -> {
            Intent i;
            if (username.equals("offline")) {
                i = new Intent(HomeActivity.this, BlackjackActivity.class);
            } else {
                i = new Intent(HomeActivity.this, LobbyActivity.class);
            }
            i.putExtra("GAMETYPE", "BLACKJACK");
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        goFishCard.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, LobbyActivity.class);
            i.putExtra("GAMETYPE", "GO_FISH");
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        euchreCard.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, LobbyActivity.class);
            i.putExtra("GAMETYPE", "EUCHRE");
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        crazy8sCard.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, LobbyActivity.class);
            i.putExtra("GAMETYPE", "CRAZY8");
            i.putExtra("USERNAME", username);
            startActivity(i);
        });
    }
}
