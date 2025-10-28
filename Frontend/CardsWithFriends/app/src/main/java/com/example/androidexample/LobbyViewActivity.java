package com.example.androidexample;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LobbyViewActivity extends AppCompatActivity {

    private String gameType;
    private String joinCode;
    private String username;
    private TextView gameTypeTxt;
    private TextView joinCodeTxt;
    private LinearLayout userListLayout;
    private Button deleteBtn;
    private Button leaveBtn;

    private static final String TAG = "LobbyViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobbyview);

        //Link all the UI elements
        gameTypeTxt = findViewById(R.id.lobbyview_type_txt);
        joinCodeTxt = findViewById(R.id.lobbyview_code_txt);
        userListLayout = findViewById(R.id.lobbyview_user_list);
        deleteBtn = findViewById(R.id.lobbyview_delete_btn);
        leaveBtn = findViewById(R.id.lobbyview_leave_btn);

        //Grab info from previous page
        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");

        //Set join code and game type text
        gameTypeTxt.setText(gameType);
        joinCodeTxt.setText(joinCode);

        //Run a GET request to get the info of the current lobby we are in
        getLobbyRequest();

        //Handle delete button
        deleteBtn.setOnClickListener(v -> deleteLobbyRequest());

        leaveBtn.setOnClickListener(v -> leaveLobbyRequest());
    }
    /**
     *
     * This class handles getting the lobby information (Specifically the other usernames in the
     * lobby and displays everyone's name as cards in the app
     *
     * **/
    private void getLobbyRequest() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + joinCode;

        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray usersArray = response.getJSONArray("users");

                        userListLayout.removeAllViews();

                        TextView countText = new TextView(LobbyViewActivity.this);
                        countText.setText("Players in Lobby: " + usersArray.length());
                        countText.setTextColor(Color.WHITE);
                        countText.setTextSize(20);
                        countText.setTypeface(Typeface.create(ResourcesCompat.getFont(LobbyViewActivity.this, R.font.inter_bold), Typeface.BOLD));
                        countText.setGravity(Gravity.CENTER);
                        countText.setPadding(0, 8, 0, 24);
                        userListLayout.addView(countText);

                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userObj = usersArray.getJSONObject(i);
                            String username = userObj.getString("username");

                            MaterialCardView card = new MaterialCardView(LobbyViewActivity.this);
                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            cardParams.setMargins(0, 8, 0, 8);
                            card.setLayoutParams(cardParams);
                            card.setRadius(20f);
                            card.setCardElevation(8f);
                            card.setCardBackgroundColor(getColor(android.R.color.white));

                            TextView userText = new TextView(LobbyViewActivity.this);
                            userText.setText(username);
                            userText.setTextColor(getColor(android.R.color.black));
                            userText.setTextSize(22);
                            userText.setTypeface(Typeface.create(ResourcesCompat.getFont(LobbyViewActivity.this, R.font.inter_bold), Typeface.BOLD));
                            userText.setPadding(0, 32, 0, 32);
                            userText.setGravity(Gravity.CENTER);

                            card.addView(userText);
                            userListLayout.addView(card);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing lobby JSON", e);
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Failed to get lobby information", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error: ", error);
                }
        );

        VolleySingleton.getInstance(LobbyViewActivity.this).addToRequestQueue(getRequest);
    }
    /**
     *
     * This class deletes the current lobby we are in and sends the user back to the join code page
     *
     * **/
    private void deleteLobbyRequest() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + joinCode;

        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(getApplicationContext(), "Deleted Lobby!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LobbyViewActivity.this, JoinActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("GAMETYPE", gameType);
                    startActivity(intent);
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Failed to delete lobby", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error: ", error);
                }
        );

        VolleySingleton.getInstance(LobbyViewActivity.this).addToRequestQueue(deleteRequest);
    }

    private void leaveLobbyRequest() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + joinCode + "/" + username;
        Log.d("ENDPOINT URL", url);
        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            if (message.equals("success")){
                                Toast.makeText(getApplicationContext(), "Lobby left!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LobbyViewActivity.this, JoinActivity.class);
                                intent.putExtra("USERNAME", username);
                                intent.putExtra("GAMETYPE", gameType);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Can't leave lobby", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Lobby Join Failed", Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(LobbyViewActivity.this).addToRequestQueue(postRequest);

    }}
