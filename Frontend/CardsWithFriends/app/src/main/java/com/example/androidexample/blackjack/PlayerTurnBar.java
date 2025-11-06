package com.example.androidexample.blackjack;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.R;
import com.example.androidexample.blackjack.BlackjackModels.GameState;
import com.example.androidexample.blackjack.BlackjackModels.PlayerState;

/**
 * PlayerTurnBar
 *
 * Renders the horizontal list of player chips shown at the top of the blackjack screen.
 * Each chip represents a player, showing their name and status (selected / current turn).
 * Handles color, shadow, and pulsing animations for the active player's turn.
 */
public class PlayerTurnBar {

    private final LinearLayout container; // Layout container holding player chips
    private final AppCompatActivity activity; // Reference to the parent activity for context

    public PlayerTurnBar(LinearLayout container, AppCompatActivity activity) {
        this.container = container;
        this.activity = activity;
    }

    /**
     * Updates the entire turn bar UI based on the latest GameState.
     *
     * @param state           The current game state containing player and turn info.
     * @param selectedPlayer  The username of the player currently being viewed by the user.
     */
    public void updateFromGameState(GameState state, String selectedPlayer) {
        // Clear any existing chips before re-rendering
        container.removeAllViews();

        // Create one chip for each player in the game
        for (PlayerState player : state.players) {
            TextView chip = new TextView(activity);
            chip.setText(player.username);
            chip.setTextSize(18);
            chip.setTypeface(ResourcesCompat.getFont(activity, R.font.inter_bold));
            chip.setTextColor(Color.WHITE);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(48, 24, 48, 24);
            chip.setBackgroundResource(R.drawable.player_chip_bg);

            GradientDrawable bg = (GradientDrawable) chip.getBackground().mutate();

            // Flags to determine chip state
            boolean isSelected = player.username.equals(selectedPlayer);
            boolean isCurrentTurn = player.username.equals(state.currentTurn);

            // Apply style for the selected (viewed) player
            if (isSelected) {
                bg.setColors(new int[]{
                        ContextCompat.getColor(activity, R.color.my_green),
                        ContextCompat.getColor(activity, R.color.my_dark_green)
                });
                bg.setStroke(3, ContextCompat.getColor(activity, R.color.my_green));
                chip.setShadowLayer(15, 0, 0,
                        ContextCompat.getColor(activity, R.color.my_green));
                chip.setAlpha(1f);
            }
            // Apply default style for non-selected players
            else {
                bg.setColors(new int[]{
                        Color.parseColor("#2E2E2E"),
                        Color.parseColor("#111111")
                });
                bg.setStroke(1, Color.parseColor("#555555"));
                chip.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
                chip.setAlpha(0.8f);
            }

            // Animate chip if this player has the current turn
            if (isCurrentTurn) {
                chip.setShadowLayer(20, 0, 0,
                        ContextCompat.getColor(activity, R.color.my_green));
                chip.setElevation(10f);

                // Simple pulsing alpha animation to indicate turn
                ObjectAnimator pulse = ObjectAnimator.ofFloat(chip, "alpha", 1f, 0.65f, 1f);
                pulse.setDuration(1000);
                pulse.setRepeatCount(ValueAnimator.INFINITE);
                pulse.setRepeatMode(ValueAnimator.REVERSE);
                pulse.start();
            }

            // Fade-in animation for chip appearance
            chip.setAlpha(0f);
            chip.animate().alpha(1f).setDuration(200).start();

            // Add margins between chips for spacing
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(20, 0, 20, 0);
            chip.setLayoutParams(params);

            // Add completed chip to the container
            container.addView(chip);
        }
    }
}
