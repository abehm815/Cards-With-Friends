package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    private TextView accountInfo;
    private String username;
    private String password;
    private Button finishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //Link UI components
        accountInfo = findViewById(R.id.account_info_text);
        finishButton = findViewById(R.id.account_finish_btn);

        //Get username and password from signup screen and display it to the user
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        password = intent.getStringExtra("PASSWORD");
        accountInfo.setText("Username: " + username + "\nPassword: "+password);

        //Create a listener for the logout button (Links back to main page when clicked)
        View.OnClickListener logoutListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        };
        finishButton.setOnClickListener(logoutListener);
    }
    }
