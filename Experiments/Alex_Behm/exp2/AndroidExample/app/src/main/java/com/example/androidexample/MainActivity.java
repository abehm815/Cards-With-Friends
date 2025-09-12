package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button joinButton;

    private String lobbyID;

    private EditText lobbyInput;

    private TextView lobbyText;

    private Boolean isValidLobby;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);// link to Main activity XML

        // Link up UI components
        joinButton = findViewById(R.id.join_button);
        lobbyInput = findViewById(R.id.lobby_code_input);
        lobbyText = findViewById(R.id.lobby_ind_text);

        // Get lobby ID and its validity from CounterActivity
        Intent intent = getIntent();
        lobbyID = intent.getStringExtra("LOBBY_ID");
        isValidLobby = intent.getBooleanExtra("VALID_LOBBY", false);

        // Check if we are in a lobby and display to user
        if (!isValidLobby){
            lobbyText.setText("You are not in a lobby");
            lobbyText.setTextColor(Color.RED);
        }
        else {
            lobbyText.setText("You are in a lobby, ID: " + lobbyID);
            lobbyText.setTextColor(Color.GREEN);
        }

        // Grabs the lobby ID from the textfield and moves to the CounterActivity when Join Lobby is pressed
        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lobbyCode = lobbyInput.getText().toString();
                Intent intent = new Intent(MainActivity.this, CounterActivity.class);
                intent.putExtra("LOBBY_ID", lobbyCode);
                startActivity(intent);
            }
        };
        joinButton.setOnClickListener(buttonListener);
    }
}