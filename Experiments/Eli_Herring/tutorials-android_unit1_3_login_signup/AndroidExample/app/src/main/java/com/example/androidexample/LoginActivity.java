package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    static String[][] whiteList = new String[][] {
            {"Tom", "1234"},
            {"Bob", "5678"},
            {"Mike", "3333"}
    };

    static boolean stringPairCompare(String[] pair1, String[] pair2){

        for(int i = 0; i < 2; i++){
            if(!pair1[i].equals(pair2[i])) {
                return false;
            }
        }
        return true;
    }
    static boolean isWhiteListed(String user, String pass){
        String[] upInfo = new String[] {user,pass};

        for(String[] whiteInfo : whiteList){
            if(stringPairCompare(whiteInfo, upInfo)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);            // link to Login activity XML
        /* initialize UI elements */
        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginButton = findViewById(R.id.login_login_btn);    // link to login button in the Login activity XML
        signupButton = findViewById(R.id.login_signup_btn);  // link to signup button in the Login activity XML







        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                Intent intent;// go to MainActivity with the key-value data
                if(isWhiteListed(username,password)){
                    intent = new Intent(LoginActivity.this, AdminActivity.class);
                    intent.putExtra("USERNAME", username);
                } else {

                    /* when login button is pressed, use intent to switch to Login Activity */
                    intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);  // key-value to pass to the MainActivity
                    intent.putExtra("PASSWORD", password);  // key-value to pass to the MainActivity
                }
                startActivity(intent);
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when signup button is pressed, use intent to switch to Signup Activity */
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);  // go to SignupActivity
            }
        });
    }
}