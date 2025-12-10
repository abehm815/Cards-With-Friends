package com.example.androidexample.crazy8;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.androidexample.R;

public class UIHelpers {

    private static Drawable lastGradient = null;

    // ------------------ GRADIENT BACKGROUND ------------------ //

    public static void applyColorTheme(View root, String code, Context ctx) {
        if (root == null) return;

        int start;
        int end;

        switch (code) {
            case "R":
                start = ctx.getColor(R.color.my_red);
                end = ctx.getColor(R.color.my_dark_red);
                break;

            case "G":
                start = ctx.getColor(R.color.my_green);
                end = ctx.getColor(R.color.my_dark_green);
                break;

            case "B":
                start = ctx.getColor(R.color.my_blue);
                end = ctx.getColor(R.color.my_dark_blue);
                break;

            case "Y":
                start = ctx.getColor(R.color.my_orange);
                end = ctx.getColor(R.color.my_dark_orange);
                break;

            default:
                // fallback grey
                root.setBackgroundColor(ctx.getColor(R.color.my_grey));
                return;
        }

        GradientDrawable gradient = createRadialGradient(start, end);
        animateGradient(root, gradient);
    }

    public static GradientDrawable createRadialGradient(int startColor, int endColor) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        g.setGradientCenter(0.5f, 0.5f);
        g.setGradientRadius(900f);
        g.setColors(new int[]{startColor, endColor});
        return g;
    }

    private static void animateGradient(View root, Drawable newGradient) {
        if (lastGradient == null) {
            root.setBackground(newGradient);
            lastGradient = newGradient;
            return;
        }

        TransitionDrawable td = new TransitionDrawable(new Drawable[]{lastGradient, newGradient});
        td.setCrossFadeEnabled(true);
        root.setBackground(td);
        td.startTransition(500);

        lastGradient = newGradient;
    }

    // ------------------ PENALTY MODE HELPERS ------------------ //

    public static void enablePenaltyMode(
            TextView banner,
            Button penaltyBtn,
            LinearLayout normalButtons,
            int drawStack
    ) {
        banner.setText("Must draw " + drawStack + " cards or stack +2/+4");
        banner.setVisibility(View.VISIBLE);

        penaltyBtn.setText("Draw " + drawStack + " Cards");
        penaltyBtn.setVisibility(View.VISIBLE);

        normalButtons.setVisibility(View.GONE);
    }

    public static void disablePenaltyMode(
            TextView banner,
            Button penaltyBtn,
            LinearLayout normalButtons
    ) {
        banner.setVisibility(View.GONE);
        penaltyBtn.setVisibility(View.GONE);
        normalButtons.setVisibility(View.VISIBLE);
    }

    // ------------------ COLOR PICKER ------------------ //

    public static void showColorPicker(View overlay) {
        overlay.setVisibility(View.VISIBLE);
    }

    public static void hideColorPicker(View overlay) {
        overlay.setVisibility(View.GONE);
    }
}
