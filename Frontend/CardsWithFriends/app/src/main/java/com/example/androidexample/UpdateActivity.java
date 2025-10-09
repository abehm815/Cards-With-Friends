package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;
import org.json.JSONException;


public class UpdateActivity extends AppCompatActivity{

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private EditText firstnameEditText;
    private EditText lastnameEditText;
    private EditText emailEditText;
    private EditText ageEditText;
    private Button updateButton;
    private Button backButton;

    private String username;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        firstnameEditText = findViewById(R.id.update_firstname_edt);
        lastnameEditText = findViewById(R.id.update_lastname_edt);
        emailEditText = findViewById(R.id.update_email_edt);
        ageEditText = findViewById(R.id.update_age_edt);
        usernameEditText = findViewById(R.id.update_username_edt);
        passwordEditText = findViewById(R.id.update_password_edt);
        confirmEditText = findViewById(R.id.update_confirm_edt);
        updateButton = findViewById(R.id.update_update_btn);
        backButton = findViewById(R.id.update_back_btn);

        Intent incomingIntent = getIntent();
        username = incomingIntent.getStringExtra("USERNAME");

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstnameEditText.getText().toString().trim();
                String lastName = lastnameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String age = ageEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                String confirm = confirmEditText.getText().toString();



                if (!password.equals(confirm)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                    return;
                }

                sendUpdate(firstName, lastName, email, age, username, password);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateActivity.this, ProfileActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

    }

    private void sendUpdate(String firstName, String lastName, String email, String age, String username, String password) {
        JSONObject body = new JSONObject();
        try {
            body.put("firstName", firstName);
            body.put("lastName", lastName);
            body.put("email", email);
            body.put("age", age);
            body.put("username", username);
            body.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error Creating Request", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("TEST FN: ",firstName);
        Log.d("TEST LN: ", lastName);
        Log.d("TEST E: ", email);
        Log.d("TEST A: ", age);   //Testing bullshit
        Log.d("TEST UN: ", username);
        Log.d("TEST PW: ", password);


        String url = "http://coms-3090-006.class.las.iastate.edu:8080/AppUser/username/" + username;

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Update successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateActivity.this, ProfileActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                        finish();
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
        VolleySingleton.getInstance(UpdateActivity.this).addToRequestQueue(postRequest);
    }


}
