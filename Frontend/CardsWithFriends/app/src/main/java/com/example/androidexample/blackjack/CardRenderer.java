package com.example.androidexample.blackjack;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;

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
        container.removeAllViews();
        if (player == null || player.hands == null) return;

        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);

        int totalHands = player.hands.size();
        boolean isSplit = totalHands > 1;

        for (int h = 0; h < totalHands; h++) {
            HandState hand = player.hands.get(h);
            if (hand.hand == null || hand.hand.isEmpty()) continue;

            LinearLayout handContainer = new LinearLayout(context);
            handContainer.setClipChildren(false);
            handContainer.setClipToPadding(false);
            handContainer.setOrientation(LinearLayout.VERTICAL);
            handContainer.setGravity(Gravity.CENTER_HORIZONTAL);
            handContainer.setPadding(0, dp(12), 0, dp(12));

            LinearLayout handRow = new LinearLayout(context);
            handRow.setClipChildren(false);
            handRow.setClipToPadding(false);
            handRow.setOrientation(LinearLayout.HORIZONTAL);
            handRow.setGravity(Gravity.CENTER_HORIZONTAL);

            int cardCount = hand.hand.size();

            // Default overlap (normal hand)
            int overlap = -dp(Math.min(12 + (cardCount - 2) * 6, 40));

            // If split, cards should overlap more tightly (reduce visible gap)
            if (isSplit) overlap = (int)(overlap * 1.5f);

            for (int i = 0; i < cardCount; i++) {
                CardState card = hand.hand.get(i);
                if (!card.isShowing) continue;

                CardView cv = new CardView(context);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(70), dp(105));
                if (i > 0) p.setMargins(overlap, 0, 0, 0);
                cv.setLayoutParams(p);
                cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);
                handRow.addView(cv);
            }

            if (handRow.getChildCount() > 0) {
                handContainer.addView(handRow);

                TextView valuePill = makePillText(
                        String.valueOf(hand.handValue),
                        R.font.inter_bold,
                        18,
                        Color.WHITE,
                        0.95f
                );
                LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                valueParams.topMargin = dp(8);
                valuePill.setLayoutParams(valueParams);
                handContainer.addView(valuePill);

                TextView betPill = makePillText(
                        "$" + hand.bet,
                        R.font.inter_regular,
                        16,
                        Color.LTGRAY,
                        0.8f
                );
                LinearLayout.LayoutParams betParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                betParams.topMargin = dp(4);
                betPill.setLayoutParams(betParams);
                handContainer.addView(betPill);

                LinearLayout.LayoutParams handParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

                // Make gap between split hands much larger
                int spacing = isSplit ? dp(40) : dp(16);
                handParams.setMargins(spacing, 0, spacing, 0);

                handContainer.setLayoutParams(handParams);
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
    public void renderDealer(LinearLayout container, DealerState dealer, boolean isRoundOver) {
        container.removeAllViews();
        if (dealer == null || dealer.hand == null || dealer.hand.isEmpty()) return;

        LinearLayout dealerLayout = new LinearLayout(context);
        dealerLayout.setOrientation(LinearLayout.VERTICAL);
        dealerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        dealerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        dealerLayout.setClipChildren(false);
        dealerLayout.setClipToPadding(false);

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        row.setClipChildren(false);
        row.setClipToPadding(false);

        for (int i = 0; i < dealer.hand.size(); i++) {
            CardState card = dealer.hand.get(i);
            CardView cv = new CardView(context);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(80), dp(120));

            if (isRoundOver && i > 0) {
                int overlap = Math.max(-dp(90 / dealer.hand.size()), -dp(45));
                p.setMargins(overlap, 0, 0, 0);
            } else {
                p.setMargins(dp(6), 0, dp(6), 0);
            }

            cv.setLayoutParams(p);
            cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);
            row.addView(cv);
        }

        dealerLayout.addView(row);

        if (isRoundOver) {
            TextView valuePill = makePillText(
                    String.valueOf(dealer.handValue),
                    R.font.inter_bold,
                    18,
                    Color.WHITE,
                    0.95f
            );
            LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            valueParams.topMargin = dp(8);
            valuePill.setLayoutParams(valueParams);
            dealerLayout.addView(valuePill);
        }

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
        tv.setBackgroundResource(R.drawable.bj_hand_value_pill);

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
    /**
     * Animates all cards on screen out of view — used at end of round.
     * Cards lift up, drift toward the top-left, and fade out.
     * Should be called before starting the next round (e.g., during countdown).
     *
     * @param containerRoot The parent layout that holds card groups (e.g., cardContainer or dealerCardContainer).
     */
    public void animateAllCardsOut(LinearLayout containerRoot) {
        if (containerRoot == null) return;

        // Recursively animate all CardViews within this layout
        animateGroupCardsOut(containerRoot);
    }

    /**
     * Recursively traverses a layout and animates all CardView children out of view.
     */
    private void animateGroupCardsOut(ViewGroup group) {
        // fade out all value/bet TextViews but preserve layout space
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);

            if (child instanceof TextView) {
                child.animate()
                        .alpha(0f)
                        .translationYBy(-40f)
                        .setDuration(400)
                        .withEndAction(() -> child.setVisibility(View.INVISIBLE))
                        .start();

            } else if (child instanceof ViewGroup) {
                animateGroupCardsOut((ViewGroup) child);
            }
        }

        // after a delay, animate cards directly off-screen (top-left corner)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            int screenWidth = group.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = group.getResources().getDisplayMetrics().heightPixels;

            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);

                if (child instanceof CardView) {
                    // Move far enough up and left to guarantee it’s off screen
                    float targetX = -screenWidth * 0.8f;
                    float targetY = -screenHeight * 0.8f;

                    child.animate()
                            .translationXBy(targetX)
                            .translationYBy(targetY)
                            .rotationBy(-15f)
                            .setDuration(800)
                            .withEndAction(() -> {
                                ViewGroup parent = (ViewGroup) child.getParent();
                                if (parent != null) parent.removeView(child);
                            })
                            .start();
                }
            }
        }, 450);
    }

}
