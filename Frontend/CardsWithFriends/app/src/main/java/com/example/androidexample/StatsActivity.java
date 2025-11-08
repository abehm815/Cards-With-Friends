package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.BottomNavHelper;
import com.example.androidexample.services.VolleySingleton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private static final String DB_URL = "http://coms-3090-006.class.las.iastate.edu:8080";

    private LinearLayout statsContainer;
    private ImageButton refreshBtn;
    private String username;

    private TextView blackjackBtn, euchreBtn, gofishBtn;

    // cache
    private JSONObject cachedStats = null;
    private String currentGame = "Blackjack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        BottomNavHelper.setupBottomNav(this, username);

        statsContainer = findViewById(R.id.stats_text_container);
        refreshBtn = findViewById(R.id.refresh_button);

        blackjackBtn = findViewById(R.id.stats_blackjack_btn);
        euchreBtn = findViewById(R.id.stats_euchre_btn);
        gofishBtn = findViewById(R.id.stats_gofish_btn);

        blackjackBtn.setOnClickListener(v -> selectGame("Blackjack"));
        euchreBtn.setOnClickListener(v -> selectGame("Euchre"));
        gofishBtn.setOnClickListener(v -> selectGame("GoFish"));

        refreshBtn.setOnClickListener(v -> fetchStats());

        fetchStats(); // load on open
        updateButtonStyles(); // make sure default highlights properly
    }

    private void selectGame(String game) {
        if (game.equals(currentGame)) return; // avoid unnecessary reloads
        currentGame = game;
        updateButtonStyles();

        // fade content during switch
        statsContainer.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            showGameStats();
            statsContainer.animate().alpha(1f).setDuration(150).start();
        }).start();
    }

    /** Visually updates which game is active **/
    private void updateButtonStyles() {
        blackjackBtn.setBackgroundResource(R.drawable.btn_dark);
        euchreBtn.setBackgroundResource(R.drawable.btn_dark);
        gofishBtn.setBackgroundResource(R.drawable.btn_dark);

        switch (currentGame) {
            case "Blackjack":
                blackjackBtn.setBackgroundResource(R.drawable.btn_red);
                break;
            case "Euchre":
                euchreBtn.setBackgroundResource(R.drawable.btn_blue);
                break;
            case "GoFish":
                gofishBtn.setBackgroundResource(R.drawable.btn_green);
                break;
        }
    }

    private void fetchStats() {
        String url = DB_URL + "/AppUser/username/" + username;

        JsonObjectRequest userReq = new JsonObjectRequest(
                Request.Method.GET, url, null,
                userRes -> {
                    try {
                        int userID = userRes.getInt("userID");
                        String statsUrl = DB_URL + "/UserStats/" + userID;

                        JsonObjectRequest statsReq = new JsonObjectRequest(
                                Request.Method.GET, statsUrl, null,
                                statsRes -> {
                                    cachedStats = statsRes;
                                    showGameStats();
                                },
                                err -> Toast.makeText(this, "Failed to load stats", Toast.LENGTH_SHORT).show()
                        );

                        VolleySingleton.getInstance(this).addToRequestQueue(statsReq);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing user info", Toast.LENGTH_SHORT).show();
                    }
                },
                err -> Toast.makeText(this, "Failed to fetch user", Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(this).addToRequestQueue(userReq);
    }

    private void showGameStats() {
        statsContainer.removeAllViews();
        if (cachedStats == null) {
            addTextBox("No stats loaded yet.");
            return;
        }

        try {
            JSONObject allGameStats = cachedStats.getJSONObject("allGameStats");
            JSONObject gameStats = allGameStats.optJSONObject(currentGame);
            if (gameStats == null) {
                addTextBox("No data for " + currentGame);
                return;
            }

            List<String> lines = new ArrayList<>();
            switch (currentGame) {
                case "Blackjack":
                    lines.add("Games played: " + gameStats.optInt("gamesPlayed"));
                    lines.add("Money won: " + gameStats.optInt("moneyWon"));
                    lines.add("Bets won: " + gameStats.optInt("betsWon"));
                    lines.add("Times doubled down: " + gameStats.optInt("timesDoubledDown"));
                    lines.add("Times split: " + gameStats.optInt("timesSplit"));
                    lines.add("Times hit: " + gameStats.optInt("timesHit"));
                    lines.add("Games won: " + gameStats.optInt("gamesWon"));
                    break;
                case "Euchre":
                    lines.add("Games played: " + gameStats.optInt("gamesPlayed"));
                    lines.add("Games won: " + gameStats.optInt("gamesWon"));
                    lines.add("Tricks taken: " + gameStats.optInt("tricksTaken"));
                    lines.add("Times picked up: " + gameStats.optInt("timesPickedUp"));
                    lines.add("Times gone alone: " + gameStats.optInt("timesGoneAlone"));
                    lines.add("Sweeps won: " + gameStats.optInt("sweepsWon"));
                    break;
                case "GoFish":
                    lines.add("Games played: " + gameStats.optInt("gamesPlayed"));
                    lines.add("Times went fishing: " + gameStats.optInt("timesWentFishing"));
                    lines.add("Questions asked: " + gameStats.optInt("questionsAsked"));
                    lines.add("Books collected: " + gameStats.optInt("booksCollected"));
                    lines.add("Games won: " + gameStats.optInt("gamesWon"));
                    break;
            }

            for (String line : lines) addTextBox(line);

        } catch (Exception e) {
            Log.e("Stats", "Error showing stats", e);
            addTextBox("Error showing stats");
        }
    }

    /** Creates a grey box for each stat line **/
    private void addTextBox(String text) {
        int accent;
        switch (currentGame) {
            case "Blackjack":
                accent = getColor(R.color.my_red);
                break;
            case "Euchre":
                accent = getColor(R.color.my_blue);
                break;
            case "GoFish":
            default:
                accent = getColor(R.color.my_green);
                break;
        }

        View cardView = getLayoutInflater().inflate(R.layout.item_stat_card, statsContainer, false);
        MaterialCardView card = (MaterialCardView) cardView;
        card.setStrokeColor(accent);

        ImageView icon = card.findViewById(R.id.stat_icon);
        icon.setColorFilter(accent);

        TextView tv = card.findViewById(R.id.stat_text);
        tv.setText(text);

        card.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        statsContainer.addView(card);
    }
}
