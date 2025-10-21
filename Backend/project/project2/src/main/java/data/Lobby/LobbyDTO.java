package data.Lobby;

import data.User.AppUser;

import java.util.ArrayList;
import java.util.List;

public class LobbyDTO {
    private Long id;
    private String joinCode;
    private GameType gameType;
    private List<String> usernames;

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
    public Long getId() {
        return id;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public GameType getGameType() {
        return gameType;
    }

    public List<String> getUsernames() {
        return usernames;
    }
}


