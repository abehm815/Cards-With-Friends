package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

    private Button backButton;

    private Button deleteButton;

    private Button updateButton;

    private TextView usernameDisplayText;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        updateButton = findViewById(R.id.profile_update_btn);
        backButton = findViewById(R.id.profile_back_btn);
        deleteButton = findViewById(R.id.profile_delete_btn);
        usernameDisplayText = findViewById(R.id.profile_username_text);
        usernameDisplayText.setText(username);

        View.OnClickListener backButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
            }
        };

        View.OnClickListener deleteButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://coms-3090-006.class.las.iastate.edu:8080/AppUser/username/" + username ;

                JsonObjectRequest deleteRequest = new JsonObjectRequest(
                        Request.Method.DELETE,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(getApplicationContext(),
                                        "Deleted profile successfully!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(),
                                        "Failed to delete user", Toast.LENGTH_LONG).show();
                            }
                        });
                VolleySingleton.getInstance(ProfileActivity.this).addToRequestQueue(deleteRequest);
            }
        };

        View.OnClickListener updateButtonListener= new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, UpdateActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        };

        updateButton.setOnClickListener(updateButtonListener);
        backButton.setOnClickListener(backButtonListener);
        deleteButton.setOnClickListener(deleteButtonListener);
    }
}
