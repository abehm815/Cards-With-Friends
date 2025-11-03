package com.example.androidexample;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.androidexample.services.BottomNavHelper;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String DB_URL = "http://coms-3090-006.class.las.iastate.edu:8080";

    private String username;
    private boolean dataLoaded = false;

    private final List<PlayerStat> blackjackList = new ArrayList<>();
    private final List<PlayerStat> euchreList = new ArrayList<>();
    private final List<PlayerStat> gofishList = new ArrayList<>();

    private LinearLayout leaderboardContainer;
    private View blackjackBtn, euchreBtn, gofishBtn;
    private ImageButton refreshBtn;

    private String currentGame = "Blackjack";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        BottomNavHelper.setupBottomNav(this, username);

        leaderboardContainer = findViewById(R.id.leaderboard_container);
        blackjackBtn = findViewById(R.id.leaderboard_blackjack_btn);
        euchreBtn = findViewById(R.id.leaderboard_euchre_btn);
        gofishBtn = findViewById(R.id.leaderboard_gofish_btn);
        refreshBtn = findViewById(R.id.refresh_button);

        blackjackBtn.setOnClickListener(v -> {
            currentGame = "Blackjack";
            displayLeaderboard(blackjackList);
        });

        euchreBtn.setOnClickListener(v -> {
            currentGame = "Euchre";
            displayLeaderboard(euchreList);
        });

        gofishBtn.setOnClickListener(v -> {
            currentGame = "Go Fish";
            displayLeaderboard(gofishList);
        });

        refreshBtn.setOnClickListener(v -> {
            getUserStats();
        });

        // Load stats once initially
        getUserStats();
    }

    private void getUserStats() {
        String url = DB_URL + "/UserStats";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                this::handleResponse,
                error -> {
                    Log.e("Volley", "Error fetching user stats: " + error);
                    Toast.makeText(this, "Cannot fetch stats right now", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleResponse(JSONArray response) {
        try {
            blackjackList.clear();
            euchreList.clear();
            gofishList.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                JSONObject userObj = obj.optJSONObject("appUser");
                if (userObj == null) continue;

                String name = userObj.optString("username", "Unknown");
                JSONObject stats = obj.optJSONObject("allGameStats");
                if (stats == null) continue;

                JSONObject bj = stats.optJSONObject("Blackjack");
                JSONObject eu = stats.optJSONObject("Euchre");
                JSONObject gf = stats.optJSONObject("GoFish");

                blackjackList.add(new PlayerStat(name, calcWinPct(bj)));
                euchreList.add(new PlayerStat(name, calcWinPct(eu)));
                gofishList.add(new PlayerStat(name, calcWinPct(gf)));
            }

            blackjackList.sort((a, b) -> Double.compare(b.winPct, a.winPct));
            euchreList.sort((a, b) -> Double.compare(b.winPct, a.winPct));
            gofishList.sort((a, b) -> Double.compare(b.winPct, a.winPct));

            dataLoaded = true;
            displayLeaderboard(getCurrentList());

        } catch (Exception e) {
            Log.e("Volley", "Parse error", e);
            Toast.makeText(this, "Error parsing leaderboard", Toast.LENGTH_SHORT).show();
        }
    }

    private List<PlayerStat> getCurrentList() {
        switch (currentGame) {
            case "Euchre":
                return euchreList;
            case "Go Fish":
                return gofishList;
            default:
                return blackjackList;
        }
    }

    private double calcWinPct(JSONObject obj) {
        if (obj == null) return 0;
        int played = obj.optInt("gamesPlayed", 0);
        int won = obj.optInt("gamesWon", 0);
        return played > 0 ? (double) won / played * 100 : 0;
    }

    /** Builds and displays leaderboard entries dynamically **/
    private void displayLeaderboard(List<PlayerStat> list) {
        leaderboardContainer.removeAllViews();

        if (!dataLoaded || list.isEmpty()) {
            TextView noData = new TextView(this);
            noData.setTextColor(getColor(android.R.color.white));
            noData.setTextSize(18);
            noData.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
            noData.setText("No stats available.");
            leaderboardContainer.addView(noData);
            return;
        }

        int rank = 1;
        for (PlayerStat p : list) {
            // Create outer block
            LinearLayout block = new LinearLayout(this);
            block.setOrientation(LinearLayout.HORIZONTAL);
            block.setPadding(30, 25, 30, 25);
            block.setGravity(Gravity.CENTER_VERTICAL);

            // Light grey rounded background
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(ContextCompat.getColor(this, R.color.my_grey));
            bg.setCornerRadius(25);
            block.setBackground(bg);

            LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            blockParams.setMargins(0, 12, 0, 12);
            block.setLayoutParams(blockParams);

            // Rank circle
            TextView rankCircle = new TextView(this);
            rankCircle.setText(String.valueOf(rank));
            rankCircle.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            rankCircle.setTextSize(18);
            rankCircle.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
            rankCircle.setGravity(Gravity.CENTER);

            GradientDrawable circleBg = new GradientDrawable();
            circleBg.setShape(GradientDrawable.OVAL);
            circleBg.setColor(ContextCompat.getColor(this, android.R.color.white));
            circleBg.setSize(80, 80);
            rankCircle.setBackground(circleBg);

            LinearLayout.LayoutParams circleParams = new LinearLayout.LayoutParams(80, 80);
            circleParams.setMargins(0, 0, 30, 0);
            rankCircle.setLayoutParams(circleParams);

            // Username
            TextView nameView = new TextView(this);
            nameView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
            nameView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            nameView.setTextSize(20);
            nameView.setText(p.username);

            // Spacer
            View spacer = new View(this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
            );
            spacer.setLayoutParams(spacerParams);

            // Score
            TextView scoreView = new TextView(this);
            scoreView.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
            scoreView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            scoreView.setTextSize(20);
            scoreView.setText(String.format("%.2f%%", p.winPct));

            block.addView(rankCircle);
            block.addView(nameView);
            block.addView(spacer);
            block.addView(scoreView);

            leaderboardContainer.addView(block);
            rank++;
        }
    }

    public static class PlayerStat {
        String username;
        double winPct;

        public PlayerStat(String username, double winPct) {
            this.username = username;
            this.winPct = winPct;
        }
    }
}
