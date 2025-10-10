package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

public class StatsActivity extends AppCompatActivity {

    private Button backButton;
    private Button euchreButton;
    private Button blackJackButton;
    private Button goFishButton;

    private TextView usernameText;

    private TextView stat1;
    private TextView stat2;
    private TextView stat3;
    private TextView stat4;
    private TextView stat5;
    private TextView stat6;
    private TextView stat7;



    private String dbURL = "http://coms-3090-006.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        backButton = findViewById(R.id.stats_back_btn);
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
        usernameText = findViewById(R.id.stats_username_text);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        usernameText.setText(username + " stats");

        View.OnClickListener backButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StatsActivity.this, HomeActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        };

        View.OnClickListener euchreButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserID(username, "Euchre");
                stat4.setText("Euchre stats to be loaded");
                stat1.setText("");
                stat2.setText("");
                stat3.setText("");
                stat5.setText("");
                stat6.setText("");
                stat7.setText("");
            }
        };

        View.OnClickListener blackJackButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserID(username, "Blackjack");
                stat4.setText("Black Jack stats to be loaded");
                stat1.setText("");
                stat2.setText("");
                stat3.setText("");
                stat5.setText("");
                stat6.setText("");
                stat7.setText("");
            }
        };

        View.OnClickListener goFishButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUserID(username, "GoFish");
                stat4.setText("Go Fish stats to be loaded");
                stat1.setText("");
                stat2.setText("");
                stat3.setText("");
                stat5.setText("");
                stat6.setText("");
                stat7.setText("");
            }
        };

        backButton.setOnClickListener(backButtonListener);
        euchreButton.setOnClickListener(euchreButtonListener);
        blackJackButton.setOnClickListener(blackJackButtonListener);
        goFishButton.setOnClickListener(goFishButtonListener);
    }


    private void getUserID(String username, String game){
        String url = dbURL + "/AppUser/username/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String user = response.getString("username");
                        int userID = response.getInt("userID");;
                        getUserStats(userID, game);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user: " + error.toString());
                    Toast.makeText(this, "User or pass incorrect", Toast.LENGTH_SHORT).show();
                }
        );

        // Add to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void getUserStats(int userID, String game){
        String url = dbURL + "/UserStats/" + userID;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {

                        if(game.equals("GoFish")){
                            JSONObject allGameStats = response.getJSONObject("allGameStats");
                            JSONObject goFishStats = allGameStats.getJSONObject(game);

                            int gamesPlayed = goFishStats.getInt("gamesPlayed");
                            int timesWentFishing = goFishStats.getInt("timesWentFishing");
                            int questionsAsked = goFishStats.getInt("questionsAsked");
                            int booksCollected = goFishStats.getInt("booksCollected");
                            int gamesWon = goFishStats.getInt("gamesWon");

                            stat1.setText("Games played: " + gamesPlayed);
                            stat2.setText("Times went fishing: " + timesWentFishing);
                            stat3.setText("Questions asked: " + questionsAsked);
                            stat4.setText("Books colllected: " + booksCollected);
                            stat5.setText("Games won: " + gamesWon);
                            stat6.setText("");
                            stat7.setText("");
                        }
                        if(game.equals("Euchre")){
                            JSONObject allGameStats = response.getJSONObject("allGameStats");
                            JSONObject euchreStats = allGameStats.getJSONObject(game);

                            int gamesPlayed = euchreStats.getInt("gamesPlayed");
                            int gamesWon = euchreStats.getInt("gamesWon");
                            int tricksTaken = euchreStats.getInt("tricksTaken");
                            int timesPickedUp = euchreStats.getInt("timesPickedUp");
                            int timesGoneAlone = euchreStats.getInt("timesGoneAlone");
                            int sweepsWon = euchreStats.getInt("sweepsWon");

                            stat1.setText("Games played: " + gamesPlayed);
                            stat2.setText("Games won: " + gamesWon);
                            stat3.setText("Tricks taken: " + tricksTaken);
                            stat4.setText("Times picked up: " + timesPickedUp);
                            stat5.setText("Times gone alone: " + timesGoneAlone);
                            stat6.setText("Sweeps won: " + sweepsWon);
                            stat7.setText("");
                        }
                        if(game.equals("Blackjack")){
                            JSONObject allGameStats = response.getJSONObject("allGameStats");
                            JSONObject blackjackStats = allGameStats.getJSONObject(game);

                            int gamesPlayed = blackjackStats.getInt("gamesPlayed");
                            int moneyWon = blackjackStats.getInt("moneyWon");
                            int betsWon = blackjackStats.getInt("betsWon");
                            int timesDoubledDown = blackjackStats.getInt("timesDoubledDown");
                            int timesSplit = blackjackStats.getInt("timesSplit");
                            int timesHit = blackjackStats.getInt("timesHit");
                            int gamesWon = blackjackStats.getInt("gamesWon");

                            stat1.setText("Games played: " + gamesPlayed);
                            stat2.setText("Money won: " + moneyWon);
                            stat3.setText("Bets won: " + betsWon);
                            stat4.setText("Times doubled down: " + timesDoubledDown);
                            stat5.setText("Times split: " + timesSplit);
                            stat6.setText("Times hit: " + timesHit);
                            stat7.setText("Games won: " + gamesWon);
                        }




                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user stats: " + error.toString());
                    Toast.makeText(this, "Cannot fetch stats right now", Toast.LENGTH_SHORT).show();
                }
        );

        // Add to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }
}