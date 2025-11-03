package com.example.androidexample;

public class Lobby {
    private String name;
    private int playerCount;

    public Lobby(String name, int playerCount) {
        this.name = name;
        this.playerCount = playerCount;
    }

    public String getName() {
        return name;
    }

    public int getPlayerCount() {
        return playerCount;
    }
}
