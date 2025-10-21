package data.Lobby;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import data.User.AppUser;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lobbyID;

    @Enumerated(EnumType.STRING)
    private GameType gameType;

    @OneToMany(mappedBy = "lobby")
    @JsonManagedReference
    private List<AppUser> users;


    private String joinCode;


    public long getLobbyID() {
        return lobbyID;
    }

    public void setLobbyID(long lobbyID) {
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

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
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

