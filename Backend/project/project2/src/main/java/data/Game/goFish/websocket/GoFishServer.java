package data.Game.goFish.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.Game.goFish.GoFishGame;
import data.Game.goFish.GoFishService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/gofish/{lobbyCode}")
public class GoFishServer {
    // Set up service layer
    private static GoFishService goFishService;

    @Autowired
    public void setGoFishService(GoFishService service) {
        GoFishServer.goFishService = service;
    }

    private static final Map<String, Set<Session>> lobbySessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("lobbyCode") String lobbyCode) {
        goFishService.addPlayerToLobby(lobbyCode, session);
        lobbySessions.computeIfAbsent(lobbyCode, k -> ConcurrentHashMap.newKeySet()).add(session);
        sendMessage(session, "Connected to Go Fish lobby: " + lobbyCode);
    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("lobbyCode") String lobbyCode) {
        try {
            Map<String, Object> msg = mapper.readValue(message, Map.class);
            String type = (String) msg.get("type");

            // Depending on type handle each situation
            switch (type) {
                case "start" -> handleStart(msg, lobbyCode);
                case "turn" -> handleTurn(msg, lobbyCode);
                case "state" -> sendFullGameState(session, lobbyCode);
                case "end" -> handleEnd(msg, lobbyCode);
                default -> sendMessage(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            sendMessage(session, "Error: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("lobbyCode") String lobbyCode) {
        Set<Session> sessions = lobbySessions.get(lobbyCode);
        if (sessions != null) {
            sessions.remove(session);
        }
        goFishService.removePlayerFromLobby(lobbyCode, session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sendMessage(session, "Error: " + throwable.getMessage());
    }

    /**
     * Takes all of the players that are given in the message and starts a game with them
     * @param msg (Contains player names)
     */
    private void handleStart(Map<String, Object> msg, String lobbyCode) {
        List<String> usernames = (List<String>) msg.get("players");
        GoFishGame game = goFishService.startGame(lobbyCode, usernames);
        // Send message that game has started
        broadcast(lobbyCode, "New game started with " + game.getPlayers().size() + " players.");
        broadcastGameState(lobbyCode);
    }

    /**
     * Takes the info given in the msg and uses it to do one turn of actions
     * @param msg (askingPlayer, targetPlayer, value)
     */
    private void handleTurn(Map<String, Object> msg, String lobbyCode) {
        // Get all data from msg
        String asking = (String) msg.get("askingPlayer");
        String target = (String) msg.get("targetPlayer");
        int value = (int) msg.get("value");

        // Call takeTurn method and broadcast the result
        String result = goFishService.processTurn(lobbyCode, asking, target, value);
        broadcast(lobbyCode, result);

        // Check if the game has ended
        GoFishGame game = goFishService.getGame(lobbyCode);
        if (game != null && game.isGameOver()) {
            // Save all user stats and remove the game
            goFishService.endGame(lobbyCode);

            // Announce the winner
            broadcast(lobbyCode, "Game over! Winner: " + game.getWinner().getUsername());
            return;
        }

        broadcastGameState(lobbyCode);
    }

    private void handleEnd(Map<String, Object> msg, String lobbyCode) {
        // Save all user stats and remove the game
        goFishService.endGame(lobbyCode);
        broadcast(lobbyCode, "Game Ended!");
    }

    /**
     * Send the game object of the session requested
     * @param session (session which state is required)
     */
    private void sendFullGameState(Session session, String lobbyCode) {
        GoFishGame game = goFishService.getGame(lobbyCode);
        if (game != null) {
            sendJson(session, game);
        } else {
            sendMessage(session, "No game running for lobby code " + lobbyCode + "!");
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to all players in session
     * @param message (String to be displayed)
     */
    private void broadcast(String lobbyCode, String message) {
        Set<Session> sessions = lobbySessions.get(lobbyCode);
        if (sessions != null) {
            for (Session s : sessions) {
                sendMessage(s, message);
            }
        }
    }

    /**
     * Sends information as a json
     * @param session
     * @param obj
     */
    private void sendJson(Session session, Object obj) {
        try {
            session.getBasicRemote().sendText(mapper.writeValueAsString(obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the game state to everyone in the game
     */
    private void broadcastGameState(String lobbyCode) {
        GoFishGame game = goFishService.getGame(lobbyCode);
        if (game != null) {
            String json;
            try {
                json = mapper.writeValueAsString(game);
                for (Session s : lobbySessions.getOrDefault(lobbyCode, Set.of())) {
                    s.getBasicRemote().sendText(json);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
