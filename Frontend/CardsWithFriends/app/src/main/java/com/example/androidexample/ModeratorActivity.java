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


public class ModeratorActivity extends AppCompatActivity{

    private EditText usernameText;
    private Button statCreateButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator);

        usernameText = findViewById(R.id.moderator_user_input);
        statCreateButton = findViewById(R.id.moderator_update_stats_button);

        statCreateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                createUserStats(usernameText.getText().toString());
            }
        });

    }

    private void createUserStats(String user){


        String url = "http://coms-3090-006.class.las.iastate.edu:8080" + "/AppUser/username/" + user;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        ;
                        int userID = response.getInt("userID");;
                        createUserStatFromID(userID);
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

    private void createUserStatFromID(int userID){
        String url = "http://coms-3090-006.class.las.iastate.edu:8080" + "/UserStats/" + userID;


        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST,
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
        VolleySingleton.getInstance(ModeratorActivity.this).addToRequestQueue(postRequest);

    }



}
