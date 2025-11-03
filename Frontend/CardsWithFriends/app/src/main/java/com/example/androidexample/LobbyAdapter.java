package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LobbyAdapter extends RecyclerView.Adapter<LobbyAdapter.LobbyViewHolder> {

    private List<Lobby> lobbies;

    public LobbyAdapter(List<Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    @NonNull
    @Override
    public LobbyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lobby, parent, false);
        return new LobbyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LobbyViewHolder holder, int position) {
        Lobby lobby = lobbies.get(position);
        holder.name.setText(lobby.getName());
        holder.players.setText(lobby.getPlayerCount() + " players");
    }

    @Override
    public int getItemCount() {
        return lobbies.size();
    }

    public static class LobbyViewHolder extends RecyclerView.ViewHolder {
        TextView name, players;

        public LobbyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.lobby_name);
            players = itemView.findViewById(R.id.lobby_players);
        }
    }
}
