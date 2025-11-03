package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.BottomNavHelper;
import com.example.androidexample.services.VolleySingleton;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private static final String DB_URL = "http://coms-3090-006.class.las.iastate.edu:8080";

    private LinearLayout statsContainer;
    private ImageButton refreshBtn;
    private String username;

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

        findViewById(R.id.stats_blackjack_btn).setOnClickListener(v -> {
            currentGame = "Blackjack";
            showGameStats();
        });
        findViewById(R.id.stats_euchre_btn).setOnClickListener(v -> {
            currentGame = "Euchre";
            showGameStats();
        });
        findViewById(R.id.stats_gofish_btn).setOnClickListener(v -> {
            currentGame = "GoFish";
            showGameStats();
        });

        refreshBtn.setOnClickListener(v -> fetchStats());

        fetchStats(); // load on open
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
                                err -> {
                                    Toast.makeText(this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                                });

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

    /** creates a grey box for each stat line **/
    private void addTextBox(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getColor(android.R.color.white));
        tv.setTextSize(20);
        tv.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
        tv.setBackgroundColor(getColor(R.color.my_grey));
        tv.setPadding(20, 15, 20, 15);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(40, 12, 40, 0);
        tv.setLayoutParams(params);
        tv.setGravity(android.view.Gravity.CENTER);

        statsContainer.addView(tv);
    }
}
