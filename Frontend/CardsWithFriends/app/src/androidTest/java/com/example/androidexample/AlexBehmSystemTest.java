package com.example.androidexample;

import android.content.Context;
import android.content.Intent;

import com.example.androidexample.R;
import com.example.androidexample.blackjack.BlackjackActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class AlexBehmSystemTest {

    private static final int SIMULATED_DELAY_MS_SHORT = 800;
    private static final int SIMULATED_DELAY_MS_LONG = 4500;

    private static Intent createBlackjackIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, BlackjackActivity.class);
        intent.putExtra("GAMETYPE", "BLACKJACK");
        intent.putExtra("USERNAME", "testUser");
        intent.putExtra("JOINCODE", "TEST123");
        intent.putExtra("HOST", false); // avoid auto-starting rounds from host logic
        return intent;
    }

    @Rule
    public ActivityScenarioRule<BlackjackActivity> activityRule =
            new ActivityScenarioRule<>(createBlackjackIntent());

    /**
     * Helper that simulates the WebSocket opening and waits for the betting panel
     * to become visible (the activity shows it after a 4 second delay).
     */
    private void openBettingPanelAndWait() {
        activityRule.getScenario().onActivity(activity -> {
            // Simulate successful WebSocket connection
            activity.onWebSocketOpen(null);
        });

        // Wait long enough for the panel to be shown by the delayed handler
        try {
            Thread.sleep(SIMULATED_DELAY_MS_LONG);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Helper that sends a simple game state with a single player and no cards.
     * Used to drive the betting UI logic.
     */
    private void sendSimpleBettingGameState(int chips, boolean hasBet) {
        activityRule.getScenario().onActivity(activity -> {
            try {
                JSONObject root = new JSONObject();
                root.put("lobbyCode", "TEST123");
                root.put("roundInProgress", false);
                // No active turn during betting
                root.put("currentTurn", JSONObject.NULL);

                // Dealer with no cards
                JSONObject dealer = new JSONObject();
                dealer.put("handValue", 0);
                dealer.put("hand", new JSONArray());
                root.put("dealer", dealer);

                // Single player representing the current user
                JSONObject player = new JSONObject();
                player.put("username", "testUser");
                player.put("chips", chips);
                player.put("hasBet", hasBet);

                // No cards yet, betting phase only
                JSONArray hands = new JSONArray();
                player.put("hands", hands);

                JSONArray players = new JSONArray();
                players.put(player);
                root.put("players", players);

                activity.onWebSocketMessage(root.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // Wait briefly for UI updates scheduled via runOnUiThread
        try {
            Thread.sleep(SIMULATED_DELAY_MS_SHORT);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Helper that sends a game state where it is the current user's turn and
     * they have one hand with cards and a bet.
     */
    private void sendTurnWithHandGameState(boolean canSplit) {
        activityRule.getScenario().onActivity(activity -> {
            try {
                JSONObject root = new JSONObject();
                root.put("lobbyCode", "TEST123");
                root.put("roundInProgress", true);
                root.put("currentTurn", "testUser");

                // Dealer with one visible card (value doesn't matter here)
                JSONObject dealer = new JSONObject();
                dealer.put("handValue", 10);
                JSONArray dealerHand = new JSONArray();
                JSONObject dealerCard = new JSONObject();
                dealerCard.put("value", 10);
                dealerCard.put("suit", "h");
                dealerCard.put("isShowing", true);
                dealerHand.put(dealerCard);
                dealer.put("hand", dealerHand);
                root.put("dealer", dealer);

                // Player with one hand, some cards, and a bet
                JSONObject player = new JSONObject();
                player.put("username", "testUser");
                player.put("chips", 100);
                player.put("hasBet", true);

                JSONArray handArray = new JSONArray();
                JSONObject hand = new JSONObject();
                hand.put("handIndex", 0);
                hand.put("handValue", 16);
                hand.put("bet", 20);
                hand.put("hasStood", false);
                hand.put("canSplit", canSplit);

                JSONArray cards = new JSONArray();
                JSONObject c1 = new JSONObject();
                c1.put("value", 8);
                c1.put("suit", "s");
                c1.put("isShowing", true);
                cards.put(c1);
                JSONObject c2 = new JSONObject();
                c2.put("value", 8);
                c2.put("suit", "d");
                c2.put("isShowing", true);
                cards.put(c2);
                hand.put("hand", cards);

                handArray.put(hand);
                player.put("hands", handArray);

                JSONArray players = new JSONArray();
                players.put(player);
                root.put("players", players);

                activity.onWebSocketMessage(root.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            Thread.sleep(SIMULATED_DELAY_MS_SHORT);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Entering an empty bet should show an "Enter an amount" error on the bet input field.
     */
    @Test
    public void emptyBetShowsError() {
        openBettingPanelAndWait();
        sendSimpleBettingGameState(100, false);

        // Ensure the bet input is reachable
        onView(withId(R.id.betInput))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Clear and click bet with empty text
        onView(withId(R.id.betInput)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.betButton)).perform(click());

        // Check the exact error text applied by BetHandler
        onView(withId(R.id.betInput))
                .check(matches(hasErrorText("Enter an amount")));
    }

    /**
     * Entering a bet larger than the available chip balance should show
     * a "Not enough balance!" error.
     */
    @Test
    public void betLargerThanBalanceShowsError() {
        openBettingPanelAndWait();
        // User has only 50 chips
        sendSimpleBettingGameState(50, false);

        onView(withId(R.id.betInput))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Try to bet more than the current chip count
        onView(withId(R.id.betInput))
                .perform(clearText(), typeText("200"), closeSoftKeyboard());
        onView(withId(R.id.betButton)).perform(click());

        onView(withId(R.id.betInput))
                .check(matches(hasErrorText("Not enough balance!")));
    }

    /**
     * Entering a valid bet amount should clear the input and hide the bet UI
     * (the input field and button).
     */
    @Test
    public void validBetHidesBetUi() {
        openBettingPanelAndWait();
        // User has plenty of chips and no bet yet
        sendSimpleBettingGameState(200, false);

        onView(withId(R.id.betInput))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Enter a valid bet within the chip amount
        onView(withId(R.id.betInput))
                .perform(clearText(), typeText("25"), closeSoftKeyboard());
        onView(withId(R.id.betButton)).perform(click());

        // Wait for BetHandler to hide the views
        try {
            Thread.sleep(SIMULATED_DELAY_MS_SHORT);
        } catch (InterruptedException e) {
            // ignore
        }

        // Bet UI should now be hidden
        onView(withId(R.id.betInput))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.betButton))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    /**
     * When it is the current user's turn and they have a hand with cards,
     * the action button row should be visible. The split button visibility
     * should follow the hand's canSplit flag.
     */
    @Test
    public void actionButtonsFollowTurnAndSplitState() {
        // First message: user's turn, canSplit = true
        sendTurnWithHandGameState(true);

        // Action button row should be visible when it is the user's turn and they have cards
        onView(withId(R.id.actionButtons))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Split button should be visible when canSplit is true
        onView(withId(R.id.btnSplit))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Second message: same situation but now canSplit = false
        sendTurnWithHandGameState(false);

        // Action buttons remain visible (still user's turn, still has cards)
        onView(withId(R.id.actionButtons))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

        // Split button should now be hidden
        onView(withId(R.id.btnSplit))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }
}
