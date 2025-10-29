package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.BottomNavHelper;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONObject;

public class StatsActivity extends AppCompatActivity {

    private Button euchreButton;
    private Button blackJackButton;
    private Button goFishButton;

    private TextView stat1, stat2, stat3, stat4, stat5, stat6, stat7;

    private static final String DB_URL = "http://coms-3090-006.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // --- Get username from intent ---
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        // --- Setup bottom nav ---
        BottomNavHelper.setupBottomNav(this, username);

        // --- Bind UI elements ---
        euchreButton = findViewById(R.id.stats_euchre_btn);
        blackJackButton = findViewById(R.id.stats_black_jack_btn);
        goFishButton = findViewById(R.id.stats_go_fish_btn);

        stat1 = findViewById(R.id.stat_1_text);
        stat2 = findViewById(R.id.stat_2_text);
        stat3 = findViewById(R.id.stat_3_text);
        stat4 = findViewById(R.id.stat_4_text);
        stat5 = findViewById(R.id.stat_5_text);
        stat6 = findViewById(R.id.stat_6_text);
        stat7 = findViewById(R.id.stat_7_text);

        // --- Button Listeners ---
        euchreButton.setOnClickListener(v -> {
            clearStats();
            stat1.setText("Loading Euchre stats...");
            getUserID(username, "Euchre");
        });

        blackJackButton.setOnClickListener(v -> {
            clearStats();
            stat1.setText("Loading Blackjack stats...");
            getUserID(username, "Blackjack");
        });

        goFishButton.setOnClickListener(v -> {
            clearStats();
            stat1.setText("Loading Go Fish stats...");
            getUserID(username, "GoFish");
        });
    }

    /** Clears all stat text fields before updating new stats. */
    private void clearStats() {
        stat1.setText("");
        stat2.setText("");
        stat3.setText("");
        stat4.setText("");
        stat5.setText("");
        stat6.setText("");
        stat7.setText("");
    }

    /** Gets the user ID from username, then fetches the corresponding stats. */
    private void getUserID(String username, String game) {
        String url = DB_URL + "/AppUser/username/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int userID = response.getInt("userID");
                        getUserStats(userID, game);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user: " + error);
                    Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /** Fetches user stats for the given game and user ID. */
    private void getUserStats(int userID, String game) {
        String url = DB_URL + "/UserStats/" + userID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject allStats = response.getJSONObject("allGameStats");
                        JSONObject gameStats = allStats.getJSONObject(game);

                        switch (game) {
                            case "GoFish":
                                populateGoFishStats(gameStats);
                                break;
                            case "Euchre":
                                populateEuchreStats(gameStats);
                                break;
                            case "Blackjack":
                                populateBlackjackStats(gameStats);
                                break;
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Error reading stats", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user stats: " + error);
                    Toast.makeText(this, "Cannot fetch stats right now", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    // --- Individual game stat renderers ---

    private void populateGoFishStats(JSONObject stats) throws Exception {
        stat1.setText("Games played: " + stats.getInt("gamesPlayed"));
        stat2.setText("Times went fishing: " + stats.getInt("timesWentFishing"));
        stat3.setText("Questions asked: " + stats.getInt("questionsAsked"));
        stat4.setText("Books collected: " + stats.getInt("booksCollected"));
        stat5.setText("Games won: " + stats.getInt("gamesWon"));
        stat6.setText("");
        stat7.setText("");
    }

    private void populateEuchreStats(JSONObject stats) throws Exception {
        stat1.setText("Games played: " + stats.getInt("gamesPlayed"));
        stat2.setText("Games won: " + stats.getInt("gamesWon"));
        stat3.setText("Tricks taken: " + stats.getInt("tricksTaken"));
        stat4.setText("Times picked up: " + stats.getInt("timesPickedUp"));
        stat5.setText("Times gone alone: " + stats.getInt("timesGoneAlone"));
        stat6.setText("Sweeps won: " + stats.getInt("sweepsWon"));
        stat7.setText("");
    }

    private void populateBlackjackStats(JSONObject stats) throws Exception {
        stat1.setText("Games played: " + stats.getInt("gamesPlayed"));
        stat2.setText("Money won: " + stats.getInt("moneyWon"));
        stat3.setText("Bets won: " + stats.getInt("betsWon"));
        stat4.setText("Times doubled down: " + stats.getInt("timesDoubledDown"));
        stat5.setText("Times split: " + stats.getInt("timesSplit"));
        stat6.setText("Times hit: " + stats.getInt("timesHit"));
        stat7.setText("Games won: " + stats.getInt("gamesWon"));
    }
}
