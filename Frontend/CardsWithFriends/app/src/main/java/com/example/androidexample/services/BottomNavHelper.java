package com.example.androidexample.services;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.widget.ImageButton;
import androidx.core.content.ContextCompat;

import com.example.androidexample.HomeActivity;
import com.example.androidexample.ProfileActivity;
import com.example.androidexample.R;
import com.example.androidexample.StatsActivity;
import com.example.androidexample.LeaderboardActivity;

public class BottomNavHelper {

    public static void setupBottomNav(Activity activity, String username) {
        ImageButton homeBtn = activity.findViewById(R.id.nav_home);
        ImageButton statsBtn = activity.findViewById(R.id.nav_stats);
        ImageButton leaderboardBtn = activity.findViewById(R.id.nav_leaderboard);
        ImageButton profileBtn = activity.findViewById(R.id.nav_profile);

        if (homeBtn == null || statsBtn == null || leaderboardBtn == null || profileBtn == null) return;

        int active = ContextCompat.getColor(activity, R.color.my_green);
        int inactive = ContextCompat.getColor(activity, android.R.color.white);

        // --- Highlight the active screen ---
        if (activity instanceof HomeActivity) {
            homeBtn.setImageTintList(ColorStateList.valueOf(active));
            statsBtn.setImageTintList(ColorStateList.valueOf(inactive));
            leaderboardBtn.setImageTintList(ColorStateList.valueOf(inactive));
            profileBtn.setImageTintList(ColorStateList.valueOf(inactive));
        } else if (activity instanceof StatsActivity) {
            statsBtn.setImageTintList(ColorStateList.valueOf(active));
            homeBtn.setImageTintList(ColorStateList.valueOf(inactive));
            leaderboardBtn.setImageTintList(ColorStateList.valueOf(inactive));
            profileBtn.setImageTintList(ColorStateList.valueOf(inactive));
        } else if (activity instanceof LeaderboardActivity) {
            leaderboardBtn.setImageTintList(ColorStateList.valueOf(active));
            homeBtn.setImageTintList(ColorStateList.valueOf(inactive));
            statsBtn.setImageTintList(ColorStateList.valueOf(inactive));
            profileBtn.setImageTintList(ColorStateList.valueOf(inactive));
        } else if (activity instanceof ProfileActivity) {
            profileBtn.setImageTintList(ColorStateList.valueOf(active));
            homeBtn.setImageTintList(ColorStateList.valueOf(inactive));
            statsBtn.setImageTintList(ColorStateList.valueOf(inactive));
            leaderboardBtn.setImageTintList(ColorStateList.valueOf(inactive));
        } else {
            homeBtn.setImageTintList(ColorStateList.valueOf(inactive));
            statsBtn.setImageTintList(ColorStateList.valueOf(inactive));
            leaderboardBtn.setImageTintList(ColorStateList.valueOf(inactive));
            profileBtn.setImageTintList(ColorStateList.valueOf(inactive));
        }

        // --- Navigation actions ---
        homeBtn.setOnClickListener(v -> {
            if (!(activity instanceof HomeActivity)) {
                Intent i = new Intent(activity, HomeActivity.class);
                i.putExtra("USERNAME", username);
                activity.startActivity(i);
            }
        });

        statsBtn.setOnClickListener(v -> {
            if (!(activity instanceof StatsActivity)) {
                Intent i = new Intent(activity, StatsActivity.class);
                i.putExtra("USERNAME", username);
                activity.startActivity(i);
            }
        });

        leaderboardBtn.setOnClickListener(v -> {
            if (!(activity instanceof LeaderboardActivity)) {
                Intent i = new Intent(activity, LeaderboardActivity.class);
                i.putExtra("USERNAME", username);
                activity.startActivity(i);
            }
        });

        profileBtn.setOnClickListener(v -> {
            if (!(activity instanceof ProfileActivity)) {
                Intent i = new Intent(activity, ProfileActivity.class);
                i.putExtra("USERNAME", username);
                activity.startActivity(i);
            }
        });
    }
}
