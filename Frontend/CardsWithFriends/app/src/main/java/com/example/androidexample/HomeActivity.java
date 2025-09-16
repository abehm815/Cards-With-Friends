package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView accountInfo;
    private String username;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Link UI components
        accountInfo = findViewById(R.id.home_username_text);
        logoutButton = findViewById(R.id.home_logout_btn);

        //Get username and password from signup screen and display it to the user
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        accountInfo.setText("Hello, " + username);

        //Create a listener for the logout button (Links back to main page when clicked)
        View.OnClickListener logoutListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };
        logoutButton.setOnClickListener(logoutListener);
    }
    }
