package com.example.androidexample.blackjack;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.R;
import com.example.androidexample.blackjack.BlackjackModels.CardState;
import com.example.androidexample.blackjack.BlackjackModels.DealerState;
import com.example.androidexample.blackjack.BlackjackModels.HandState;
import com.example.androidexample.blackjack.BlackjackModels.PlayerState;
import com.example.androidexample.services.CardView;

/**
 * Responsible for rendering card visuals and related UI elements
 * (like value and bet pills) for both players and the dealer.
 *
 * <p>This class encapsulates all layout logic for card display,
 * ensuring consistent spacing, alignment, and scaling across devices.
 * It focuses purely on UI generation — no game logic or data manipulation.</p>
 */
public class CardRenderer {

    /** Context used to access resources and instantiate views. */
    private final Context context;

    /** Screen density multiplier to convert dp → px for consistent sizing. */
    private final float density;

    /**
     * Constructs a new CardRenderer.
     *
     * @param context The Android context (the activity).
     * @param density The current device's display density.
     */
    public CardRenderer(Context context, float density) {
        this.context = context;
        this.density = density;
    }

    /**
     * Renders all of a player's hands within the given container.
     * <p>Each hand is stacked vertically with its cards displayed horizontally,
     * followed by value and bet indicators ("pills").</p>
     *
     * @param container The LinearLayout where player hands will be added.
     * @param player    The player whose hands should be rendered.
     */
    public void renderPlayerHands(LinearLayout container, PlayerState player) {
        // Clear any existing views first so we don’t append duplicates
        container.removeAllViews();

        // Stop early if there's no valid player or hands
        if (player == null || player.hands == null) return;

        // Player hands stacked vertically (one LinearLayout per hand)
        container.setOrientation(LinearLayout.VERTICAL);

        for (HandState hand : player.hands) {
            // Skip empty hands
            if (hand.hand == null || hand.hand.isEmpty()) continue;

            // Create a vertical container for this single hand
            LinearLayout handContainer = new LinearLayout(context);
            handContainer.setOrientation(LinearLayout.VERTICAL);
            handContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            handContainer.setPadding(0, dp(12), 0, dp(12));

            // Horizontal row for cards (placed FIRST so pills don’t constrain width)
            LinearLayout handRow = new LinearLayout(context);
            handRow.setOrientation(LinearLayout.HORIZONTAL);
            handRow.setGravity(Gravity.CENTER_HORIZONTAL);

            // Loop through each card and render it visually
            for (int i = 0; i < hand.hand.size(); i++) {
                CardState card = hand.hand.get(i);

                // Skip cards that should not be shown (face-down, etc.)
                if (!card.isShowing) continue;

                // Create a CardView (custom visual class) for each card
                CardView cv = new CardView(context);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(70), dp(105));

                // Overlap cards slightly (negative left margin after the first)
                if (i > 0) p.setMargins(dp(-30), 0, 0, 0);
                cv.setLayoutParams(p);

                // Set card visuals (value, suit, visibility)
                cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);
                handRow.addView(cv);
            }

            // Only add this hand to the layout if it has visible cards
            if (handRow.getChildCount() > 0) {
                handContainer.addView(handRow); // add card row first

                // Create the value pill (hand total)
                TextView valuePill = makePillText(
                        String.valueOf(hand.handValue),
                        R.font.inter_bold,
                        18,
                        Color.WHITE,
                        0.95f
                );

                // Position slightly below the cards
                LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                valueParams.topMargin = dp(8);
                valuePill.setLayoutParams(valueParams);
                handContainer.addView(valuePill);

                // Create the bet pill (shows the wager on this hand)
                TextView betPill = makePillText(
                        "$" + hand.bet,
                        R.font.inter_regular,
                        16,
                        Color.LTGRAY,
                        0.8f
                );

                // Add slight spacing below the value pill
                LinearLayout.LayoutParams betParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                betParams.topMargin = dp(4);
                betPill.setLayoutParams(betParams);
                handContainer.addView(betPill);

                // Add this entire hand (cards + pills) to the main container
                container.addView(handContainer);
            }
        }
    }

    /**
     * Renders the dealer’s hand in the provided container.
     * <p>The dealer layout includes a horizontal row of cards
     * followed by a single value pill underneath.</p>
     *
     * @param container The LinearLayout to place the dealer's hand in.
     * @param dealer    The dealer state object containing cards and hand value.
     */
    public void renderDealer(LinearLayout container, DealerState dealer) {
        // Remove any old dealer cards first
        container.removeAllViews();

        // Stop early if no valid dealer or empty hand
        if (dealer == null || dealer.hand == null || dealer.hand.isEmpty()) return;

        // Vertical container for the dealer’s section
        LinearLayout dealerLayout = new LinearLayout(context);
        dealerLayout.setOrientation(LinearLayout.VERTICAL);
        dealerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        dealerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Horizontal row of cards (centered)
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_HORIZONTAL);

        // Loop through dealer cards and add them visually
        for (CardState card : dealer.hand) {
            CardView cv = new CardView(context);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(80), dp(120));
            p.setMargins(dp(6), 0, dp(6), 0); // small gap between cards
            cv.setLayoutParams(p);
            cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);
            row.addView(cv);
        }

        // Add the card row to the dealer layout
        dealerLayout.addView(row);

        // Create a value pill for the dealer’s total
        TextView valuePill = makePillText(
                String.valueOf(dealer.handValue),
                R.font.inter_bold,
                18,
                Color.WHITE,
                0.95f
        );

        // Add spacing between cards and pill
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        valueParams.topMargin = dp(8);
        valuePill.setLayoutParams(valueParams);

        // Add pill below cards
        dealerLayout.addView(valuePill);

        // Finally add the dealer layout to the container
        container.addView(dealerLayout);
    }

    // ==========================================================
    // Private Helpers — UI construction utilities
    // ==========================================================

    /**
     * Creates a pill-shaped TextView with consistent styling.
     *
     * @param text    The text content for the pill.
     * @param fontRes The font resource (e.g., R.font.inter_bold).
     * @param sizeSp  The text size in SP.
     * @param color   The text color.
     * @param alpha   The view opacity (1.0f = fully visible).
     * @return A styled TextView representing a pill.
     */
    private TextView makePillText(String text, int fontRes, int sizeSp, int color, float alpha) {
        // Create new TextView and apply visual style
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        tv.setTypeface(ResourcesCompat.getFont(context, fontRes));
        tv.setGravity(Gravity.CENTER);

        // Add generous horizontal padding to create "pill" shape
        tv.setPadding(dp(24), dp(8), dp(24), dp(8));

        // Use reusable rounded background drawable
        tv.setBackgroundResource(R.drawable.bg_hand_value_pill);

        // Adjust transparency for secondary UI elements
        tv.setAlpha(alpha);

        return tv;
    }

    /**
     * Converts a density-independent pixel (dp) value to
     * actual screen pixels for consistent scaling across devices.
     *
     * @param v The dp value to convert.
     * @return The equivalent pixel value as an integer.
     */
    private int dp(int v) {
        // Multiply by density and round down to nearest pixel
        return (int) (v * density);
    }
}
