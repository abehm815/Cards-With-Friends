package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class BlackjackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blackjack);

        ConstraintLayout root = findViewById(R.id.rootLayout);
        Button flipBtn = findViewById(R.id.flipButton);

        // Create and add card
        CardView card = new CardView(this);
        card.setId(View.generateViewId());
        card.setLayoutParams(new ConstraintLayout.LayoutParams(300, 400));
        card.setCard("J", "C", true);
        root.addView(card);

        // Position the card slightly below center
        ConstraintSet set = new ConstraintSet();
        set.clone(root);
        set.connect(card.getId(), ConstraintSet.LEFT, root.getId(), ConstraintSet.LEFT, 0);
        set.connect(card.getId(), ConstraintSet.RIGHT, root.getId(), ConstraintSet.RIGHT, 0);
        set.connect(card.getId(), ConstraintSet.TOP, root.getId(), ConstraintSet.TOP, 0);
        set.connect(card.getId(), ConstraintSet.BOTTOM, root.getId(), ConstraintSet.BOTTOM, 0);

        // Bias it toward the bottom (0.0 = top, 1.0 = bottom)
        set.setVerticalBias(card.getId(), 0.75f); // 75% down the screen
        set.applyTo(root);

        // Flip animation
        flipBtn.setOnClickListener(v -> card.flipCard());
    }
}
