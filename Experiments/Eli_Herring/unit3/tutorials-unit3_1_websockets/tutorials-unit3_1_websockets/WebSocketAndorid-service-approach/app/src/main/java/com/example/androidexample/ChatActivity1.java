package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.handshake.ServerHandshake;

public class ChatActivity1 extends AppCompatActivity {

    private Button sendBtn, backMainBtn;
    private EditText msgEtx;
    private TextView msgTv;
    private static final String TAG = "ChatActivity1";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat1);
        Log.d(TAG, "onCreate: ChatActivity1 initialized.");


        sendBtn = findViewById(R.id.sendBtn);
        backMainBtn = findViewById(R.id.backMainBtn);
        msgEtx = findViewById(R.id.msgEdt);
        msgTv = findViewById(R.id.tx1);

        // send button listener
        sendBtn.setOnClickListener(v -> {
            String message = msgEtx.getText().toString().trim();
            if (message.isEmpty()) return;

            Intent intent = new Intent("SendWebSocketMessage");
            intent.putExtra("key", "chat1");
            intent.putExtra("message", msgEtx.getText().toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            msgEtx.setText("");
        });

        backMainBtn.setOnClickListener(view -> {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
                Log.d(TAG, "Receiver manually unregistered on back press");
            } catch (Exception ignored) {}
            isReceiverRegistered = false;

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // ✅ Register receiver here (once)

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Unregister receiver here (once)

    }


    // For receiving messages
    // only react to messages with tag "chat1"
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String key = intent.getStringExtra("key");
            if ("chat1".equals(key)){
                String message = intent.getStringExtra("message");
                runOnUiThread(() -> {
                    String s = msgTv.getText().toString();
                    msgTv.setText(s + "\n" + message);
                });
            }
        }
    };



    private boolean isReceiverRegistered = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    messageReceiver, new IntentFilter("WebSocketMessageReceived"));
            isReceiverRegistered = true;
            Log.d(TAG, "Receiver registered");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isReceiverRegistered) {
            try {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
                Log.d(TAG, "Receiver unregistered");
            } catch (Exception e) {
                Log.w(TAG, "Receiver already unregistered or context mismatch");
            }
            isReceiverRegistered = false;
        }
        finish();
    }


}