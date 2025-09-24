package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private String username;
    private Button profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profileButton = findViewById(R.id.home_profile_btn);

        //Get username and password from signup screen and display it to the user
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        profileButton.setText(username);

        //Create a listener for the logout button (Links back to main page when clicked)
        View.OnClickListener profileListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        };
        profileButton.setOnClickListener(profileListener);
    }
    }
