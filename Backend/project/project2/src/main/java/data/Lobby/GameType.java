package data.Lobby;

/**
 * Represents the different types of games supported by the lobby system.
 * <p>
 * This enum is typically stored in a lobby object to determine
 * which game logic, WebSocket endpoint, and ruleset should be used.
 */
public enum GameType {
    GO_FISH,
    BLACKJACK,
    EUCHRE
}
