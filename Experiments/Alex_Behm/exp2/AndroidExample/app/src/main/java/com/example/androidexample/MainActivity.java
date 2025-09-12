package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

/*

1. To run this project, open the directory "Android Example", otherwise it may not recognize the file structure properly

2. Ensure you are using a compatible version of gradle, to do so you need to check 2 files.

    AndroidExample/Gradle Scripts/build.gradle
    Here, you will have this block of code. Ensure it is set to a compatible version,
    in this case 8.12.2 should be sufficient:
        plugins {
            id 'com.android.application' version '8.12.2' apply false
        }

    Gradle Scripts/gradle-wrapper.properties

3. This file is what actually determines the Gradle version used, 8.13 should be sufficient.
    "distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip" ---Edit the version if needed

4. You might be instructed by the plugin manager to upgrade plugins, accept it and you may execute the default selected options.

5. Press "Sync project with gradle files" located at the top right of Android Studio,
   once this is complete you will be able to run the app

   This version is compatible with both JDK 17 and 21. The Java version you want to use can be
   altered in Android Studio->Settings->Build, Execution, Deployment->Build Tools->Gradle

 */


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
            lobbyText.setText("You are in a lobby!: " + lobbyID);
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