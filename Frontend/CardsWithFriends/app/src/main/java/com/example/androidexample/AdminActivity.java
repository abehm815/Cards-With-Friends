package com.example.androidexample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AdminActivity extends AppCompatActivity{

    private EditText usernameText;
    private EditText gameText;
    private EditText statToChangeText;
    private EditText newStatText;
    private Button statUpdateButton;

    // Delete account elements
    private EditText deleteUsernameText;
    private Button deleteAccountButton;

    // Chips management elements
    private EditText chipsUsernameText;
    private EditText chipsAmountText;
    private Button addChipsButton;
    private Button removeChipsButton;

    // Lobby management elements
    private Button refreshLobbiesButton;
    private LinearLayout lobbiesContainer;
    private List<LobbyInfo> currentLobbies = new ArrayList<>();

    private Button deleteEmptyLobbiesButton;

    private static final String BASE_URL = "http://coms-3090-006.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Existing elements
        usernameText = findViewById(R.id.admin_user_input);
        gameText = findViewById(R.id.admin_game_input);
        statToChangeText = findViewById(R.id.admin_stat_input);
        newStatText = findViewById(R.id.admin_new_stat_input);
        statUpdateButton = findViewById(R.id.admin_update_stats_button);

        // Delete account elements
        deleteUsernameText = findViewById(R.id.admin_delete_username_input);
        deleteAccountButton = findViewById(R.id.admin_delete_account_button);

        // Chips management elements
        chipsUsernameText = findViewById(R.id.admin_chips_username_input);
        chipsAmountText = findViewById(R.id.admin_chips_amount_input);
        addChipsButton = findViewById(R.id.admin_add_chips_button);
        removeChipsButton = findViewById(R.id.admin_remove_chips_button);

        // Lobby management elements
        refreshLobbiesButton = findViewById(R.id.admin_refresh_lobbies_button);
        lobbiesContainer = findViewById(R.id.admin_lobbies_container);

        // Existing listeners
        statUpdateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                updateUserStat(usernameText.getText().toString(), gameText.getText().toString(),
                        statToChangeText.getText().toString(), newStatText.getText().toString());
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = deleteUsernameText.getText().toString().trim();
                if (username.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                    return;
                }
                showDeleteConfirmationDialog(username);
            }
        });

        addChipsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = chipsUsernameText.getText().toString().trim();
                String amountStr = chipsAmountText.getText().toString().trim();

                if (username.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Please enter username and amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(AdminActivity.this, "Amount must be positive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    modifyChips(username, amount, true);
                } catch (NumberFormatException e) {
                    Toast.makeText(AdminActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        removeChipsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = chipsUsernameText.getText().toString().trim();
                String amountStr = chipsAmountText.getText().toString().trim();

                if (username.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(AdminActivity.this, "Please enter username and amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int amount = Integer.parseInt(amountStr);
                    if (amount <= 0) {
                        Toast.makeText(AdminActivity.this, "Amount must be positive", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    modifyChips(username, amount, false);
                } catch (NumberFormatException e) {
                    Toast.makeText(AdminActivity.this, "Invalid amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // New lobby refresh listener
        refreshLobbiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadActiveLobbies();
            }
        });

        deleteEmptyLobbiesButton = findViewById(R.id.admin_delete_empty_lobbies_button);

        deleteEmptyLobbiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEmptyLobbies();
            }
        });

        // Load lobbies on startup
        loadActiveLobbies();
    }

    // ============= LOBBY MANAGEMENT METHODS =============

    private void deleteEmptyLobbies() {
        String url = BASE_URL + "/Lobby/empty";

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            String message = json.getString("message");
                            Toast.makeText(AdminActivity.this, message, Toast.LENGTH_LONG).show();
                            loadActiveLobbies(); // Refresh the list
                        } catch (Exception e) {
                            Toast.makeText(AdminActivity.this, "Empty lobbies cleaned up", Toast.LENGTH_SHORT).show();
                            loadActiveLobbies();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AdminActivity", "Error deleting empty lobbies: " + error.toString());
                        Toast.makeText(AdminActivity.this, "Failed to delete empty lobbies", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Loads all active lobbies from the server
     */
    private void loadActiveLobbies() {
        String url = BASE_URL + "/Lobby";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        currentLobbies.clear();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject lobbyJson = response.getJSONObject(i);
                                LobbyInfo lobby = new LobbyInfo();
                                lobby.lobbyID = lobbyJson.getLong("lobbyID");
                                lobby.joinCode = lobbyJson.getString("joinCode");
                                lobby.gameType = lobbyJson.getString("gameType");

                                // Parse users array
                                JSONArray usersArray = lobbyJson.getJSONArray("users");
                                for (int j = 0; j < usersArray.length(); j++) {
                                    JSONObject userJson = usersArray.getJSONObject(j);
                                    lobby.usernames.add(userJson.getString("username"));
                                }

                                currentLobbies.add(lobby);
                            }
                            displayLobbies();
                            Toast.makeText(AdminActivity.this, "Loaded " + currentLobbies.size() + " lobbies", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Log.e("AdminActivity", "Error parsing lobbies: " + e.toString());
                            Toast.makeText(AdminActivity.this, "Error parsing lobby data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AdminActivity", "Error loading lobbies: " + error.toString());
                        Toast.makeText(AdminActivity.this, "Failed to load lobbies", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Displays all lobbies in the UI
     */
    private void displayLobbies() {
        lobbiesContainer.removeAllViews();

        if (currentLobbies.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No active lobbies");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(14);
            lobbiesContainer.addView(emptyText);
            return;
        }

        for (LobbyInfo lobby : currentLobbies) {
            View lobbyView = createLobbyView(lobby);
            lobbiesContainer.addView(lobbyView);
        }
    }

    /**
     * Creates a view for a single lobby
     */
    private View createLobbyView(LobbyInfo lobby) {
        LinearLayout lobbyLayout = new LinearLayout(this);
        lobbyLayout.setOrientation(LinearLayout.VERTICAL);
        lobbyLayout.setPadding(24, 16, 24, 16);
        lobbyLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        lobbyLayout.setLayoutParams(params);

        // Lobby header
        TextView headerText = new TextView(this);
        headerText.setText(lobby.gameType + " - Code: " + lobby.joinCode);
        headerText.setTextSize(16);
        headerText.setTextColor(0xFF1976D2);
        headerText.setPadding(0, 0, 0, 8);
        lobbyLayout.addView(headerText);

        // Player count
        TextView countText = new TextView(this);
        countText.setText(lobby.usernames.size() + " player(s) in lobby");
        countText.setTextSize(12);
        countText.setTextColor(0xFF666666);
        countText.setPadding(0, 0, 0, 8);
        lobbyLayout.addView(countText);

        // Players list
        for (String username : lobby.usernames) {
            LinearLayout playerLayout = new LinearLayout(this);
            playerLayout.setOrientation(LinearLayout.HORIZONTAL);
            playerLayout.setPadding(0, 4, 0, 4);

            TextView playerText = new TextView(this);
            playerText.setText("• " + username);
            playerText.setTextSize(14);
            playerText.setLayoutParams(new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));
            playerLayout.addView(playerText);

            Button kickButton = new Button(this);
            kickButton.setText("Remove");
            kickButton.setTextSize(12);
            kickButton.setBackgroundColor(0xFFFF5722);
            kickButton.setTextColor(0xFFFFFFFF);
            kickButton.setPadding(16, 8, 16, 8);
            kickButton.setOnClickListener(v -> showKickConfirmation(lobby, username));
            playerLayout.addView(kickButton);

            lobbyLayout.addView(playerLayout);
        }

        return lobbyLayout;
    }

    /**
     * Shows confirmation dialog before kicking a player
     */
    private void showKickConfirmation(LobbyInfo lobby, String username) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Player")
                .setMessage("Remove " + username + " from lobby " + lobby.joinCode + "?")
                .setPositiveButton("Remove", (dialog, which) -> removePlayerFromLobby(lobby, username))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Removes a player from a lobby
     */
    private void removePlayerFromLobby(LobbyInfo lobby, String username) {
        String url = BASE_URL + "/Lobby/" + lobby.lobbyID + "/" + username;

        StringRequest request = new StringRequest(
                Request.Method.PUT,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(AdminActivity.this, username + " removed from lobby", Toast.LENGTH_SHORT).show();
                        loadActiveLobbies(); // Refresh the list
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AdminActivity", "Error removing player: " + error.toString());
                        Toast.makeText(AdminActivity.this, "Failed to remove player", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Helper class to store lobby information
     */
    private static class LobbyInfo {
        long lobbyID;
        String joinCode;
        String gameType;
        List<String> usernames = new ArrayList<>();
    }

    // ============= EXISTING METHODS (Delete Account, Chips, Stats) =============

    private void showDeleteConfirmationDialog(String username) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete the account for user '" + username + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount(username))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount(String username) {
        String url = BASE_URL + "/AppUser/username/" + username;

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(AdminActivity.this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                        deleteUsernameText.setText("");
                        Log.d("AdminActivity", "User deleted: " + username);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminActivity.this, "Failed to delete account. User may not exist.", Toast.LENGTH_LONG).show();
                        Log.e("AdminActivity", "Delete failed: " + error.toString());
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }

    private void modifyChips(String username, int amount, boolean isAdd) {
        String url = BASE_URL + "/AppUser/username/" + username;

        JsonObjectRequest getUserRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int currentChips = response.getInt("chips");
                            int newChips;

                            if (isAdd) {
                                newChips = currentChips + amount;
                            } else {
                                newChips = currentChips - amount;
                                if (newChips < 0) {
                                    Toast.makeText(AdminActivity.this,
                                            "Cannot remove " + amount + " chips. User only has " + currentChips + " chips.",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                            updateChips(username, newChips, amount, isAdd);

                        } catch (Exception e) {
                            Toast.makeText(AdminActivity.this, "Error processing user data", Toast.LENGTH_SHORT).show();
                            Log.e("AdminActivity", "Error parsing user: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        Log.e("AdminActivity", "Error fetching user: " + error.toString());
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(getUserRequest);
    }

    private void updateChips(String username, int newChips, int amount, boolean isAdd) {
        String url = BASE_URL + "/AppUser/username/" + username;

        JSONObject body = new JSONObject();
        try {
            body.put("chips", newChips);
            body.put("username", username);
            body.put("password", "");
            body.put("email", "");
            body.put("firstName", "");
            body.put("lastName", "");
            body.put("age", "");
        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String action = isAdd ? "added to" : "removed from";
                        Toast.makeText(AdminActivity.this,
                                amount + " chips " + action + " " + username + "'s account",
                                Toast.LENGTH_LONG).show();
                        chipsAmountText.setText("");
                        Log.d("AdminActivity", "Chips updated for " + username + ": new total = " + newChips);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AdminActivity.this, "Failed to update chips", Toast.LENGTH_LONG).show();
                        Log.e("AdminActivity", "Chip update failed: " + error.toString());
                    }
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }

    private void updateUserStat(String user, String game, String statToChange, String newStat){
        String url = BASE_URL + "/AppUser/username/" + user;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int userID = response.getInt("userID");
                        updateUserStatFromID(userID, game, statToChange, newStat);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Volley", "Error fetching user: " + error.toString());
                    Toast.makeText(this, "Failed to fetch userID", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateUserStatFromID(int userID, String game, String statToChange, String newStat){
        String url = BASE_URL + "/UserStats/" + userID + "/" + game + "/set/" + statToChange + "/" + newStat;

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(AdminActivity.this, "Stat updated successfully", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Update Failed", Toast.LENGTH_LONG).show();
                    }
                }
        );

        VolleySingleton.getInstance(AdminActivity.this).addToRequestQueue(postRequest);
    }
}