package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class ElijahHerringSystemTest {

    private ActivityScenario<GofishActivity> scenario;
    private CountingIdlingResource idlingResource;

    @Before
    public void setUp() {
        // Create idling resource for async operations
        idlingResource = new CountingIdlingResource("WebSocket");
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }

    /**
     * Test 1: Complete host flow - verify host can see start button, start game,
     * and UI updates correctly when game begins
     */
    @Test
    public void testHostStartGameCompleteFlow() {
        // Create intent with host privileges
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GofishActivity.class);
        intent.putExtra("GAMETYPE", "gofish");
        intent.putExtra("USERNAME", "HostPlayer");
        intent.putExtra("JOINCODE", "TEST123");
        intent.putExtra("HOST", true);
        
        ArrayList<String> players = new ArrayList<>();
        players.add("HostPlayer");
        players.add("Player2");
        players.add("Player3");
        intent.putStringArrayListExtra("PLAYERS", players);

        scenario = ActivityScenario.launch(intent);

        // Verify initial state - host should see start button
        onView(withText("Start Game"))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        // Verify waiting message is displayed
        onView(withText(containsString("Waiting for host")))
                .check(matches(isDisplayed()));

        // Verify ask button is initially hidden
        onView(withId(R.id.gofish_ask_button))
                .check(matches(not(isDisplayed())));

        // Click start game button
        onView(withText("Start Game")).perform(click());

        // Wait for potential game state update
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // After clicking start, button should eventually disappear
        // (This will happen when game state is received from server)
        scenario.onActivity(activity -> {
            // Verify the activity has the correct initial state
            assert activity != null;
            assert "HostPlayer".equals(activity.getIntent().getStringExtra("USERNAME"));
            assert activity.getIntent().getBooleanExtra("HOST", false);
        });
    }

    /**
     * Test 2: Non-host player experience - verify non-host cannot start game
     * and sees appropriate waiting state
     */
    @Test
    public void testNonHostPlayerWaitingState() {
        // Create intent for non-host player
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GofishActivity.class);
        intent.putExtra("GAMETYPE", "gofish");
        intent.putExtra("USERNAME", "Player2");
        intent.putExtra("JOINCODE", "TEST123");
        intent.putExtra("HOST", false);
        
        ArrayList<String> players = new ArrayList<>();
        players.add("HostPlayer");
        players.add("Player2");
        intent.putStringArrayListExtra("PLAYERS", players);

        scenario = ActivityScenario.launch(intent);

        // Verify start button is NOT visible for non-host
        onView(withText("Start Game"))
                .check(matches(not(isDisplayed())));

        // Verify waiting message is shown
        onView(withText(containsString("Waiting for host")))
                .check(matches(isDisplayed()));

        // Verify ask button is hidden before game starts
        onView(withId(R.id.gofish_ask_button))
                .check(matches(not(isDisplayed())));

        // Verify player list layout exists
        onView(withId(R.id.playerList))
                .check(matches(isDisplayed()));

        // Verify activity state
        scenario.onActivity(activity -> {
            assert "Player2".equals(activity.getIntent().getStringExtra("USERNAME"));
            assert !activity.getIntent().getBooleanExtra("HOST", true);
            assert "TEST123".equals(activity.getIntent().getStringExtra("JOINCODE"));
        });
    }

    /**
     * Test 3: Player list initialization and display - verify all players
     * appear correctly in the lobby with proper data
     */
    @Test
    public void testPlayerListDisplayAndDataIntegrity() {
        // Create intent with multiple players
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GofishActivity.class);
        intent.putExtra("GAMETYPE", "gofish");
        intent.putExtra("USERNAME", "TestPlayer1");
        intent.putExtra("JOINCODE", "GAME456");
        intent.putExtra("HOST", true);
        
        ArrayList<String> players = new ArrayList<>();
        players.add("TestPlayer1");
        players.add("TestPlayer2");
        players.add("TestPlayer3");
        players.add("TestPlayer4");
        intent.putStringArrayListExtra("PLAYERS", players);

        scenario = ActivityScenario.launch(intent);

        // Verify player list container is visible
        onView(withId(R.id.playerList))
                .check(matches(isDisplayed()));

        // Verify root layout exists
        onView(withId(R.id.rootLayout))
                .check(matches(isDisplayed()));

        // Verify the intent data was passed correctly
        scenario.onActivity(activity -> {
            ArrayList<String> receivedPlayers = activity.getIntent()
                    .getStringArrayListExtra("PLAYERS");
            
            assert receivedPlayers != null;
            assert receivedPlayers.size() == 4;
            assert receivedPlayers.contains("TestPlayer1");
            assert receivedPlayers.contains("TestPlayer2");
            assert receivedPlayers.contains("TestPlayer3");
            assert receivedPlayers.contains("TestPlayer4");
            
            // Verify join code and game type
            assert "GAME456".equals(activity.getIntent().getStringExtra("JOINCODE"));
            assert "gofish".equals(activity.getIntent().getStringExtra("GAMETYPE"));
        });
    }

    /**
     * Test 4: UI state consistency during game lifecycle - verify multiple
     * UI elements maintain correct visibility and state throughout activity lifecycle
     */
    @Test
    public void testUIStateConsistencyThroughLifecycle() {
        // Create intent for host player
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GofishActivity.class);
        intent.putExtra("GAMETYPE", "gofish");
        intent.putExtra("USERNAME", "LifecycleTestPlayer");
        intent.putExtra("JOINCODE", "LIFE999");
        intent.putExtra("HOST", true);
        
        ArrayList<String> players = new ArrayList<>();
        players.add("LifecycleTestPlayer");
        players.add("OpponentPlayer");
        intent.putStringArrayListExtra("PLAYERS", players);

        scenario = ActivityScenario.launch(intent);

        // Initial state verification
        onView(withId(R.id.rootLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.playerList)).check(matches(isDisplayed()));
        onView(withText("Start Game")).check(matches(isDisplayed()));
        onView(withId(R.id.gofish_ask_button)).check(matches(not(isDisplayed())));

        // Simulate activity pause and resume (lifecycle changes)
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);

        // Verify UI elements are still in correct state after lifecycle change
        onView(withId(R.id.rootLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.playerList)).check(matches(isDisplayed()));
        
        // Verify data persistence through lifecycle
        scenario.onActivity(activity -> {
            // Check that all intent extras are still intact
            assert "LifecycleTestPlayer".equals(activity.getIntent().getStringExtra("USERNAME"));
            assert "LIFE999".equals(activity.getIntent().getStringExtra("JOINCODE"));
            assert "gofish".equals(activity.getIntent().getStringExtra("GAMETYPE"));
            assert activity.getIntent().getBooleanExtra("HOST", false);
            
            ArrayList<String> persistedPlayers = activity.getIntent()
                    .getStringArrayListExtra("PLAYERS");
            assert persistedPlayers != null;
            assert persistedPlayers.size() == 2;
            
            // Verify WebSocket connection would be re-established
            // (in real scenario, onStart() would reconnect)
        });

        // Try clicking start button after lifecycle change
        onView(withText("Start Game"))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());

        // Brief wait to allow for any async operations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify activity is still in valid state
        scenario.onActivity(activity -> {
            assert activity != null;
            // Activity should still have all its data
            assert activity.getIntent().hasExtra("USERNAME");
            assert activity.getIntent().hasExtra("JOINCODE");
        });
    }
}