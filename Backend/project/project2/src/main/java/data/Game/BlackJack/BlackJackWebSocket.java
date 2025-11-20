package data.Game.BlackJack;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * WebSocket endpoint for real-time multiplayer Blackjack.
 * <p>
 * Each lobby (identified by a lobbyCode) maintains:
 * <ul>
 *     <li>A {@link BlackJackGame} instance</li>
 *     <li>A set of WebSocket sessions connected to that lobby</li>
 *     <li>A mapping of WebSocket sessions to player usernames</li>
 * </ul>
 *
 * This class is responsible for:
 * <ul>
 *     <li>Creating game instances when players join a lobby</li>
 *     <li>Routing player messages (BET/HIT/STAND/DOUBLE/SPLIT/etc.) to the game engine</li>
 *     <li>Broadcasting game updates to all connected players in the same lobby</li>
 * </ul>
 */
@Component
@ServerEndpoint("/ws/blackjack/{lobbyCode}")
public class BlackJackWebSocket {

    /** Map of lobbyCode → Blackjack game instance. */
    private static final Map<String, BlackJackGame> games = new ConcurrentHashMap<>();

    /** Map of lobbyCode → connected WebSocket sessions for that lobby. */
    private static final Map<String, Set<Session>> lobbySessions = new ConcurrentHashMap<>();

    /** Map of WebSocket session → username of the player using that session. */
    private static final Map<Session, String> sessionPlayers = new ConcurrentHashMap<>();

    /** JSON serializer for WebSocket messages. */
    private final ObjectMapper mapper = new ObjectMapper();

    // Spring-managed repositories (set via @Autowired setter methods)
    private static LobbyRepository lobbyRepository;
    private static AppUserRepository appUserRepository;
    private static UserStatsRepository userStatsRepository;

    @Autowired
    public void setLobbyRepository(LobbyRepository repo) {
        BlackJackWebSocket.lobbyRepository = repo;
    }

    @Autowired
    public void setAppUserRepository(AppUserRepository repo) {
        BlackJackWebSocket.appUserRepository = repo;
    }

    @Autowired
    public void setUserStatsRepository(UserStatsRepository repo) {
        BlackJackWebSocket.userStatsRepository = repo;
    }

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Triggered when a new WebSocket connection is opened.
     * Responsible for:
     * <ul>
     *     <li>Creating a Blackjack game for the lobby if it does not exist</li>
     *     <li>Adding the session to the lobby's session set</li>
     * </ul>
     *
     * @param session    the WebSocket session opened
     * @param lobbyCode  the lobby code extracted from the WebSocket URL
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("lobbyCode") String lobbyCode) {
        System.out.println("New connection: " + session.getId() + " in lobby " + lobbyCode);

        // Create or retrieve a BlackJackGame for this lobby

        games.computeIfAbsent(lobbyCode, code -> {
            BlackJackGame game = new BlackJackGame(code);
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
     * Handles incoming WebSocket messages from players.
     * Expected JSON format:
     * <pre>
     * {
     *   "action": "HIT" | "BET" | "STAND" | "DOUBLE" | ...,
     *   "player": "username",
     *   "value": optional integer (for bets)
     * }
     * </pre>
     *
     * Routes actions to {@link BlackJackGame}.
     *
     * @param session    the session sending the message
     * @param message    raw JSON message
     * @param lobbyCode  the lobby this session belongs to
     * @throws IOException if JSON parsing fails
     */
    @OnMessage
    public void onMessage(Session session, String message, @PathParam("lobbyCode") String lobbyCode) throws IOException {
        BlackJackGame game = games.get(lobbyCode);
        if (game == null) return;

        // Parse JSON message
        var json = mapper.readTree(message);
        String action = json.get("action").asText();
        String player = json.get("player").asText();
        int value = json.has("value") ? json.get("value").asInt() : 0;

        // Associate session with player
        sessionPlayers.put(session, player);

        switch (action.toUpperCase()) {
            case "JOIN":
                // Optionally, handle JOIN logic here
                break;
            case "START":
                game.initializeGameFromLobby(lobbyCode);
                break;
            case "BET":
                game.handlePlayerBet(player, value);
                break;
            case "HIT":
            case "STAND":
            case "DOUBLE":
            case "SPLIT":
            case "LEAVE":
                game.handlePlayerDecision(player, action);
                break;
            case "STARTROUND":
                game.startRound();
                break;
            default:
                game.handlePlayerDecision(player, action);
        }

        // Broadcast updated game state to only this lobby
        broadcastGameState(game, lobbyCode);
    }

    /**
     * Triggered when a client's WebSocket connection closes.
     * Cleans up session tracking and removes empty lobby entries.
     *
     * @param session    the session that was closed
     * @param lobbyCode  the lobby the session belonged to
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
     * Called when an exception occurs within WebSocket processing.
     *
     * @param session   the session that encountered the error
     * @param throwable the exception thrown
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

    private void broadcastGameState(BlackJackGame game, String lobbyCode) {
        String json;
        try {
            json = mapper.writeValueAsString(game.toDTO());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

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