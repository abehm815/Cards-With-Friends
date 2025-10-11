package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class AdminActivity extends AppCompatActivity{

    private EditText usernameText;
    private EditText gameText;
    private EditText statToChangeText;
    private EditText newStatText;

    private Button statUpdateButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        usernameText = findViewById(R.id.admin_user_input);
        gameText = findViewById(R.id.admin_game_input);
        statToChangeText = findViewById(R.id.admin_stat_input);
        newStatText = findViewById(R.id.admin_new_stat_input);
        statUpdateButton = findViewById(R.id.admin_update_stats_button);

        statUpdateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                updateUserStat(usernameText.getText().toString(), gameText.getText().toString(),
                        statToChangeText.getText().toString(), newStatText.getText().toString());
            }
        });

    }

    private void updateUserStat(String user, String game, String statToChange, String newStat){


        String url = "http://coms-3090-006.class.las.iastate.edu:8080" + "/AppUser/username/" + user;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        ;
                        int userID = response.getInt("userID");;
                        updateUserStatFromID(userID, game, statToChange, newStat);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user: " + error.toString());
                    Toast.makeText(this, "Failed to fetch userID", Toast.LENGTH_SHORT).show();
                }
        );

        // Add to the request queue
        VolleySingleton.getInstance(this).addToRequestQueue(request);

    }

    private void updateUserStatFromID(int userID, String game, String statToChange, String newStat){
        String url = "http://coms-3090-006.class.las.iastate.edu:8080" + "/UserStats/" + userID + "/" + game + "/set/" + statToChange + "/" + newStat;


        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Update Failed", Toast.LENGTH_LONG).show();
                    }
                }
        );
        //Volley
        VolleySingleton.getInstance(AdminActivity.this).addToRequestQueue(postRequest);

    }



}
