package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class JoinActivity extends AppCompatActivity {

    private Button backButton, delButton;
    private TextView codeText;
    private String code;

    private String gameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Intent incomingIntent = getIntent();
        gameType = incomingIntent.getStringExtra("GAMETYPE");

        code = "";

        backButton = findViewById(R.id.join_back_btn);
        delButton = findViewById(R.id.join_backspace_btn);
        codeText = findViewById(R.id.join_code_display);

        // Button IDs into an array
        int[] numberButtonIds = {
                R.id.join_0_btn,
                R.id.join_1_btn,
                R.id.join_2_btn,
                R.id.join_3_btn,
                R.id.join_4_btn,
                R.id.join_5_btn,
                R.id.join_6_btn,
                R.id.join_7_btn,
                R.id.join_8_btn,
                R.id.join_9_btn
        };

        // Create ONE click listener for all number buttons
        View.OnClickListener numberClickListener = v -> {
            Button b = (Button) v;
            handleCode(b);
        };

        // Attach the listener to all buttons using a loop
        for (int id : numberButtonIds) {
            findViewById(id).setOnClickListener(numberClickListener);
        }

        // Delete button logic
        delButton.setOnClickListener(v -> {
            if (!code.isEmpty()) {
                code = code.substring(0, code.length() - 1);
                codeText.setText(code);
            }
        });

        // Back button logic
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(JoinActivity.this, LobbyActivity.class);
            intent.putExtra("GAMETYPE", gameType);
            startActivity(intent);
        });
    }

    // Helper function to set the code based on button
    private void handleCode(Button b) {
        if (code.length() <= 3) {
            code += b.getText().toString();
            codeText.setText(code);
        }
    }
}
