package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.androidexample.services.CardView;
import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;

import org.java_websocket.handshake.ServerHandshake;

import java.util.ArrayList;

public class BlackjackActivity extends AppCompatActivity implements WebSocketListener {

    private String joinCode;
    private String username;

    private ArrayList<String> players = new ArrayList<>();

    private String gameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blackjack);
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");
        players = intent.getStringArrayListExtra("PLAYERS");

        ConstraintLayout root = findViewById(R.id.rootLayout);
        Button flipBtn = findViewById(R.id.flipButton);
        // Create and add card
        CardView card = new CardView(this);
        card.setId(View.generateViewId());
        card.setLayoutParams(new ConstraintLayout.LayoutParams(300, 400));
        card.setCard("J", "C", true);
        root.addView(card);
        // Position the card slightly below center
        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        set.connect(card.getId(), ConstraintSet.LEFT, root.getId(), ConstraintSet.LEFT, 0);
        set.connect(card.getId(), ConstraintSet.RIGHT, root.getId(), ConstraintSet.RIGHT, 0);
        set.connect(card.getId(), ConstraintSet.TOP, root.getId(), ConstraintSet.TOP, 0);
        set.connect(card.getId(), ConstraintSet.BOTTOM, root.getId(), ConstraintSet.BOTTOM, 0);
        // Bias it toward the bottom (0.0 = top, 1.0 = bottom)
        set.setVerticalBias(card.getId(), 0.75f); // 75% down the screen
        set.applyTo(root);
        // Flip animation
        flipBtn.setOnClickListener(v -> card.flipCard());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/blackjack/" + joinCode;
        WebSocketManager.getInstance().connectWebSocket(wsUrl);
        WebSocketManager.getInstance().setWebSocketListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebSocketManager.getInstance().disconnectWebSocket();
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {

    }

    @Override
    public void onWebSocketMessage(String message) {

    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onWebSocketError(Exception ex) {

    }
}
