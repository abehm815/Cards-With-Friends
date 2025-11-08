package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.card.MaterialCardView;

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
    private TextView blackjackBtn, euchreBtn, gofishBtn;
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

        // Button click listeners
        blackjackBtn.setOnClickListener(v -> {
            currentGame = "Blackjack";
            displayLeaderboard(blackjackList);
            setActiveButton(currentGame);
        });

        euchreBtn.setOnClickListener(v -> {
            currentGame = "Euchre";
            displayLeaderboard(euchreList);
            setActiveButton(currentGame);
        });

        gofishBtn.setOnClickListener(v -> {
            currentGame = "Go Fish";
            displayLeaderboard(gofishList);
            setActiveButton(currentGame);
        });

        refreshBtn.setOnClickListener(v -> getUserStats());

        getUserStats(); // load on open
        setActiveButton(currentGame); // default highlight
    }

    private void setActiveButton(String activeGame) {
        // Reset all buttons to dark
        blackjackBtn.setBackgroundResource(R.drawable.btn_dark);
        euchreBtn.setBackgroundResource(R.drawable.btn_dark);
        gofishBtn.setBackgroundResource(R.drawable.btn_dark);

        // Highlight selected one
        switch (activeGame) {
            case "Blackjack":
                blackjackBtn.setBackgroundResource(R.drawable.btn_red);
                break;
            case "Euchre":
                euchreBtn.setBackgroundResource(R.drawable.btn_blue);
                break;
            case "Go Fish":
                gofishBtn.setBackgroundResource(R.drawable.btn_green);
                break;
        }
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
            setActiveButton(currentGame);

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

        // pick border color based on game type
        int borderColor;
        switch (currentGame) {
            case "Euchre":
                borderColor = ContextCompat.getColor(this, R.color.my_blue);
                break;
            case "Go Fish":
                borderColor = ContextCompat.getColor(this, R.color.my_green);
                break;
            default:
                borderColor = ContextCompat.getColor(this, R.color.my_red);
                break;
        }

        int rank = 1;
        for (PlayerStat p : list) {
            View card = getLayoutInflater().inflate(R.layout.item_leaderboard_entry, leaderboardContainer, false);
            MaterialCardView cardView = (MaterialCardView) card;
            cardView.setStrokeColor(borderColor);

            TextView rankText = card.findViewById(R.id.rank_text);
            TextView usernameText = card.findViewById(R.id.username_text);
            TextView scoreText = card.findViewById(R.id.score_text);

            rankText.setText(String.valueOf(rank));
            usernameText.setText(p.username);
            scoreText.setText(String.format("%.2f%%", p.winPct));

            // Gradient backgrounds
            if (rank == 1) {
                rankText.setBackgroundResource(R.drawable.bg_rank_gold);
            } else if (rank == 2) {
                rankText.setBackgroundResource(R.drawable.bg_rank_silver);
            } else if (rank == 3) {
                rankText.setBackgroundResource(R.drawable.bg_rank_bronze);
            } else {
                rankText.setBackgroundResource(R.drawable.bg_rank_gradient);
            }
            rankText.setTextColor(ContextCompat.getColor(this, android.R.color.white));

            leaderboardContainer.addView(card);
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
