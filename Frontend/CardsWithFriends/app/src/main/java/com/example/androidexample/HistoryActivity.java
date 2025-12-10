package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.androidexample.services.BottomNavHelper;
import com.example.androidexample.services.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ProgressBar progress;
    private HistoryAdapter adapter;

    private final List<HistoryItem> items = new ArrayList<>();
    private String username;

    private static final String BASE = "http://coms-3090-006.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        username = getIntent().getStringExtra("USERNAME");
        BottomNavHelper.setupBottomNav(this, username);

        recycler = findViewById(R.id.history_recycler);
        progress = findViewById(R.id.history_progress);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(items);
        recycler.setAdapter(adapter);

        loadAll();
    }

    private void loadAll() {
        progress.setVisibility(View.VISIBLE);

        loadGame("/blackjack/history/player/" + username, "Blackjack");
        loadGame("/euchre/history/player/" + username, "Euchre");
        loadGame("/gofish/history/player/" + username, "GoFish");
        loadGame("/Crazy8/history/player/" + username, "Crazy8");
    }

    private void loadGame(String endpoint, String gameType) {
        String url = BASE + endpoint;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                arr -> parseGame(arr, gameType),
                err -> progress.setVisibility(View.GONE)
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void parseGame(JSONArray arr, String gameType) {
        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject match = arr.getJSONObject(i);

                String matchId = match.optString("matchId", "");
                String start = match.optString("startTime", "");
                String end = match.optString("endTime", "");
                String winner = match.optString("winner", "Unknown");

                JSONArray eventsArr = match.optJSONArray("events");
                List<String> events = new ArrayList<>();

                if (eventsArr != null) {
                    for (int j = 0; j < eventsArr.length(); j++) {
                        JSONObject e = eventsArr.getJSONObject(j);
                        events.add(formatEvent(e, gameType));
                    }
                }

                items.add(new HistoryItem(gameType, matchId, winner, start, end, events));
            }
        } catch (Exception ignored) {}

        adapter.notifyDataSetChanged();
        progress.setVisibility(View.GONE);
    }

    private String formatEvent(JSONObject e, String type) {

        switch (type) {

            case "Crazy8":
                return e.optString("timestamp") + " — "
                        + e.optString("player") + " "
                        + e.optString("action")
                        + " | Card: " + e.optString("cardPlayed");

            case "Blackjack":
                return e.optString("timestamp") + " — "
                        + e.optString("player") + " "
                        + e.optString("action")
                        + " | Card: " + e.optString("card")
                        + " | Hand=" + e.optInt("handValueAfter");

            case "Euchre":
                return e.optString("timestamp") + " — "
                        + e.optString("eventType")
                        + " | Player: " + e.optString("player")
                        + " | Card: " + e.optString("cardPlayed")
                        + " | TrickWinner: " + e.optString("trickWinner");

            case "GoFish":
                return e.optString("timestamp") + " — "
                        + e.optString("player") + " asked "
                        + e.optString("target") + " for "
                        + e.optInt("cardValue")
                        + " | Drew: " + e.optString("cardDrawn");

            default:
                return e.toString();
        }
    }

    // ---------------------------
    // Data wrapper
    // ---------------------------
    public static class HistoryItem {
        String gameType, matchId, winner, start, end;
        List<String> events;

        HistoryItem(String g, String id, String w, String s, String e, List<String> events) {
            gameType = g;
            matchId = id;
            winner = w;
            start = s;
            end = e;
            this.events = events;
        }
    }

    // ---------------------------
    // Recycler Adapter
    // ---------------------------
    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

        List<HistoryItem> list;

        HistoryAdapter(List<HistoryItem> l) { list = l; }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int pos) {
            HistoryItem item = list.get(pos);

            h.title.setText(item.gameType + " — Match " + item.matchId);
            h.time.setText(item.start);

            switch (item.gameType.toLowerCase()) {
                case "blackjack": h.root.setBackgroundResource(R.drawable.bg_blackjack); break;
                case "euchre":    h.root.setBackgroundResource(R.drawable.bg_euchre); break;
                case "gofish":    h.root.setBackgroundResource(R.drawable.bg_gofish); break;
                case "crazy8":    h.root.setBackgroundResource(R.drawable.bg_crazy8); break;
            }

            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(HistoryActivity.this, MatchDetailActivity.class);
                i.putExtra("events", item.events instanceof ArrayList ? (ArrayList<String>) item.events : new ArrayList<>(item.events));
                i.putExtra("title", item.gameType + " — " + item.matchId);
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class Holder extends RecyclerView.ViewHolder {
            ConstraintLayout root;
            TextView title, time;

            Holder(View v) {
                super(v);
                root = v.findViewById(R.id.history_card_root);
                title = v.findViewById(R.id.history_game_type);
                time = v.findViewById(R.id.history_time);
            }
        }
    }
}
