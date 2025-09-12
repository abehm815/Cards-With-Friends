package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


    public class PlayActivity extends AppCompatActivity {

        private Button playButton;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_play);

            playButton = findViewById(R.id.back_button);

            View.OnClickListener backButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(PlayActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            };
            playButton.setOnClickListener(backButtonListener);
        }
}
