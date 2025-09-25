package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private Button backButton;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        backButton = findViewById(R.id.profile_back_btn);

        View.OnClickListener backButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
            }
        };

        backButton.setOnClickListener(backButtonListener);

    }
}
