package data.Game.Crazy8;

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

@Component
@ServerEndpoint("/ws/Crazy8/{lobbyCode}")
public class Crazy8WebSocket {
    // Map of lobbyCode -> Crazy8Game instance
    private static final Map<String, Crazy8Game> games = new ConcurrentHashMap<>();

    // Map of lobbyCode -> set of sessions
    private static final Map<String, Set<Session>> lobbySessions = new ConcurrentHashMap<>();

    // Map of session -> player username
    private static final Map<Session, String> sessionPlayers = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    // Spring-managed repositories
    private static LobbyRepository lobbyRepository;
    private static AppUserRepository appUserRepository;
    private static UserStatsRepository userStatsRepository;

    @Autowired
    public void setLobbyRepository(LobbyRepository repo) {
        Crazy8WebSocket.lobbyRepository = repo;
    }

    @Autowired
    public void setAppUserRepository(AppUserRepository repo) {
        Crazy8WebSocket.appUserRepository = repo;
    }

    @Autowired
    public void setUserStatsRepository(UserStatsRepository repo) {
        Crazy8WebSocket.userStatsRepository = repo;
    }

    @Autowired
    private ApplicationContext applicationContext;

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

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("lobbyCode") String lobbyCode) throws IOException {
        Crazy8Game game = games.get(lobbyCode);
        if (game == null) {
            sendError(session, "Game not found");
            return;
        }

        try {
            // Parse JSON message
            var json = mapper.readTree(message);
            String action = json.has("action") ? json.get("action").asText() : "";
            String player = json.has("player") ? json.get("player").asText() : "";

            // Get card details if present
            char cardcolor = ' ';
            int value = 0;

            if (json.has("cardcolor") && json.get("cardcolor").asText().length() > 0) {
                cardcolor = json.get("cardcolor").asText().charAt(0);
            }

            if (json.has("cardvalue")) {
                value = json.get("cardvalue").asInt();
            }

            // Associate session with player
            if (!player.isEmpty()) {
                sessionPlayers.put(session, player);
            }

            // Handle different actions
            switch (action.toUpperCase()) {
                case "START":
                    game.initializeGameFromLobby(lobbyCode);
                    sendConfirmation(session, "Game initialized");
                    break;

                case "STARTROUND":
                    game.startRound();
                    sendConfirmation(session, "Round started");
                    break;

                case "PLAYCARD":
                    if (cardcolor == ' ' || value == 0) {
                        sendError(session, "Invalid card data");
                        break;
                    }
                    game.handlePlayerDecision(player, action, cardcolor, value);
                    break;

                case "DRAW":
                    game.handlePlayerDecision(player, action, cardcolor, value);
                    break;

                case "CHOOSECOLOR":
                    // Handle color selection for wild cards
                    if (cardcolor == ' ') {
                        sendError(session, "Must specify a color (R, G, B, or Y)");
                        break;
                    }
                    game.handlePlayerDecision(player, action, cardcolor, value);
                    break;

                case "LEAVE":
                    game.handlePlayerDecision(player, action, cardcolor, value);
                    break;

                case "STATUS":
                    // Request current game state
                    // Game will broadcast automatically
                    break;

                default:
                    sendError(session, "Unknown action: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("lobbyCode") String lobbyCode) {
        System.out.println("Connection closed: " + session.getId());

        String username = sessionPlayers.remove(session);

        Set<Session> sessions = lobbySessions.get(lobbyCode);
        if (sessions != null) {
            sessions.remove(session);

            // If lobby is empty, clean up the game
            if (sessions.isEmpty()) {
                lobbySessions.remove(lobbyCode);
                games.remove(lobbyCode);
                System.out.println("Lobby " + lobbyCode + " cleaned up - no active sessions");
            } else if (username != null) {
                // Player disconnected, handle in game
                Crazy8Game game = games.get(lobbyCode);
                if (game != null && game.isRoundInProgress()) {
                    game.handlePlayerDecision(username, "LEAVE", ' ', 0);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error for session " + session.getId());
        throwable.printStackTrace();
    }

    /**
     * Broadcast message to all sessions in a specific lobby
     */
    private void broadcastToLobby(String json, String lobbyCode) {
        Set<Session> sessions = lobbySessions.getOrDefault(lobbyCode, Collections.emptySet());
        for (Session session : sessions) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(json);
                } catch (IOException e) {
                    System.err.println("Error broadcasting to session " + session.getId());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Send error message to a specific session
     */
    private void sendError(Session session, String error) {
        if (session.isOpen()) {
            try {
                var errorJson = mapper.createObjectNode();
                errorJson.put("type", "error");
                errorJson.put("message", error);
                session.getBasicRemote().sendText(mapper.writeValueAsString(errorJson));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send confirmation message to a specific session
     */
    private void sendConfirmation(Session session, String message) {
        if (session.isOpen()) {
            try {
                var confirmJson = mapper.createObjectNode();
                confirmJson.put("type", "confirmation");
                confirmJson.put("message", message);
                session.getBasicRemote().sendText(mapper.writeValueAsString(confirmJson));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}