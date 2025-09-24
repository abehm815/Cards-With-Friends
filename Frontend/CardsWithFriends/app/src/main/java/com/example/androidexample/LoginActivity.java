package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable

    private Button loginButton;

    private Button backButton;


    private HashMap<String, String> validUsers = new HashMap<String, String>() {{
        put("alex", "alex1234");
        put("jake", "jake1234");
        put("eli", "eli1234");
        put("colton", "colton1234");
    }};

    private HashMap<String, String> validModeratorUsers = new HashMap<String, String>() {{
        put("alexM", "alex1234");
        put("jakeM", "jake1234");
        put("eliM", "eli1234");
        put("coltonM", "colton1234");
    }}; //Hardcoded list of users (Would grab from backend when complete)

    private HashMap<String, String> validAdminUsers = new HashMap<String, String>(){{
       put("alexA","alex1234");
       put("jakeA", "jake1234");
       put("eliA", "eli1234");
       put("coltonA", "colton1234");
    }};

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

                // Verify the user login attempt matches an account in our mock database
                boolean isValidUser = validUsers.containsKey(username) && validUsers.get(username).equals(password);
                boolean isValidModeratorUser = validModeratorUsers.containsKey(username) && validModeratorUsers.get(username).equals(password);
                boolean isValidAdminUser = validAdminUsers.containsKey(username) && validAdminUsers.get(username).equals(password);



                if(!isValidUser && !isValidModeratorUser && !isValidAdminUser) {
                    //If the user is not valid in any manor display a toast
                    Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
                else if(isValidUser){
                    /* when login button is pressed, use intent to switch to Login Activity */
                    Intent intent;
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);

                } else if(isValidModeratorUser){
                    /* if the user is a valid moderator, direct them to the moderator page*/
                    Log.d("HIT: ","Moderator Page");
                    Intent intent;
                    intent = new Intent(LoginActivity.this, ModeratorActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                } else{
                    /* if the user is a valid admin, direct them to the admin page*/
                    Log.d("HIT: ","Admin Page");
                    Intent intent;
                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
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
}