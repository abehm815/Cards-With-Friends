package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CounterActivity extends AppCompatActivity {

    private TextView numberTxt; // define number textview variable

    private TextView errorMessage;
    private Button increaseBtn; // define increase button variable
    private Button decreaseBtn; // define decrease button variable

    private Button multBtn;
    private Button backBtn;     // define back button variable

    private Button divBtn;
    private int counter = 0;    // counter variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);

        /* initialize UI elements */
        numberTxt = findViewById(R.id.number);
        errorMessage = findViewById(R.id.error_msg);
        increaseBtn = findViewById(R.id.counter_increase_btn);
        decreaseBtn = findViewById(R.id.counter_decrease_btn);
        multBtn = findViewById(R.id.counter_mult_btn);
        backBtn = findViewById(R.id.counter_back_btn);
        divBtn = findViewById(R.id.counter_div_btn);

        /* when increase btn is pressed, counter++, reset number textview */
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter + 1 >= 10000){
                    errorMessage.setText("Number is too large");
                } else {
                    numberTxt.setText(String.valueOf(++counter));
                    errorMessage.setText("");
                }
            }
        });

        /* when decrease btn is pressed, counter--, reset number textview */
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter - 1 <= -10000){
                    errorMessage.setText("Number is too small");
                } else {
                    numberTxt.setText(String.valueOf(--counter));
                    errorMessage.setText("");
                }
            }
        });

        multBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter * 2 >= 10000){
                    errorMessage.setText("Number is too large");
                } else if(counter * 2 <= -10000){
                    errorMessage.setText("Number is too small");
                } else {
                    counter = counter * 2;
                    numberTxt.setText(String.valueOf(counter));
                    errorMessage.setText("");
                }
            }
        });

        divBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter % 2 == 0){
                    counter = counter / 2;
                    numberTxt.setText(String.valueOf(counter));
                }else{
                    errorMessage.setText("Cannot divide an odd number by 2");
                }

            }
        });

        /* when back btn is pressed, switch back to MainActivity */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CounterActivity.this, MainActivity.class);
                intent.putExtra("NUM", String.valueOf(counter));  // key-value to pass to the MainActivity
                startActivity(intent);
            }
        });

    }
}