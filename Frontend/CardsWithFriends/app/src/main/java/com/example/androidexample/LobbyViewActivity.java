package com.example.androidexample;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.androidexample.services.Message;
import com.example.androidexample.services.VolleySingleton;
import com.example.androidexample.services.WebSocketListener;
import com.example.androidexample.services.WebSocketManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;

import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LobbyViewActivity extends AppCompatActivity implements WebSocketListener {

    private String gameType;
    private String joinCode;
    private String username;
    private Boolean isHost;
    private TextView gameTypeTxt;
    private TextView joinCodeTxt;
    private LinearLayout userListLayout;
    private android.widget.Button deleteBtn;
    private android.widget.Button leaveBtn;
    private android.widget.Button startBtn;
    private int unreadMessages;
    private final List<Message> chat = new ArrayList<>();
    private final ArrayList<String> currentUsers = new ArrayList<>();

    // Chat UI
    private LinearLayout chatMessagesLayout;
    private TextView chatUnreadLabel;
    private ScrollView chatScroll;
    private BottomSheetDialog chatDialog;

    private static final String TAG = "LobbyViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobbyview);

        gameTypeTxt = findViewById(R.id.lobbyview_type_txt);
        joinCodeTxt = findViewById(R.id.lobbyview_code_txt);
        userListLayout = findViewById(R.id.lobbyview_user_list);
        deleteBtn = findViewById(R.id.lobbyview_close_btn);
        leaveBtn = findViewById(R.id.lobbyview_leave_btn);
        startBtn = findViewById(R.id.lobbyview_start_btn);
        chatUnreadLabel = findViewById(R.id.chat_unread_label);

        Intent intent = getIntent();
        gameType = intent.getStringExtra("GAMETYPE");
        username = intent.getStringExtra("USERNAME");
        joinCode = intent.getStringExtra("JOINCODE");


        isHost = intent.getBooleanExtra("HOST", false);
        if (!isHost) {
            startBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);
        }

        gameTypeTxt.setText(gameType);
        joinCodeTxt.setText(joinCode);

        getLobbyRequest();

        deleteBtn.setOnClickListener(v -> deleteLobbyRequest());
        leaveBtn.setOnClickListener(v -> leaveLobbyRequest());

        ImageView chatBtn = findViewById(R.id.chat_button);
        chatBtn.setOnClickListener(v -> openChatSheet());
        startBtn.setOnClickListener(v -> startGame());

        unreadMessages = 0;
    }

    @Override
    protected void onStart() {
        super.onStart();
        String wsUrl = "ws://coms-3090-006.class.las.iastate.edu:8080/ws/lobby/" + joinCode + "/" + username;
        WebSocketManager.getInstance().connectWebSocket(wsUrl);
        WebSocketManager.getInstance().setWebSocketListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatDialog != null && chatDialog.isShowing()) chatDialog.dismiss();
    }

    // -----------------------WEBSOCKET METHODS--------------------
    @Override
    public void onWebSocketMessage(String message) {
        runOnUiThread(() -> {
            try {
                JSONObject json = new JSONObject(message);
                String type = json.getString("type");
                String user = json.getString("username");
                String msgText = json.getString("message");

                switch (type) {
                    case "JOIN":
                        if (!currentUsers.contains(user)) {
                            currentUsers.add(user);
                            addUserCard(user);
                            updateHeaderCount();
                        }
                        break;

                    case "LEAVE":
                        currentUsers.remove(user);
                        removeUserCard(user);
                        updateHeaderCount();
                        break;

                    case "MESSAGE":
                        if (msgText.equals("/start")){
                            if (gameType.equals("BLACKJACK")) {
                                Intent i = new Intent(LobbyViewActivity.this, BlackjackActivity.class);
                                i.putExtra("GAMETYPE", gameType);
                                i.putExtra("USERNAME", username);
                                i.putExtra("JOINCODE", joinCode);
                                i.putExtra("HOST", isHost);
                                i.putStringArrayListExtra("PLAYERS", currentUsers);
                                startActivity(i);
                            }
                            //TODO IMPLEMENT THE STARTING OF OTHER GAMES
                        }
                        else {
                            unreadMessages += 1;
                            updateUnreadLabel();
                            Message newMessage = new Message(user, msgText, System.currentTimeMillis());
                            chat.add(newMessage);
                            if (chatDialog != null && chatDialog.isShowing()) {
                                addMessageBubble(user, msgText);
                            }
                            Log.d("Chat", "Received chat message: " + msgText);
                            break;
                        }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Bad JSON from WebSocket: " + message, e);
            }
        });
    }

    @Override public void onWebSocketOpen(ServerHandshake handshakedata) { Log.d(TAG, "WebSocket connection opened"); }
    @Override public void onWebSocketClose(int code, String reason, boolean remote) { Log.d(TAG, "WebSocket closed: " + reason); }
    @Override public void onWebSocketError(Exception ex) { Log.e(TAG, "WebSocket error", ex); }

    // -----------------------UI HELPERS--------------------
    private MaterialCardView createUserCard(String username) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        card.setLayoutParams(params);
        card.setRadius(20f);
        card.setCardElevation(8f);
        card.setCardBackgroundColor(Color.WHITE);

        TextView userText = new TextView(this);
        userText.setText(username);
        userText.setTextColor(Color.BLACK);
        userText.setTextSize(22);
        userText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
        userText.setPadding(0, 32, 0, 32);
        userText.setGravity(android.view.Gravity.CENTER);

        card.addView(userText);
        return card;
    }
    private void updateUnreadLabel() {
        if (chatUnreadLabel == null) return;
        if (unreadMessages > 0) {
            chatUnreadLabel.setText(unreadMessages + " unread");
            chatUnreadLabel.setVisibility(View.VISIBLE);
        } else {
            chatUnreadLabel.setVisibility(View.GONE);
        }
    }

    private void addUserCard(String username) { userListLayout.addView(createUserCard(username)); }

    private void removeUserCard(String username) {
        for (int i = 0; i < userListLayout.getChildCount(); i++) {
            if (userListLayout.getChildAt(i) instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) userListLayout.getChildAt(i);
                TextView t = (TextView) card.getChildAt(0);
                if (t.getText().toString().equals(username)) {
                    userListLayout.removeViewAt(i);
                    break;
                }
            }
        }
    }

    private void addHeader(int playerCount) {
        TextView countText = new TextView(this);
        countText.setText("Players in Lobby: " + playerCount);
        countText.setTextColor(Color.WHITE);
        countText.setTextSize(20);
        countText.setTypeface(ResourcesCompat.getFont(this, R.font.inter_bold));
        countText.setGravity(android.view.Gravity.CENTER);
        countText.setPadding(0, 8, 0, 24);
        userListLayout.addView(countText);
    }

    private void updateHeaderCount() {
        if (userListLayout.getChildCount() > 0 && userListLayout.getChildAt(0) instanceof TextView) {
            TextView countText = (TextView) userListLayout.getChildAt(0);
            countText.setText("Players in Lobby: " + currentUsers.size());
        }
    }

    // -----------------------HTTP METHODS--------------------
    private void getLobbyRequest() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + joinCode;

        JsonObjectRequest getRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray usersArray = response.getJSONArray("users");
                        currentUsers.clear();
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject userObj = usersArray.getJSONObject(i);
                            currentUsers.add(userObj.getString("username"));
                        }
                        userListLayout.removeAllViews();
                        addHeader(currentUsers.size());
                        for (String user : currentUsers) {
                            addUserCard(user);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing lobby JSON", e);
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Failed to get lobby info", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error: ", error);
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(getRequest);
    }

    private void deleteLobbyRequest() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + joinCode;
        JsonObjectRequest deleteRequest = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    Toast.makeText(getApplicationContext(), "Closed Lobby", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, JoinActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.putExtra("GAMETYPE", gameType);
                    startActivity(intent);
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Failed to delete lobby", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error: ", error);
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(deleteRequest);
    }

    private void leaveLobbyRequest() {
        String url = "http://coms-3090-006.class.las.iastate.edu:8080/Lobby/joinCode/" + joinCode + "/" + username;
        Log.d(TAG, "Leave URL: " + url);
        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                null,
                (Response.Listener<JSONObject>) response -> {
                    try {
                        if (response.getString("message").equals("success")) {
                            Toast.makeText(getApplicationContext(), "Lobby left!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(this, LobbyActivity.class);
                            intent.putExtra("USERNAME", username);
                            intent.putExtra("GAMETYPE", gameType);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Can't leave lobby", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> {
                    Toast.makeText(getApplicationContext(), "Failed to leave lobby", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Volley error: ", error);
                }
        );
        VolleySingleton.getInstance(this).addToRequestQueue(putRequest);
    }

    // -----------------------CHAT (BOTTOM SHEET)--------------------
    private void openChatSheet() {
        chatDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.chat_bottom_sheet, null);
        chatDialog.setContentView(sheetView);

        // Force full-height & expanded state
        chatDialog.setOnShowListener(dlg -> {
            View bottomSheet = chatDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.requestLayout();
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setFitToContents(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        chatDialog.show();

        unreadMessages = 0;
        updateUnreadLabel();

        // Hook up views
        chatMessagesLayout = sheetView.findViewById(R.id.chat_messages_layout);
        chatScroll = sheetView.findViewById(R.id.chat_scroll);
        EditText input = sheetView.findViewById(R.id.chat_edit_text);
        Button sendBtn = sheetView.findViewById(R.id.chat_send_btn);

        // Dump history
        for (Message msg : chat) addMessageBubble(msg.getSender(), msg.getText());

        // Send handler
        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                WebSocketManager.getInstance().sendMessage(text);
                input.setText("");
            }
        });
    }

    private void addMessageBubble(String sender, String text) {
        if (chatMessagesLayout == null) return;
        TextView msgView = new TextView(this);
        msgView.setText(sender + ": " + text);
        msgView.setTextColor(Color.WHITE);
        msgView.setTextSize(16f);
        int pad = (int) (8 * getResources().getDisplayMetrics().density);
        msgView.setPadding(pad, pad, pad, pad);
        chatMessagesLayout.addView(msgView);

        // Always scroll to bottom after adding
        if (chatScroll != null) {
            chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void startGame(){
        WebSocketManager.getInstance().sendMessage("/start");
    }
}