package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    private Button deleteButton;
    private Button updateButton;
    private Button logoutButton;
    private TextView usernameDisplayText;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");

        // Setup bottom navigation bar
        BottomNavHelper.setupBottomNav(this, username);

        // Link UI elements
        updateButton = findViewById(R.id.profile_update_btn);
        deleteButton = findViewById(R.id.profile_delete_btn);
        logoutButton = findViewById(R.id.profile_logout_btn);
        usernameDisplayText = findViewById(R.id.profile_username_text);

        usernameDisplayText.setText(username);

        // --- Update Button ---
        updateButton.setOnClickListener(v -> {
            Intent i = new Intent(ProfileActivity.this, UpdateActivity.class);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        // --- Delete Button ---
        deleteButton.setOnClickListener(v -> deleteStats(username));

        // --- Logout Button ---
        logoutButton.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void deleteStats(String username) {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/AppUser/username/" + username;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int userID = response.getInt("userID");
                        deleteStatsFromID(userID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user: " + error);
                    Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void deleteStatsFromID(int userID) {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/UserStats/" + userID;

        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(this, "Deleted profile stats successfully!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(i);
                },
                error -> {
                    Toast.makeText(this, "Failed to delete user stats", Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }
}
