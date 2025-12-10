package com.example.androidexample;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MatchDetailActivity extends AppCompatActivity {

    private TextView titleBox, eventsBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        titleBox = findViewById(R.id.detail_title);
        eventsBox = findViewById(R.id.detail_events);

        String title = getIntent().getStringExtra("title");
        ArrayList<String> events = getIntent().getStringArrayListExtra("events");

        titleBox.setText(title);

        StringBuilder sb = new StringBuilder();
        for (String e : events) {
            sb.append("• ").append(e).append("\n\n");
        }

        eventsBox.setText(sb.toString());
    }
}
