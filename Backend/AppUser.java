package Backend;

import java.util.List;

public class AppUser {
    public String username;
    public String password;
    public String email;
    public String firstName;
    public String lastName;
    public int age;
    public int userID;
    public List<MyCard> hand;
    public boolean inLobby = false;
    public int tricksWon = 0;
    public int gamesWon =0;
    public int gamesLost = 0;

    //constructor for a AppUser
    public void AppUser(String username, String password, String email, String firstName, String lastName, int age, int userID) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.userID = userID;
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

    public int getTricksWon() {
        return this.tricksWon;
    }

    public void setTricksWon(int tricksWon) {
        this.tricksWon = tricksWon;
    }

    public int getGamesWon() {
        return this.gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return this.gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public List<MyCard> getHand() {
        return this.hand;
    }

    @Override
    public String toString() {
        return username + " " + tricksWon + " " + gamesWon + " " + gamesLost + " win loss ratio " + String.format("%.2f", (double) gamesWon / gamesLost);
    }

}


