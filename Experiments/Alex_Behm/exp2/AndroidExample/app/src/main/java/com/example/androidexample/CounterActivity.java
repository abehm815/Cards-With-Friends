package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CounterActivity extends AppCompatActivity {

    private TextView lobbyText;

    private boolean isValidLobby;

    private Button backButton;

    //Hardcoded lobby IDs for example (would get from backend)
    private String[] lobbies = new String[] {"test1","test2","test3","test4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        // Get the lobby ID the user typed in from the main activity
        Intent intent = getIntent();
        String lobbyID = intent.getStringExtra("LOBBY_ID");

        // Hook up UI components
        lobbyText = findViewById(R.id.lobby_id_text);
        backButton = findViewById(R.id.back_button_ui);

        // Test to see if the lobby the user entered is valid
        isValidLobby = false;
        for (int i = 0; i < lobbies.length; i++){
            if (lobbyID.equals(lobbies[i])){
                isValidLobby = true;
            }
        }

        // Set and format text to show if we are in a lobby or not
        if (isValidLobby){
            lobbyText.setText("Entered Lobby, ID" + ": " + lobbyID);
            lobbyText.setTextColor(Color.GREEN);
            backButton.setText("Back");
            backButton.setBackgroundColor(Color.RED);
        }
        else{
            lobbyText.setText("Not a valid lobby");
            lobbyText.setTextColor(Color.RED);
            backButton.setText("Retry");
            backButton.setBackgroundColor(Color.DKGRAY);
        }

        // Set back button to communicate the lobby ID back to the main activity
        View.OnClickListener backButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CounterActivity.this, MainActivity.class);
                intent.putExtra("LOBBY_ID", lobbyID);
                intent.putExtra("VALID_LOBBY", isValidLobby);
                startActivity(intent);
            }
        };
        backButton.setOnClickListener(backButtonListener);

    }
}