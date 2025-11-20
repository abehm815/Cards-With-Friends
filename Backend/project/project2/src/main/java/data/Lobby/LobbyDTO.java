package data.Lobby;

import data.User.AppUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object (DTO) for the {@link Lobby} entity.
 * <p>
 * This class is used to transfer simplified lobby information
 * between layers or over the network, avoiding exposing
 * full entity relationships.
 * <p>
 * Contains the lobby ID, join code, game type, and a list of
 * usernames of users in the lobby.
 */
public class LobbyDTO {
    private Long id;
    private String joinCode;
    private GameType gameType;
    private List<String> usernames;

    /**
     * Constructs a {@link LobbyDTO} from a given {@link Lobby} entity.
     *
     * @param lobby the {@link Lobby} entity to convert
     */
    public LobbyDTO(Lobby lobby) {
        this.id = lobby.getLobbyID();
        this.joinCode = lobby.getJoinCode();
        this.gameType = lobby.getGameType();
        this.usernames = new ArrayList<>();
        if (lobby.getUsers() != null) {
            for (AppUser user : lobby.getUsers()) {
                usernames.add(user.getUsername());

            }
        }
        // Getters and setters (or use Lombok @Data if preferred)
    }

    /**
     * Gets the unique ID of the lobby.
     *
     * @return the lobby ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the join code of the lobby.
     *
     * @return the lobby join code
     */
    public String getJoinCode() {
        return joinCode;
    }

    /**
     * Gets the type of game associated with the lobby.
     *
     * @return the {@link GameType} of the lobby
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * Gets a list of usernames of users in the lobby.
     *
     * @return a {@link List} of usernames
     */
    public List<String> getUsernames() {
        return usernames;
    }
}


