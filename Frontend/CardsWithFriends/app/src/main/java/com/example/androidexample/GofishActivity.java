package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.androidexample.services.CardView;
import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GofishActivity extends AppCompatActivity implements WebSocketListener {

    private static final String[] CARD_VALUES = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "K", "Q"};
    private static final String[] SUIT_VALUES = {"S", "C", "H", "D"};

    private static final float CENTER_X_BIAS = 0.5f;
    private static final float CENTER_Y_BIAS = 0.75f;
    private static final float RADIUS_BIAS = 1.4f;
    private static final float TOTAL_SPREAD = 30f;

    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 200;
    private static final int INITIAL_CARD_COUNT = 7;

    private String gameType;
    private String username;
    private CardView selectedCard = null;
    private ConstraintLayout rootLayout;
    private List<CardView> cards;
    private String TAG = "yo ts a tag foo: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gofish);

        initializeViews();
        retrieveIntentData();
        initializeCards();
        setupCardLayout();
        animateCardsIntroduction();
        setupFlipButton();
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.rootLayout);
        cards = new ArrayList<>();
    }

    private void retrieveIntentData() {
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        username = "bigDon"; // for testing, will be dynamic later on
    }

    private void initializeCards() {
        Random rand = new Random(10);

        for (int i = 0; i < INITIAL_CARD_COUNT; i++) {
            CardView card = createCard(rand);
            rootLayout.addView(card);
            cards.add(card);
        }
    }

    private CardView createCard(Random rand) {
        CardView card = new CardView(this);
        card.setId(View.generateViewId());
        card.setLayoutParams(new ConstraintLayout.LayoutParams(CARD_WIDTH, CARD_HEIGHT));
        card.setClickable(true);
        card.setFocusable(true);

        int cardValueIndex = rand.nextInt(CARD_VALUES.length);
        int suitValueIndex = rand.nextInt(SUIT_VALUES.length);
        card.setCard(CARD_VALUES[cardValueIndex], SUIT_VALUES[suitValueIndex], true);

        return card;
    }

    private void setupCardLayout() {
        ConstraintSet set = new ConstraintSet();
        set.clone(rootLayout);
        applyArcConstraints(set, cards);
        set.applyTo(rootLayout);
    }

    private void applyArcConstraints(ConstraintSet set, List<CardView> cardList) {
        int n = cardList.size();
        if (n == 0) return;

        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            CardView card = cardList.get(i);
            int id = card.getId();

            // Connect card to parent edges
            set.connect(id, ConstraintSet.LEFT, rootLayout.getId(), ConstraintSet.LEFT, 0);
            set.connect(id, ConstraintSet.RIGHT, rootLayout.getId(), ConstraintSet.RIGHT, 0);
            set.connect(id, ConstraintSet.TOP, rootLayout.getId(), ConstraintSet.TOP, 0);
            set.connect(id, ConstraintSet.BOTTOM, rootLayout.getId(), ConstraintSet.BOTTOM, 0);

            // Calculate arc position
            float angle = startAngle + i * angleStep;
            double rad = Math.toRadians(angle);
            float xBias = (float) (CENTER_X_BIAS + RADIUS_BIAS * Math.sin(rad));
            float yBias = (float) (CENTER_Y_BIAS + RADIUS_BIAS * (1 - Math.cos(rad)));

            // Clamp values to valid range
            xBias = Math.max(0f, Math.min(1f, xBias));
            yBias = Math.max(0f, Math.min(1f, yBias));

            set.setHorizontalBias(id, xBias);
            set.setVerticalBias(id, yBias);
        }
    }

    private void animateCardsIntroduction() {
        int n = cards.size();
        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            final CardView card = cards.get(i);
            final float targetAngle = startAngle + i * angleStep;
            final int index = i;

            card.post(() -> {
                card.setPivotX(card.getWidth() / 2f);
                card.setPivotY(card.getHeight());
                card.setRotation(0f);

                card.animate()
                        .rotation(targetAngle)
                        .setStartDelay(index * 80L)
                        .setDuration(350)
                        .start();
            });
        }
    }

    private void setupFlipButton() {
        Button flipBtn = findViewById(R.id.flipButton);
        flipBtn.setOnClickListener(v -> removeFirstCard());
    }

    private void removeFirstCard() {
        if (cards.isEmpty()) return;

        CardView cardToDelete = cards.get(0);

        cardToDelete.animate()
                .alpha(0f)
                .translationY(-150f)
                .setDuration(250)
                .withEndAction(() -> {
                    rootLayout.removeView(cardToDelete);
                    cards.remove(cardToDelete);
                    selectedCard = null;
                    relayoutHand(cards, rootLayout);
                })
                .start();
    }

    private void relayoutHand(List<CardView> cardList, ConstraintLayout root) {
        int n = cardList.size();
        if (n == 0) return;

        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        applyArcConstraints(set, cardList);

        // Animate movement between constraints
        androidx.transition.TransitionManager.beginDelayedTransition(root);
        set.applyTo(root);

        // Animate rotation back into fan shape
        animateCardRotations(cardList);
    }

    private void animateCardRotations(List<CardView> cardList) {
        int n = cardList.size();
        float startAngle = -TOTAL_SPREAD / 2f;
        float angleStep = (n == 1) ? 0f : TOTAL_SPREAD / (n - 1);

        for (int i = 0; i < n; i++) {
            final CardView card = cardList.get(i);
            final float targetAngle = startAngle + i * angleStep;
            final int index = i;

            card.post(() -> {
                card.setPivotX(card.getWidth() / 2f);
                card.setPivotY(card.getHeight());
                card.animate()
                        .rotation(targetAngle)
                        .setStartDelay(index * 60L)
                        .setDuration(250)
                        .start();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Connect to your WebSocket that sends lobby updates
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/gofish";
        WebSocketManager.getInstance().connectWebSocket(wsUrl);
        WebSocketManager.getInstance().setWebSocketListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WebSocketManager.getInstance().disconnectWebSocket();
    }

    // ----------------------- WEBSOCKET CALLBACKS -----------------------

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        Log.d(TAG, "Connected to lobby WebSocket");
    }

    @Override
    public void onWebSocketMessage(String message) {
        Log.d(TAG, "Received message: " + message);

        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON", e);
            }
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        Log.d(TAG, "WebSocket closed: " + reason);
    }

    @Override
    public void onWebSocketError(Exception ex) {
        Log.e(TAG, "WebSocket error", ex);
    }
}