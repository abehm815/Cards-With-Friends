package data.Game.Crazy8;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.Game.BlackJack.BlackJackGame;
import data.Game.BlackJack.BlackJackWebSocket;
import data.Lobby.LobbyRepository;
import data.User.AppUserRepository;
import data.User.Stats.UserStatsRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
        * WebSocket endpoint that manages Crazy 8s gameplay communication between clients.
 * <p>
 * Each lobby is identified by a unique {@code lobbyCode}. This class handles:
        * <ul>
 *     <li>Connecting and disconnecting clients</li>
        *     <li>Routing player actions to the appropriate {@link Crazy8Game}</li>
        *     <li>Broadcasting updated game state to all connected players</li>
        * </ul>
        *
        * This endpoint integrates with Spring-managed repositories for lobby and user data.
        */
@Component
@ServerEndpoint("/ws/Crazy8/{lobbyCode}")
public class Crazy8WebSocket {
    /**
     * Map of lobbyCode → Crazy8Game instance.
     * Ensures each lobby has exactly one active game model.
     */
    private static final Map<String, Crazy8Game> games = new ConcurrentHashMap<>();

    /**
     * Map of lobbyCode → set of active websocket sessions.
     * Used to broadcast events only to players in the same lobby.
     */
    private static final Map<String, Set<Session>> lobbySessions = new ConcurrentHashMap<>();

    /**
     * Map of websocket session → username.
     * Tracks which player is connected on which session.
     */
    private static final Map<Session, String> sessionPlayers = new ConcurrentHashMap<>();


    /**
     * JSON serializer for parsing client messages.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    // 🔹 Spring-managed repositories
    private static LobbyRepository lobbyRepository;
    private static AppUserRepository appUserRepository;
    private static UserStatsRepository userStatsRepository;

    /**
     * Injects the LobbyRepository.
     *
     * @param repo the repository instance
     */
    @Autowired
    public void setLobbyRepository(LobbyRepository repo) {
        Crazy8WebSocket.lobbyRepository = repo;
    }

    /**
     * Injects the AppUserRepository.
     *
     * @param repo the repository instance
     */
    @Autowired
    public void setAppUserRepository(AppUserRepository repo) {
        Crazy8WebSocket.appUserRepository = repo;
    }

    /**
     * Injects the UserStatsRepository.
     *
     * @param repo the repository instance
     */
    @Autowired
    public void setUserStatsRepository(UserStatsRepository repo) {
        Crazy8WebSocket.userStatsRepository = repo;
    }


    @Autowired
    private ApplicationContext applicationContext;


    /**
     * Triggered when a new WebSocket connection opens.
     * <p>
     * Creates a new Crazy 8 game for the lobby if one does not already exist,
     * and registers the player session for broadcasting.
     *
     * @param session   the client WebSocket session
     * @param lobbyCode the lobby the client is joining
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("lobbyCode") String lobbyCode) {
        System.out.println("New connection: " + session.getId() + " in lobby " + lobbyCode);

        // Create or retrieve a Crazy8Game for this lobby

        games.computeIfAbsent(lobbyCode, code -> {
            Crazy8Game game = new Crazy8Game(code);
            game.setLobbyRepository(lobbyRepository);
            game.setAppUserRepository(appUserRepository);
            game.setUserStatsRepository(userStatsRepository);
            game.setBroadcastFunction(json -> broadcastToLobby(json, code));
            return game;
        });
        // Add this session to the lobby
        lobbySessions.computeIfAbsent(lobbyCode, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * Triggered when a message is received from a client.
     * <p>
     * Parses the JSON payload and forwards the action to the Crazy8Game instance
     * assigned to the current lobby.
     *
     * @param session   the sending client session
     * @param message   the raw JSON payload
     * @param lobbyCode the lobby the message belongs to
     * @throws IOException if parsing the JSON fails
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("lobbyCode") String lobbyCode) throws IOException {
        Crazy8Game game = games.get(lobbyCode);
        if (game == null) return;

        // Parse JSON message
        var json = mapper.readTree(message);
        String action = json.get("action").asText();
        String player = json.get("player").asText();
        String cardcolor = json.get("cardcolor").asText();
        int value = json.has("cardvalue") ? json.get("value").asInt() : 0;

        // Associate session with player
        sessionPlayers.put(session, player);

        switch (action.toUpperCase()) {
            case "START":
                game.initializeGameFromLobby(lobbyCode);
                break;
            case "LEAVE":
                game.handlePlayerDecision(player, action, cardcolor.charAt(0),value);
                break;
            case "STARTROUND":
                game.startRound();
                break;
            default:
                game.handlePlayerDecision(player, action, cardcolor.charAt(0),value);
        }

        // Broadcast updated game state to only this lobby
        //broadcastGameState(game, lobbyCode);
    }

    /**
     * Triggered when a WebSocket connection closes.
     * Removes the session from the lobby and cleans up empty lobbies.
     *
     * @param session   the disconnected session
     * @param lobbyCode the lobby the session belonged to
     */
    @OnClose
    public void onClose(Session session, @PathParam("lobbyCode") String lobbyCode) {
        System.out.println("Connection closed: " + session.getId());
        sessionPlayers.remove(session);

        Set<Session> sessions = lobbySessions.get(lobbyCode);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                lobbySessions.remove(lobbyCode);
            }
        }
    }

    /**
     * Triggered when a WebSocket error occurs.
     *
     * @param session   the session where the error occurred
     * @param throwable the exception that occurred
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void broadcastToLobby(String json, String lobbyCode) {
        Set<Session> sessions = lobbySessions.getOrDefault(lobbyCode, Collections.emptySet());
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
