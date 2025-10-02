package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private Button backButton;

    private Button deleteButton;

    private TextView usernameDisplayText;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        backButton = findViewById(R.id.profile_back_btn);
        deleteButton = findViewById(R.id.profile_delete_btn);
        usernameDisplayText = findViewById(R.id.profile_username_text);
        usernameDisplayText.setText(username);

        View.OnClickListener backButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
            }
        };

        View.OnClickListener deleteButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //this is where we will do delete stuff
            }
        };



        backButton.setOnClickListener(backButtonListener);
        deleteButton.setOnClickListener(deleteButtonListener);





    }
}
