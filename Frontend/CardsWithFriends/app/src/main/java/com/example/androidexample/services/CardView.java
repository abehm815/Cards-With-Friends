package com.example.androidexample.services;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.R;

/**
 * Custom view for displaying playing cards with animations and selection support
 */
public class CardView extends View {

    // Card properties
    private String rank = "7";
    private String suit = "s";
    private boolean faceUp = true;
    private boolean isCardSelected = false;

    // Drawing objects
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private Typeface interBlack;

    // Colors
    private int redStart, redEnd, greyStart, greyEnd;
    private int selectionColor;

    // Touch handling
    private float dX, dY;
    private float startX, startY;
    private float startRotation;
    private boolean isDragging = false;

    // Constructors
    public CardView(Context context) {
        super(context);
        init(context);
    }

    public CardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * Initialize the card view with fonts, colors, and paint settings
     */
    private void init(Context context) {
        // Load custom font
        interBlack = ResourcesCompat.getFont(context, R.font.inter_black);
        paint.setTypeface(interBlack);
        paint.setTextAlign(Paint.Align.LEFT);

        // Load colors
        redStart = ContextCompat.getColor(context, R.color.my_red);
        redEnd = ContextCompat.getColor(context, R.color.my_dark_red);
        greyStart = ContextCompat.getColor(context, R.color.my_dark_grey);
        greyEnd = ContextCompat.getColor(context, R.color.my_light_grey);
        selectionColor = Color.rgb(255, 215, 0); // Gold color for selection

        // Setup selection paint
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(10f);
        selectionPaint.setColor(selectionColor);
        selectionPaint.setStrokeCap(Paint.Cap.ROUND);

        // Enable hardware acceleration for smoother animations
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    // ==================== Public API ====================

    /**
     * Set the card's rank, suit, and face-up state
     */
    public void setCard(String rank, String suit, boolean faceUp) {
        this.rank = rank.toLowerCase();
        this.suit = suit.toLowerCase();
        this.faceUp = faceUp;
        invalidate();
    }

    /**
     * Get the card's rank/value (e.g., "A", "K", "10")
     */
    public String getCardValue() {
        return rank;
    }

    /**
     * Get the card's suit (e.g., "h", "d", "c", "s")
     */
    public String getCardSuit() {
        return suit;
    }

    /**
     * Check if the card is face up
     */
    public boolean isFaceUp() {
        return faceUp;
    }

    /**
     * Override setSelected to provide visual feedback
     */
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        this.isCardSelected = selected;

        if (selected) {
            // Animate selection
            animateSelection(true);
        } else {
            // Animate deselection
            animateSelection(false);
        }

        invalidate();
    }

    @Override
    public boolean isSelected() {
        return isCardSelected;
    }

    /**
     * Flip the card with animation
     */
    public void flipCard() {
        ObjectAnimator half1 = ObjectAnimator.ofFloat(this, "rotationY", 0f, 90f);
        half1.setDuration(150);

        ObjectAnimator half2 = ObjectAnimator.ofFloat(this, "rotationY", -90f, 0f);
        half2.setDuration(150);

        half1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                faceUp = !faceUp;
                invalidate();
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(half1, half2);
        set.start();
    }

    // ==================== Touch Handling ====================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the card is not clickable, don't handle touch events
        if (!isClickable()) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDragging = false;

                // Remember original position and rotation
                startX = getX();
                startY = getY();
                startRotation = getRotation();

                // Record offset between touch and card position
                dX = event.getRawX() - getX();
                dY = event.getRawY() - getY();

                // Straighten and "lift" the card
                animate()
                        .rotation(0f)
                        .scaleX(1.15f)
                        .scaleY(1.15f)
                        .setDuration(150)
                        .start();
                setElevation(25f);
                return true;

            case MotionEvent.ACTION_MOVE:
                // Check if user is dragging
                float deltaX = Math.abs(event.getRawX() - (startX + dX));
                float deltaY = Math.abs(event.getRawY() - (startY + dY));

                if (deltaX > 10 || deltaY > 10) {
                    isDragging = true;
                }

                if (isDragging) {
                    // Move with finger
                    float newX = event.getRawX() - dX;
                    float newY = event.getRawY() - dY;
                    setX(newX);
                    setY(newY);
                }
                return true;

            case MotionEvent.ACTION_UP:
                // Animate back to original position
                animate()
                        .x(startX)
                        .y(startY)
                        .rotation(startRotation)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(350)
                        .withEndAction(() -> setElevation(0f))
                        .start();

                // If not dragging, treat as a click
                if (!isDragging) {
                    performClick();
                }

                isDragging = false;
                return true;

            case MotionEvent.ACTION_CANCEL:
                // Animate back to original position
                animate()
                        .x(startX)
                        .y(startY)
                        .rotation(startRotation)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(350)
                        .withEndAction(() -> setElevation(0f))
                        .start();

                isDragging = false;
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // ==================== Animation ====================

    /**
     * Animate the selection state change
     */
    private void animateSelection(boolean selected) {
        if (selected) {
            // Slightly scale up and add elevation
            animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(200)
                    .start();
            setElevation(12f);
        } else {
            // Scale back to normal
            animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
            setElevation(0f);
        }
    }

    // ==================== Drawing ====================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float cornerRadius = w * 0.12f;

        rect.set(0, 0, w, h);

        if (faceUp) {
            drawCardFront(canvas, w, h, cornerRadius);
        } else {
            drawCardBack(canvas, w, h, cornerRadius);
        }

        // Draw selection indicator if selected
        if (isCardSelected) {
            drawSelectionIndicator(canvas, w, h, cornerRadius);
        }
    }

    /**
     * Draw the front of the card
     */
    private void drawCardFront(Canvas canvas, int w, int h, float cornerRadius) {
        // Create rounded rectangle path
        Path path = new Path();
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);

        // Draw white background
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPath(path, paint);

        // Determine color based on suit
        boolean isRed = suit.equals("h") || suit.equals("d");
        int startColor = isRed ? redStart : greyStart;
        int endColor = isRed ? redEnd : greyEnd;

        // Create gradient for text
        Shader gradient = new LinearGradient(0, 0, 0, h, startColor, endColor, Shader.TileMode.CLAMP);
        paint.setShader(gradient);

        // Draw rank in top-left corner
        paint.setTextSize(h * 0.35f);
        canvas.drawText(formatRank(rank), w * 0.1f, h * 0.35f, paint);

        // Draw suit symbol in center
        paint.setTextSize(h * 0.55f);
        String suitSymbol = getSuitSymbol(suit);
        float suitX = (w - paint.measureText(suitSymbol)) / 2f;
        canvas.drawText(suitSymbol, suitX, h * 0.90f, paint);

        // Draw small rank and suit in bottom-right (upside down)
        canvas.save();
        canvas.rotate(180, w / 2f, h / 2f);
        paint.setTextSize(h * 0.25f);
        canvas.drawText(formatRank(rank), w * 0.1f, h * 0.25f, paint);
        canvas.restore();
    }

    /**
     * Draw the back of the card
     */
    private void drawCardBack(Canvas canvas, int w, int h, float cornerRadius) {
        Path path = new Path();
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);

        // Draw gradient background
        Shader backShader = new LinearGradient(
                0, 0, w, h,
                ContextCompat.getColor(getContext(), R.color.my_dark_grey),
                ContextCompat.getColor(getContext(), R.color.my_light_grey),
                Shader.TileMode.CLAMP
        );
        paint.setShader(backShader);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);

        // Draw decorative pattern
        paint.setShader(null);
        paint.setColor(Color.WHITE);
        paint.setTextSize(h * 0.4f);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("★", w / 2f, h / 2f + h * 0.15f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * Draw selection indicator (gold border with glow effect)
     */
    private void drawSelectionIndicator(Canvas canvas, int w, int h, float cornerRadius) {
        // Draw outer glow
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(12f);
        selectionPaint.setColor(Color.argb(100, 255, 215, 0)); // Semi-transparent gold
        RectF glowRect = new RectF(6, 6, w - 6, h - 6);
        canvas.drawRoundRect(glowRect, cornerRadius, cornerRadius, selectionPaint);

        // Draw main border
        selectionPaint.setStrokeWidth(8f);
        selectionPaint.setColor(selectionColor); // Solid gold
        RectF borderRect = new RectF(4, 4, w - 4, h - 4);
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, selectionPaint);

        // Draw inner highlight
        selectionPaint.setStrokeWidth(3f);
        selectionPaint.setColor(Color.argb(200, 255, 255, 100)); // Bright yellow
        RectF innerRect = new RectF(8, 8, w - 8, h - 8);
        canvas.drawRoundRect(innerRect, cornerRadius - 2, cornerRadius - 2, selectionPaint);
    }

    // ==================== Helper Methods ====================

    /**
     * Format the rank for display
     */
    private String formatRank(String rank) {
        try {
            int value = Integer.parseInt(rank);
            switch (value) {
                case 11: return "J";
                case 12: return "Q";
                case 13: return "K";
                case 14: return "A";
                default: return String.valueOf(value);
            }
        } catch (NumberFormatException e) {
            // Fallback for non-numeric ranks
            switch (rank.toLowerCase()) {
                case "a": return "A";
                case "k": return "K";
                case "q": return "Q";
                case "j": return "J";
                default: return rank.toUpperCase();
            }
        }
    }

    /**
     * Get the Unicode symbol for the suit
     */
    private String getSuitSymbol(String suit) {
        switch (suit.toLowerCase()) {
            case "h": return "\u2665\uFE0E"; // Heart
            case "d": return "\u2666\uFE0E"; // Diamond
            case "c": return "\u2663\uFE0E"; // Club
            case "s": return "\u2660\uFE0E"; // Spade
            default: return "?";
        }
    }
}