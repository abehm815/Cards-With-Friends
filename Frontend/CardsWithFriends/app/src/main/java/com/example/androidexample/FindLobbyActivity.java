package com.example.androidexample;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FindLobbyActivity extends AppCompatActivity {


    String gameType;
    String username;
    private Button backButton;
    private Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_findlobby);

        // Link UI
        ConstraintLayout rootLayout = findViewById(R.id.find_lobby_root);
        backButton = findViewById(R.id.find_lobby_back_btn);
        joinButton = findViewById(R.id.find_lobby_join_btn);

        // Get intent data
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");


        // Set dynamic background gradient
        setDynamicBackground(rootLayout, gameType);

        RecyclerView recyclerView = findViewById(R.id.lobby_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

// Temporary dummy data (you can replace this later with backend results)
        List<Lobby> testLobbies = new ArrayList<>();
        testLobbies.add(new Lobby("Fun Table", 3));
        testLobbies.add(new Lobby("Serious Players", 5));
        testLobbies.add(new Lobby("Late Night Crew", 2));

        LobbyAdapter adapter = new LobbyAdapter(testLobbies);
        recyclerView.setAdapter(adapter);


        // Back button → return to home
        backButton.setOnClickListener(v -> {
            Intent i = new Intent(FindLobbyActivity.this, LobbyActivity.class);
            i.putExtra("USERNAME", username);
            i.putExtra("GAMETYPE", gameType);
            startActivity(i);
        });

        // Join button
        joinButton.setOnClickListener(v -> {
            //Somehow need to get target game id from websocket and do some shit with it.
            Intent i = new Intent(FindLobbyActivity.this, JoinActivity.class);
            i.putExtra("GAMETYPE", gameType);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        // Host button



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
