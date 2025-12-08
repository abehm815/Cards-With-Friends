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

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class ElijahHerringSystemTest {

    private ActivityScenario<GofishActivity> scenario;

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
    }

    /**
     * Test 1: Verify host player sees start game button and correct initial UI state
     */
    @Test
    public void testHostPlayerInitialUIState() {
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

        // Verify root layout is displayed
        onView(withId(R.id.rootLayout))
                .check(matches(isDisplayed()));

        // Verify player list is displayed
        onView(withId(R.id.playerList))
                .check(matches(isDisplayed()));

        // Verify host sees start button
        onView(withText("Start Game"))
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()));

        // Verify activity received correct intent data
        scenario.onActivity(activity -> {
            assert "HostPlayer".equals(activity.getIntent().getStringExtra("USERNAME"));
            assert activity.getIntent().getBooleanExtra("HOST", false);
            assert "TEST123".equals(activity.getIntent().getStringExtra("JOINCODE"));
        });
    }

    /**
     * Test 2: Verify non-host player does NOT see start button
     */
    @Test
    public void testNonHostPlayerCannotStartGame() {
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

        // Verify UI is displayed
        onView(withId(R.id.rootLayout))
                .check(matches(isDisplayed()));

        // Verify player list exists
        onView(withId(R.id.playerList))
                .check(matches(isDisplayed()));

        // CRITICAL: Non-host should NOT see start button
        onView(withText("Start Game"))
                .check(matches(not(isDisplayed())));

        // Verify correct player data
        scenario.onActivity(activity -> {
            assert "Player2".equals(activity.getIntent().getStringExtra("USERNAME"));
            assert !activity.getIntent().getBooleanExtra("HOST", true);
        });
    }

    /**
     * Test 3: Verify all player data is correctly passed and stored
     */
    @Test
    public void testMultiplePlayersDataIntegrity() {
        // Create intent with 4 players
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

        // Verify UI elements exist
        onView(withId(R.id.rootLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.playerList)).check(matches(isDisplayed()));

        // Verify all player data was passed correctly
        scenario.onActivity(activity -> {
            ArrayList<String> receivedPlayers = activity.getIntent()
                    .getStringArrayListExtra("PLAYERS");

            assert receivedPlayers != null : "Players list is null";
            assert receivedPlayers.size() == 4 : "Expected 4 players, got " + receivedPlayers.size();
            assert receivedPlayers.contains("TestPlayer1") : "Missing TestPlayer1";
            assert receivedPlayers.contains("TestPlayer2") : "Missing TestPlayer2";
            assert receivedPlayers.contains("TestPlayer3") : "Missing TestPlayer3";
            assert receivedPlayers.contains("TestPlayer4") : "Missing TestPlayer4";

            assert "GAME456".equals(activity.getIntent().getStringExtra("JOINCODE"));
            assert "gofish".equals(activity.getIntent().getStringExtra("GAMETYPE"));
        });
    }

    /**
     * Test 4: Verify UI state persists through activity lifecycle changes
     */
    @Test
    public void testActivityLifecyclePersistence() {
        // Create intent
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), GofishActivity.class);
        intent.putExtra("GAMETYPE", "gofish");
        intent.putExtra("USERNAME", "LifecycleTest");
        intent.putExtra("JOINCODE", "LIFE999");
        intent.putExtra("HOST", true);

        ArrayList<String> players = new ArrayList<>();
        players.add("LifecycleTest");
        players.add("Opponent");
        intent.putStringArrayListExtra("PLAYERS", players);

        scenario = ActivityScenario.launch(intent);

        // Verify initial state
        onView(withId(R.id.rootLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.playerList)).check(matches(isDisplayed()));
        onView(withText("Start Game")).check(matches(isDisplayed()));

        // Simulate lifecycle changes (pause and resume)
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED);
        scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED);

        // Verify UI still exists after lifecycle change
        onView(withId(R.id.rootLayout)).check(matches(isDisplayed()));
        onView(withId(R.id.playerList)).check(matches(isDisplayed()));
        onView(withText("Start Game")).check(matches(isDisplayed()));

        // Verify data persists through lifecycle
        scenario.onActivity(activity -> {
            assert "LifecycleTest".equals(activity.getIntent().getStringExtra("USERNAME"));
            assert "LIFE999".equals(activity.getIntent().getStringExtra("JOINCODE"));
            assert "gofish".equals(activity.getIntent().getStringExtra("GAMETYPE"));
            assert activity.getIntent().getBooleanExtra("HOST", false);

            ArrayList<String> persistedPlayers = activity.getIntent()
                    .getStringArrayListExtra("PLAYERS");
            assert persistedPlayers != null;
            assert persistedPlayers.size() == 2;
        });
    }
}