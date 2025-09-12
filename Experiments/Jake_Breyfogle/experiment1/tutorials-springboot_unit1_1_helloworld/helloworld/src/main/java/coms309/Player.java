package coms309;

public class Player {
    // Stores the player's name
    public String name;

    // Stores the player's favorite game
    public String favoriteGame;

    // Stores the player's age
    public int age;

    // Basic constructor for a player
    public Player (String name, String favoriteGame, int age) {
        this.name = name;
        this.favoriteGame = favoriteGame;
        this.age = age;
    }

    // Returns the player's name
    public String getName() {
        return name;
    }

    // Sets the player's name
    public void setName(String name) {
        this.name = name;
    }

    // Returns the player's age
    public int getAge() {
        return age;
    }

    // Sets the player's age
    public void setAge(int age) {
        this.age = age;
    }

    // Returns the player's favorite game
    public String getFavoriteGame() {
        return favoriteGame;
    }

    // Sets the player's favorite game
    public void setFavoriteGame(String favoriteGame) {
        this.favoriteGame = favoriteGame;
    }
}
