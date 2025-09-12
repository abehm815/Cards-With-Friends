package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.constraintlayout.widget.ConstraintLayout;


public class AdminActivity extends  AppCompatActivity{

    private TextView welcomeText;

    private TextView messageText;

    private Button darkButton;

    private Button lightButton;

    private Button signInButton;

    private ConstraintLayout adminLayout;





    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        /* initialize UI elements */
        welcomeText = findViewById(R.id.textView);
        messageText = findViewById(R.id.welcomeText);
        darkButton = findViewById(R.id.darkModeButton);
        lightButton = findViewById(R.id.lightModeButton);
        signInButton = findViewById(R.id.signInButton);
        adminLayout = findViewById(R.id.adminLayout);

        Bundle extras = getIntent().getExtras();

        //should wrap this in an if else for if extras == null
        String adminName = extras.getString("USERNAME");
        messageText.setText("admin " + adminName);






        darkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcomeText.setTextColor(Color.parseColor("#BDBDBD"));
                adminLayout.setBackgroundColor(Color.parseColor("#1F1F1F"));
                messageText.setTextColor(Color.parseColor("#BDBDBD"));

            }
        });

        lightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcomeText.setTextColor(Color.parseColor("#000000"));
                adminLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                messageText.setTextColor(Color.parseColor("#000000"));


            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });







    }

}
