package com.example.androidexample.services;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.R;

public class Crazy8CardView extends View {

    private int value = 8;        // 2–14
    private char color = 'R';     // 'R','G','B','Y'
    private boolean faceUp = true;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private Typeface interBlack;

    // Colors from your palette
    private int redStart, redEnd;
    private int greenStart, greenEnd;
    private int blueStart, blueEnd;
    private int yellowStart, yellowEnd;
    private int greyStart, greyEnd;

    // Icons
    private Drawable reverseDrawable;
    private Drawable skipDrawable;
    private Drawable eightDrawable;

    public Crazy8CardView(Context c) { super(c); init(c); }
    public Crazy8CardView(Context c, @Nullable AttributeSet a) { super(c, a); init(c); }
    public Crazy8CardView(Context c, @Nullable AttributeSet a, int s) { super(c, a, s); init(c); }

    private void init(Context context) {
        interBlack = ResourcesCompat.getFont(context, R.font.inter_black);
        paint.setTypeface(interBlack);
        paint.setTextAlign(Paint.Align.CENTER);

        redStart    = ContextCompat.getColor(context, R.color.my_red);
        redEnd      = ContextCompat.getColor(context, R.color.my_dark_red);

        greenStart  = ContextCompat.getColor(context, R.color.my_green);
        greenEnd    = ContextCompat.getColor(context, R.color.my_dark_green);

        blueStart   = ContextCompat.getColor(context, R.color.my_blue);
        blueEnd     = ContextCompat.getColor(context, R.color.my_dark_blue);

        yellowStart = ContextCompat.getColor(context, R.color.my_orange);
        yellowEnd   = ContextCompat.getColor(context, R.color.my_dark_orange);

        greyStart   = ContextCompat.getColor(context, R.color.my_dark_grey);
        greyEnd     = ContextCompat.getColor(context, R.color.my_light_grey);

        reverseDrawable = ContextCompat.getDrawable(context, R.drawable.card_reverse);
        skipDrawable    = ContextCompat.getDrawable(context, R.drawable.card_skip);
        eightDrawable   = ContextCompat.getDrawable(context, R.drawable.card_eight);

        setLayerType(LAYER_TYPE_HARDWARE, null);

        // Enable click handling
        setClickable(true);
    }

    // -------------------------------------------------------------------------
    // Public setter
    // -------------------------------------------------------------------------
    public void setCard(int value, char color, boolean faceUp) {
        this.value = value;
        this.color = color;
        this.faceUp = faceUp;
        invalidate();
    }

    // -------------------------------------------------------------------------
    // FIXED TOUCH → CLICK HANDLING
    // -------------------------------------------------------------------------
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                // Slight press animation
                animate().scaleX(1.05f).scaleY(1.05f).setDuration(80).start();
                return true;

            case MotionEvent.ACTION_UP:
                // Release animation
                animate().scaleX(1f).scaleY(1f).setDuration(80).start();

                // Register click event → triggers OnClickListener in Activity
                performClick();
                return true;

            case MotionEvent.ACTION_CANCEL:
                animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // -------------------------------------------------------------------------
    // DRAWING
    // -------------------------------------------------------------------------
    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth();
        int h = getHeight();
        float radius = w * 0.12f;

        rect.set(0, 0, w, h);
        Path path = new Path();
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);

        if (!faceUp) {
            Shader shader = new LinearGradient(0, 0, w, h, greyStart, greyEnd, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            c.drawPath(path, paint);

            paint.setShader(null);
            paint.setColor(Color.WHITE);
            paint.setTextSize(h * 0.35f);
            c.drawText("★", w / 2f, h / 2f + h * 0.12f, paint);
            return;
        }

        // FRONT gradient (special for 8)
        Shader bg;
        if (value == 8) {
            int[] colors = new int[]{ redStart, yellowStart, greenStart, blueStart };
            float[] positions = new float[]{ 0f, 0.33f, 0.66f, 1f };

            bg = new LinearGradient(0, 0, w, h, colors, positions, Shader.TileMode.CLAMP);
        } else {
            bg = new LinearGradient(0, 0, 0, h, getStartColor(color), getEndColor(color), Shader.TileMode.CLAMP);
        }

        paint.setShader(bg);
        c.drawPath(path, paint);
        paint.setShader(null);

        // ICON / TEXT
        if (value == 11 && reverseDrawable != null) {
            drawCenteredDrawable(c, reverseDrawable, w, h);
        } else if (value == 12 && skipDrawable != null) {
            drawCenteredDrawable(c, skipDrawable, w, h);
        } else if (value == 8 && eightDrawable != null) {
            drawCenteredDrawable(c, eightDrawable, w, h);
        } else {
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);

            if (value == 13 || value == 14)
                paint.setTextSize(h * 0.42f);
            else
                paint.setTextSize(h * 0.55f);

            c.drawText(getCenterText(value), w / 2f, h * 0.67f, paint);
        }
    }

    // -------------------------------------------------------------------------
    // Icon helpers
    // -------------------------------------------------------------------------
    private void drawCenteredDrawable(Canvas canvas, Drawable drawable, int w, int h) {
        int size = (int) (Math.min(w, h) * 0.55f);
        int left = (w - size) / 2;
        int top = (h - size) / 2;
        drawable.setBounds(left, top, left + size, top + size);
        drawable.draw(canvas);
    }

    // -------------------------------------------------------------------------
    // Color helpers
    // -------------------------------------------------------------------------
    private int getStartColor(char c) {
        switch (c) {
            case 'R': return redStart;
            case 'G': return greenStart;
            case 'B': return blueStart;
            case 'Y': return yellowStart;
            default: return greyStart;
        }
    }

    private int getEndColor(char c) {
        switch (c) {
            case 'R': return redEnd;
            case 'G': return greenEnd;
            case 'B': return blueEnd;
            case 'Y': return yellowEnd;
            default: return greyEnd;
        }
    }

    private String getCenterText(int v) {
        switch (v) {
            case 13: return "+2";
            case 14: return "+4";
            default: return String.valueOf(v);
        }
    }
}
