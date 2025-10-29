package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONObject;
import org.json.JSONException;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmEditText;
    private EditText firstnameEditText;
    private EditText lastnameEditText;
    private EditText emailEditText;
    private EditText ageEditText;

    private Button signupButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI elements
        firstnameEditText = findViewById(R.id.signup_firstname_edt);
        lastnameEditText = findViewById(R.id.signup_lastname_edt);
        emailEditText = findViewById(R.id.signup_email_edt);
        ageEditText = findViewById(R.id.signup_age_edt);
        usernameEditText = findViewById(R.id.signup_username_edt);
        passwordEditText = findViewById(R.id.signup_password_edt);
        confirmEditText = findViewById(R.id.signup_confirm_edt);
        signupButton = findViewById(R.id.signup_signup_btn);
        backButton = findViewById(R.id.signup_back_btn);

        // Handle signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = firstnameEditText.getText().toString().trim();
                String lastName = lastnameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String age = ageEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();
                String confirm = confirmEditText.getText().toString();

                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || age.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirm)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                    return;
                }

                sendSignup(firstName, lastName, email, age, username, password);
            }
        });

        // Handle back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    // Sends signup info to backend
    private void sendSignup(String firstName, String lastName, String email, String age, String username, String password) {
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

        String url = "http://coms-3090-006.class.las.iastate.edu:8080/AppUser";

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(), "Signup successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Signup Failed", Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(SignupActivity.this).addToRequestQueue(postRequest);
    }
}
