package com.example.androidexample;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.androidexample.services.CardView;

import java.util.Arrays;

public class GofishActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gofish);

        ConstraintLayout root = findViewById(R.id.rootLayout);
        Button flipBtn = findViewById(R.id.flipButton);

        // Create and add card
        /*
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

        */

        CardView card1 = new CardView(this);
        card1.setId(View.generateViewId());
        card1.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card1.setCard("4", "C", true);
        root.addView(card1);

        CardView card2 = new CardView(this);
        card2.setId(View.generateViewId());
        card2.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card2.setCard("4", "S", true);
        root.addView(card2);

        CardView card3 = new CardView(this);
        card3.setId(View.generateViewId());
        card3.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card3.setCard("4", "H", true);
        root.addView(card3);

        CardView card4 = new CardView(this);
        card4.setId(View.generateViewId());
        card4.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card4.setCard("4", "D", true);
        root.addView(card4);

        CardView card5 = new CardView(this);
        card5.setId(View.generateViewId());
        card5.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card5.setCard("4", "H", true);
        root.addView(card5);

        CardView card6 = new CardView(this);
        card6.setId(View.generateViewId());
        card6.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card6.setCard("4", "D", true);
        root.addView(card6);

        CardView card7 = new CardView(this);
        card7.setId(View.generateViewId());
        card7.setLayoutParams(new ConstraintLayout.LayoutParams(150,200));
        card7.setCard("4", "H", true);
        root.addView(card7);



        // ---------- Dynamic fan layout (replace your ConstraintSet loop) ----------
        ConstraintSet set = new ConstraintSet();
        set.clone(root);

// Put your cards into an array (works for any length)
        CardView[] cards = { card1, card2, card3, card4, card5, card6, card7};

        int n = cards.length;
        if (n == 0) return; // nothing to do

// Horizontal bias start and spacing (tweak to taste)
        float startBias = 0.2f;    // leftmost bias
        float endBias   = 0.8f;    // rightmost bias

// Total angular spread in degrees (tweak: bigger = wider fan)
        float totalSpread = 40f; // degrees; spread across all cards

// Compute bias step so cards spread evenly across the available range
        float biasStep = (n == 1) ? 0f : (endBias - startBias) / (n - 1);

// If you want rotation to be symmetric about center, compute per-card angle:
// For n==1 -> 0 degrees. For n>1 -> angles from -totalSpread/2 to +totalSpread/2
        float angleStep = (n == 1) ? 0f : totalSpread / (n - 1);
        float firstAngle = -totalSpread / 2f;

        int midN = n / 2;
        float verticalOffset = 0.05f / midN;


        for (int i = 0; i < n; i++) {


            CardView card = cards[i];
            int id = card.getId();

            // Connect to parent's edges so bias applies (centered within parent)
            set.connect(id, ConstraintSet.LEFT, root.getId(), ConstraintSet.LEFT, 0);
            set.connect(id, ConstraintSet.RIGHT, root.getId(), ConstraintSet.RIGHT, 0);
            set.connect(id, ConstraintSet.TOP, root.getId(), ConstraintSet.TOP, 0);
            set.connect(id, ConstraintSet.BOTTOM, root.getId(), ConstraintSet.BOTTOM, 0);

            // Vertical placement: near bottom
            if(i < midN){
                set.setVerticalBias(id, 0.80f - (i % midN * verticalOffset));
            } else if(i > midN) {
                set.setVerticalBias(id, 0.75f + (i % (midN + 1) * verticalOffset));
            } else if(i == midN){
                set.setVerticalBias(id, 0.75f);
            }


            // Horizontal bias spaced across startBias...endBias
            set.setHorizontalBias(id, startBias + i * biasStep);
        }

// Apply constraints first so cards have a measured size when we rotate
        set.applyTo(root);

// Now set pivots + rotation AFTER layout pass so width/height are available.
// Use post() to run after layout. Also add optional animation.
        for (int i = 0; i < n; i++) {
            final CardView card = cards[i];
            final float targetAngle = firstAngle + i * angleStep; // computed angle
            card.post(() -> {
                // pivot is bottom-center so cards rotate like a hand
                card.setPivotX(card.getWidth() / 2f);
                card.setPivotY(card.getHeight());

                // instantly set rotation:
                card.setRotation(targetAngle);

                // — optional: animate instead of instantly set —
                // uncomment the block below to animate the fan opening

        card.setRotation(0f); // start flat (or some initial)
        int index = Arrays.asList(cards).indexOf(card); // or capture i in outer scope
        long delay = index * 80L; // stagger (ms)
        card.animate()
            .rotation(targetAngle)
            .setStartDelay(delay)
            .setDuration(350)
            .start();

            });
        }



        flipBtn.setOnClickListener(v -> card7.flipCard());

    }
}
