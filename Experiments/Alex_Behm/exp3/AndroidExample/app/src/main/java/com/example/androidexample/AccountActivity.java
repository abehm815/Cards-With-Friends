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

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        accountInfo = findViewById(R.id.account_info_text);
        Intent intent = getIntent();

        logoutButton = findViewById(R.id.account_logout_btn);

        username = intent.getStringExtra("USERNAME");
        password = intent.getStringExtra("PASSWORD");

        accountInfo.setText("Username: " + username + "\nPassword: "+password);

        View.OnClickListener logoutListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };

        logoutButton.setOnClickListener(logoutListener);



    }
    }
