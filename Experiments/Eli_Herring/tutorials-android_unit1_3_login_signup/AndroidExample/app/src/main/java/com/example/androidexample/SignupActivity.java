package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText;  // define username edittext variable
    private EditText passwordEditText;  // define password edittext variable
    private EditText confirmEditText;   // define confirm edittext variable
    private Button loginButton;         // define login button variable
    private Button signupButton;        // define signup button variable

    private ConstraintLayout signupLayout;

    private Boolean darkModeOn = false;

    private LinearLayoutCompat signin;

    private TextView signinMessage;

    private TextView userText;

    private TextView passwordText;

    private TextView confirmPasswordText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /* initialize UI elements */
        usernameEditText = findViewById(R.id.signup_username_edt);  // link to username edtext in the Signup activity XML
        passwordEditText = findViewById(R.id.signup_password_edt);  // link to password edtext in the Signup activity XML
        confirmEditText = findViewById(R.id.signup_confirm_edt);    // link to confirm edtext in the Signup activity XML
        loginButton = findViewById(R.id.signup_login_btn);    // link to login button in the Signup activity XML
        signupButton = findViewById(R.id.signup_signup_btn);  // link to signup button in the Signup activity XML
        signin = findViewById(R.id.signupLayout);
        signinMessage = findViewById(R.id.signinText);
        userText = findViewById(R.id.userText);
        passwordText = findViewById(R.id.passwordText);
        confirmPasswordText = findViewById(R.id.confirmPasswordText);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            darkModeOn = extras.getBoolean("DARKMODE");
        }

        if(darkModeOn) {
            signin.setBackgroundColor(Color.parseColor("#1F1F1F"));
            usernameEditText.setTextColor(Color.parseColor("#BDBDBD"));
            passwordEditText.setTextColor(Color.parseColor("#BDBDBD"));
            confirmEditText.setTextColor(Color.parseColor("#BDBDBD"));
            signinMessage.setTextColor(Color.parseColor("#BDBDBD"));
            userText.setTextColor(Color.parseColor("#BDBDBD"));
            passwordText.setTextColor(Color.parseColor("#BDBDBD"));
            confirmPasswordText.setTextColor(Color.parseColor("#BDBDBD"));




        } else {
            signin.setBackgroundColor(Color.parseColor("#FFFFFF"));
            usernameEditText.setTextColor(Color.parseColor("#000000"));
            passwordEditText.setTextColor(Color.parseColor("#000000"));
            confirmEditText.setTextColor(Color.parseColor("#000000"));
            signinMessage.setTextColor(Color.parseColor("#000000"));
            userText.setTextColor(Color.parseColor("#000000"));
            passwordText.setTextColor(Color.parseColor("#000000"));
            confirmPasswordText.setTextColor(Color.parseColor("#000000"));


        }

        /* click listener on login button pressed */
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* when login button is pressed, use intent to switch to Login Activity */
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                intent.putExtra("DARKMODE",darkModeOn);
                startActivity(intent);  // go to LoginActivity
            }
        });

        /* click listener on signup button pressed */
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* grab strings from user inputs */
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirm = confirmEditText.getText().toString();

                if (password.equals(confirm)){
                    Toast.makeText(getApplicationContext(), "Signing up", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Password don't match", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}