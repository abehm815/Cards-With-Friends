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
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.VolleySingleton;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable

    private Button loginButton;

    private Button backButton;

    String dbURL = "http://coms-3090-006.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML

        /* initialize UI elements */

        usernameEditText = findViewById(R.id.login_username_edt); //Username text field
        passwordEditText = findViewById(R.id.login_password_edt); //Password text field
        loginButton = findViewById(R.id.login_login_btn);    //Login button
        backButton = findViewById(R.id.login_back_btn);

        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if(username.equals("admin") && password.equals("admin")){
                    Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                    startActivity(intent);
                }
                if(username.equals("mod") && password.equals("mod")){
                    Intent intent = new Intent(LoginActivity.this, ModeratorActivity.class);
                    startActivity(intent);
                }
                //TODO: Remove temporary
                if(username.equals("offline") && password.equals("1")){
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USERNAME", "offline");
                    startActivity(intent);
                }
                else {
                    getUserFromBackend(username, password);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }



    private void getUserFromBackend(String username, String password) {

        String url = "http://coms-3090-006.class.las.iastate.edu:8080/AppUser/username/" + username;
        Log.d("URL user: ",username);
        Log.d("URL:",url);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String user = response.getString("username");
                        String pass = response.getString("password");
                        if(username.equals(user) && password.equals(pass)){
                            Toast.makeText(this, "Logging in: " + user, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("USERNAME", username);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this,"User or pass incorrect", Toast.LENGTH_SHORT).show();
                        }

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
}