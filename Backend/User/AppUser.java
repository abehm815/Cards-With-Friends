package Backend.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

    @Id
    public int userID;
    public String username;
    public String password;
    public String email;
    public String firstName;
    public String lastName;
    public int age;
    public List<MyCard> hand;
    public boolean inLobby = false;

    @OneToOne
    private UserStats UserStats;

    //constructor for a AppUser
    public void AppUser(String username, String password, String email, String firstName, String lastName, int age, int userID) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }



    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return this.age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public int getUserID() {
        return this.userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public List<MyCard> getHand() {
        return this.hand;
    }

    @Override
    public String toString() {
        return username;
    }

    // This is a base class that has methods that all the other stat classes will use
    public abstract static class GameStats {
        public int gamesPlayed;

        public void addGamePlayed() {gamesPlayed++;}
        public int getGamesPlayed() {return gamesPlayed;}
    }

    public static class UserStats {
        private Map<String, GameStats> gameStats;

        public UserStats() {
            this.gameStats = new HashMap<>();
        }

        public void addGameStats (String gameName, GameStats stats) {
            gameStats.put(gameName, stats);
        }

        public GameStats getGameStats(String gameName) {
            return gameStats.get(gameName);
        }

        public Map<String, GameStats> getAllGameStats() {
            return gameStats;
        }
    }
}


