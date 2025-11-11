package com.example.androidexample.blackjack;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.androidexample.R;
import com.example.androidexample.blackjack.BlackjackModels.CardState;
import com.example.androidexample.blackjack.BlackjackModels.DealerState;
import com.example.androidexample.blackjack.BlackjackModels.HandState;
import com.example.androidexample.blackjack.BlackjackModels.PlayerState;
import com.example.androidexample.services.CardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for rendering card visuals and related UI elements
 * (like value and bet pills) for both players and the dealer.
 *
 * This class encapsulates layout/animation logic only — no game rules.
 */
public class CardRenderer {

    /** Android context for inflating views / resources. */
    private final Context context;

    /** Density multiplier for dp → px. */
    private final float density;

    public CardRenderer(Context context, float density) {
        this.context = context;
        this.density = density;
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────────
    // Old static render (No animation)
    // ─────────────────────────────────────────────────────────────────────────────────────────────
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

            int overlap = -dp(Math.min(12 + (cardCount - 2) * 6, 40));
            if (isSplit) overlap = (int) (overlap * 1.5f);

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
                int spacing = isSplit ? dp(40) : dp(16);
                handParams.setMargins(spacing, 0, spacing, 0);

                handContainer.setLayoutParams(handParams);
                container.addView(handContainer);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────────
    // Dealer
    // ─────────────────────────────────────────────────────────────────────────────────────────────
    public void renderDealer(LinearLayout container, DealerState dealer, boolean isRoundOver, List<CardState> previousDealerHand) {
        if (dealer == null || dealer.hand == null || dealer.hand.isEmpty()) return;
        if (previousDealerHand == null) previousDealerHand = new ArrayList<>();

        int oldCount = previousDealerHand.size();

        // Create or reuse dealer layout
        LinearLayout dealerLayout;
        if (container.getChildCount() == 0) {
            dealerLayout = new LinearLayout(context);
            dealerLayout.setOrientation(LinearLayout.VERTICAL);
            dealerLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            dealerLayout.setClipChildren(false);
            dealerLayout.setClipToPadding(false);
            container.addView(dealerLayout);
        } else {
            dealerLayout = (LinearLayout) container.getChildAt(0);
        }

        // Create or reuse card row
        LinearLayout row;
        if (dealerLayout.getChildCount() > 0 && dealerLayout.getChildAt(0) instanceof LinearLayout) {
            row = (LinearLayout) dealerLayout.getChildAt(0);
        } else {
            row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_HORIZONTAL);
            row.setClipChildren(false);
            row.setClipToPadding(false);
            dealerLayout.addView(row);
        }

        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        float offscreenY = -screenHeight * 0.25f;

        for (int i = 0; i < dealer.hand.size(); i++) {
            CardState card = dealer.hand.get(i);
            CardView cv;

            if (i < row.getChildCount()) {
                cv = (CardView) row.getChildAt(i);
                if (i < oldCount) {
                    CardState old = previousDealerHand.get(i);
                    if (!old.isShowing && card.isShowing && !cv.isFaceUp()) {
                        cv.flipCard();
                    }
                }
            } else {
                cv = new CardView(context);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(80), dp(120));
                p.setMargins(dp(6), 0, dp(6), 0);
                cv.setLayoutParams(p);
                cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);

                cv.setTranslationY(offscreenY);
                row.addView(cv);

                cv.animate()
                        .translationY(0f)
                        .setDuration(500)
                        .setStartDelay(i * 250)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f))
                        .start();
            }
        }

        int totalCards = row.getChildCount();
        if (totalCards > 1) {
            int overlap;
            if (totalCards <= 2) {
                overlap = dp(12);
            } else {
                overlap = Math.max(-dp(90 / totalCards), -dp(45));
            }

            for (int i = 0; i < totalCards; i++) {
                View cardView = row.getChildAt(i);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) cardView.getLayoutParams();
                if (i == 0) {
                    lp.setMargins(0, 0, 0, 0);
                } else {
                    lp.setMargins(overlap, 0, 0, 0);
                }
                cardView.setLayoutParams(lp);

                cardView.animate()
                        .translationX(0f)
                        .setDuration(250)
                        .setStartDelay(500 + (i * 50))
                        .start();
            }
        }

        if (!isRoundOver) {
            if (dealerLayout.getChildCount() > 1 && dealerLayout.getChildAt(1) instanceof TextView) {
                dealerLayout.removeViewAt(1);
            }
        }

        if (isRoundOver) {
            long delay = 600 + ((dealer.hand.size() - 1) * 600);

            row.postDelayed(() -> {
                TextView valuePill;
                if (dealerLayout.getChildCount() > 1 && dealerLayout.getChildAt(1) instanceof TextView) {
                    valuePill = (TextView) dealerLayout.getChildAt(1);
                    valuePill.setText(String.valueOf(dealer.handValue));
                } else {
                    valuePill = makePillText(
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
                valuePill.setAlpha(1f);
                valuePill.requestLayout();
                valuePill.invalidate();
            }, delay);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────────
    // Public utility: animate everything off (used between rounds).
    // ─────────────────────────────────────────────────────────────────────────────────────────────
    public void animateAllCardsOut(LinearLayout containerRoot) {
        if (containerRoot == null) return;
        animateGroupCardsOut(containerRoot);
    }

    private void animateGroupCardsOut(ViewGroup group) {
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

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            int screenWidth = group.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = group.getResources().getDisplayMetrics().heightPixels;

            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);

                if (child instanceof CardView) {
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

    // ─────────────────────────────────────────────────────────────────────────────────────────────
    // Player: Incremental, animated renderer (HIT/FLIP/SPLIT/RE-SPLIT)
    // ─────────────────────────────────────────────────────────────────────────────────────────────
    /**
     * Animated rendering for player hands.
     * Incremental updates only (no full redraw):
     *  - Reuses CardViews in-place; new cards are dealt from above.
     *  - Flips when a hidden card becomes visible.
     *  - On split/re-split, moves the CardView from donor hand to recipient hand (no cloning).
     *  - Hands are spaced evenly and re-centered when returning to one hand.
     *  - Value/bet pills render only if a hand has at least one card.
     */
    public void renderPlayerHandsAnimated(LinearLayout container, PlayerState player, List<HandState> previous) {
        if (player == null || player.hands == null) return;
        if (previous == null) previous = new ArrayList<>();

        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        container.setClipChildren(false);
        container.setClipToPadding(false);
        // Defensive: container might have padding from layout; ensure it's not skewing centering
        container.setPadding(0, container.getPaddingTop(), 0, container.getPaddingBottom());

        final int currCount = player.hands.size();
        final int prevCount = previous.size();

        // Ensure hand containers & rows exist up to current count
        for (int h = 0; h < currCount; h++) {
            LinearLayout hc = getOrCreateHandContainer(container, h);
            zeroMargins(hc);                              // normalize margins every time
            LinearLayout row = getOrCreateCardRow(hc);
            zeroMargins(row);                             // normalize margins every time
        }

        // Hard reset when returning to a single hand (typical after new round)
        if (currCount == 1) {
            for (int i = 0; i < container.getChildCount(); i++) {
                View v = container.getChildAt(i);
                v.animate().cancel();
                v.setTranslationX(0f);
                zeroMargins(v);                           // CRITICAL: wipe old split margins
            }
            while (container.getChildCount() > 1) {
                container.removeViewAt(container.getChildCount() - 1);
            }
            container.requestLayout();
        }

        // Detect split/re-split: move the last card view from donor to recipient (no cloning)
        if (currCount > prevCount && prevCount > 0 && container.getChildCount() >= currCount) {
            int donorIdx = -1;
            int recipientIdx = -1;

            for (int i = 0; i < Math.min(prevCount, currCount); i++) {
                HandState oldH = previous.get(i);
                HandState newH = player.hands.get(i);
                int oldSize = (oldH != null && oldH.hand != null) ? oldH.hand.size() : 0;
                int newSize = (newH != null && newH.hand != null) ? newH.hand.size() : 0;
                if (oldSize - newSize == 1) donorIdx = i;
                if (newSize - oldSize == 1) recipientIdx = i;
            }
            if (recipientIdx == -1 && currCount > prevCount) {
                recipientIdx = currCount - 1;
            }

            if (donorIdx >= 0 && recipientIdx >= 0 &&
                    donorIdx < container.getChildCount() && recipientIdx < container.getChildCount()) {
                LinearLayout donorHC = (LinearLayout) container.getChildAt(donorIdx);
                LinearLayout recipHC = (LinearLayout) container.getChildAt(recipientIdx);
                LinearLayout donorRow = (LinearLayout) donorHC.getChildAt(0);
                LinearLayout recipRow = (LinearLayout) recipHC.getChildAt(0);

                if (donorRow.getChildCount() > 0) {
                    View moved = donorRow.getChildAt(donorRow.getChildCount() - 1);
                    donorRow.removeViewAt(donorRow.getChildCount() - 1);

                    float sign = (recipientIdx > donorIdx) ? 1f : -1f;
                    moved.setTranslationX(sign * -dp(140));
                    recipRow.addView(moved);

                    moved.animate()
                            .translationX(0f)
                            .setDuration(350)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.25f))
                            .start();
                }
            }
        }

        // Evenly position/center hands when more than one
        if (currCount > 1) {
            positionHandsEvenly(container, currCount, 500);
        }

        // Per-hand incremental updates
        for (int h = 0; h < currCount; h++) {
            HandState newHand = player.hands.get(h);
            HandState oldHand = (h < prevCount) ? previous.get(h) : null;

            LinearLayout handContainer = (LinearLayout) container.getChildAt(h);
            LinearLayout cardRow = (LinearLayout) handContainer.getChildAt(0);

            int oldSize = (oldHand != null && oldHand.hand != null) ? oldHand.hand.size() : 0;
            int newSize = (newHand.hand != null) ? newHand.hand.size() : 0;

            // Trim any extra views if server corrected size
            while (cardRow.getChildCount() > newSize) {
                View toRemove = cardRow.getChildAt(cardRow.getChildCount() - 1);
                cardRow.removeViewAt(cardRow.getChildCount() - 1);
            }

            // Update existing indices and append new cards
            for (int i = 0; i < newSize; i++) {
                CardState card = newHand.hand.get(i);

                if (i < cardRow.getChildCount()) {
                    CardView cv = (CardView) cardRow.getChildAt(i);
                    cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);
                    if (oldHand != null && i < oldSize) {
                        CardState oldCard = oldHand.hand.get(i);
                        if (!oldCard.isShowing && card.isShowing && !cv.isFaceUp()) {
                            cv.flipCard();
                        }
                    }
                } else {
                    CardView cv = new CardView(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(70), dp(105));
                    lp.setMargins(dp(6), 0, dp(6), 0);
                    cv.setLayoutParams(lp);
                    cv.setCard(String.valueOf(card.value), card.suit, card.isShowing);

                    int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                    float offscreenY = -screenHeight * 0.7f;
                    cv.setTranslationY(offscreenY);
                    cardRow.addView(cv);

                    cv.animate()
                            .translationY(0f)
                            .setDuration(520)
                            .setStartDelay(i * 160)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator(1.7f))
                            .start();
                }
            }

            // Recompute simple overlap fan
            int totalCards = cardRow.getChildCount();
            if (totalCards > 1) {
                int overlap = Math.max(-dp(50 / Math.max(totalCards, 1)), -dp(40));
                for (int i = 0; i < totalCards; i++) {
                    View v = cardRow.getChildAt(i);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
                    lp.setMargins(i == 0 ? 0 : overlap, 0, 0, 0);
                    v.setLayoutParams(lp);
                }
            }

            // Pills only when the hand actually has cards
            while (handContainer.getChildCount() > 1) handContainer.removeViewAt(1);
            if (newHand.hand != null && !newHand.hand.isEmpty()) {
                TextView valuePill = makePillText(
                        String.valueOf(newHand.handValue),
                        R.font.inter_bold, 18, Color.WHITE, 0.95f
                );
                LinearLayout.LayoutParams vParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                vParams.topMargin = dp(8);
                valuePill.setLayoutParams(vParams);
                handContainer.addView(valuePill);

                TextView betPill = makePillText(
                        "$" + newHand.bet,
                        R.font.inter_regular, 16, Color.LTGRAY, 0.8f
                );
                LinearLayout.LayoutParams bParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                bParams.topMargin = dp(4);
                betPill.setLayoutParams(bParams);
                handContainer.addView(betPill);
            }
        }

        // Safety: trim any extra hand containers
        while (container.getChildCount() > currCount) {
            container.removeViewAt(container.getChildCount() - 1);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────────────────────────
    /** Creates a pill-styled text view. */
    private TextView makePillText(String text, int fontRes, int sizeSp, int color, float alpha) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        tv.setTypeface(ResourcesCompat.getFont(context, fontRes));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(24), dp(8), dp(24), dp(8));
        tv.setBackgroundResource(R.drawable.bj_hand_value_pill);
        tv.setAlpha(alpha);
        return tv;
    }

    /** dp → px helper. */
    private int dp(int v) {
        return (int) (v * density);
    }

    /** Ensure a vertical hand container exists at a specific index. */
    private LinearLayout getOrCreateHandContainer(LinearLayout parent, int index) {
        if (parent.getChildCount() > index && parent.getChildAt(index) instanceof LinearLayout) {
            return (LinearLayout) parent.getChildAt(index);
        }
        LinearLayout hand = new LinearLayout(context);
        hand.setOrientation(LinearLayout.VERTICAL);
        hand.setGravity(Gravity.CENTER_HORIZONTAL);
        hand.setClipChildren(false);
        hand.setClipToPadding(false);

        // Insert at the desired index with clean layout params (no margins)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, 0);
        if (parent.getChildCount() >= index) parent.addView(hand, index, lp);
        else parent.addView(hand, lp);
        return hand;
    }

    /** Ensure child-0 of a hand container is a horizontal card row. */
    private LinearLayout getOrCreateCardRow(LinearLayout handContainer) {
        if (handContainer.getChildCount() > 0 && handContainer.getChildAt(0) instanceof LinearLayout) {
            return (LinearLayout) handContainer.getChildAt(0);
        }
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_HORIZONTAL);
        row.setClipChildren(false);
        row.setClipToPadding(false);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(0, 0, 0, 0);
        handContainer.addView(row, 0, lp);
        return row;
    }

    /** Evenly distribute hand containers around center using translationX. */
    private void positionHandsEvenly(LinearLayout container, int count, long durationMs) {
        if (count <= 0) return;
        float spread = dp(110); // distance between each hand's center
        float totalWidth = spread * (count - 1);
        float startX = -totalWidth / 2f;

        for (int h = 0; h < container.getChildCount(); h++) {
            View v = container.getChildAt(h);
            if (h >= count) break;

            // Ensure no residual margins can bias centering
            zeroMargins(v);

            float targetX = startX + h * spread;
            v.animate()
                    .translationX(targetX)
                    .setDuration(durationMs)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.25f))
                    .start();
        }
    }

    /** Zero out margins on a view if it has LinearLayout.LayoutParams. */
    private void zeroMargins(View v) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams ll = (LinearLayout.LayoutParams) lp;
            if (ll.leftMargin != 0 || ll.rightMargin != 0 || ll.topMargin != 0 || ll.bottomMargin != 0) {
                ll.setMargins(0, 0, 0, 0);
                v.setLayoutParams(ll);
            }
        }
    }
}
