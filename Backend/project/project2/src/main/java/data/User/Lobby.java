package data.User;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int lobbyID;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    public void setId(int lobbyID) {
        this.lobbyID = lobbyID;
    }

    public int getId() {
        return lobbyID;
    }

    public List<AppUser> users;

    public int getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(int lobbyID) {
        this.lobbyID = lobbyID;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public List<AppUser> getUsers() {
        return users;
    }

    public void setUsers(List<AppUser> users) {
        this.users = users;
    }

    public AppUser getUser(int userID) {
        for (AppUser user : users) {
            if (user.getUserID() == userID) {
                return user;
            }
        }
        return null;
    }
}

