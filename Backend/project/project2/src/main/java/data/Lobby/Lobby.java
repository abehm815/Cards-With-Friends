package data.Lobby;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import data.User.AppUser;
import jakarta.persistence.*;

import java.util.List;

/**
 * Represents a game lobby where users can join and play a specific game type.
 * <p>
 * Each lobby has a unique ID, a game type, a list of users, and a join code
 * that allows other users to enter the lobby.
 */
@Entity
public class Lobby {

    /**
     * The unique identifier for the lobby.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long lobbyID;

    /**
     * The type of game being played in this lobby.
     */
    @Enumerated(EnumType.STRING)
    private GameType gameType;

    /**
     * The list of users currently in the lobby.
     */
    @OneToMany(mappedBy = "lobby")
    @JsonManagedReference
    private List<AppUser> users;


    /**
     * The join code used by players to enter this lobby.
     */
    private String joinCode;


    /**
     * Gets the unique lobby ID.
     * @return the lobby ID
     */
    public long getLobbyID() {
        return lobbyID;
    }

    /**
     * Sets the unique lobby ID.
     * @param lobbyID the lobby ID to set
     */
    public void setLobbyID(long lobbyID) {
        this.lobbyID = lobbyID;
    }

    /**
     * Gets the game type for this lobby.
     * @return the game type
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * Sets the game type for this lobby.
     * @param gameType the game type to set
     */
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    /**
     * Gets the list of users currently in the lobby.
     * @return the list of users
     */
    public List<AppUser> getUsers() {
        return users;
    }

    /**
     * Sets the list of users in the lobby.
     * @param users the list of users to set
     */
    public void setUsers(List<AppUser> users) {
        this.users = users;
    }

    /**
     * Gets the join code for this lobby.
     * @return the join code
     */
    public String getJoinCode() {
        return joinCode;
    }

    /**
     * Sets the join code for this lobby.
     * @param joinCode the join code to set
     */
    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    /**
     * Retrieves a user from the lobby by their user ID.
     * @param userID the ID of the user to retrieve
     * @return the AppUser with the matching ID, or null if not found
     */
    public AppUser getUser(int userID) {
        for (AppUser user : users) {
            if (user.getUserID() == userID) {
                return user;
            }
        }
        return null;
    }
}

