package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class EuchreActivity extends AppCompatActivity {
    private Button backButton;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_euchre);// link to Main activity XML
        backButton = findViewById(R.id.euchre_back_btn);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EuchreActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
