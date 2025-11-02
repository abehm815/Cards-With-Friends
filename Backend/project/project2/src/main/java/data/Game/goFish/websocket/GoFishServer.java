package data.Game.goFish.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.Game.goFish.GoFishGame;
import data.Game.goFish.GoFishService;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint("/gofish")
public class GoFishServer {
    // Set up service layer
    private static GoFishService goFishService;

    @Autowired
    public void setGoFishService(GoFishService service) {
        GoFishServer.goFishService = service;
    }

    public static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        sendMessage(session, "Connected to Go Fish server");
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            Map<String, Object> msg = mapper.readValue(message, Map.class);
            String type = (String) msg.get("type");

            // Depending on type handle each situation
            switch (type) {
                case "start" -> handleStart(msg);
                case "turn" -> handleTurn(msg);
                case "state" -> sendFullGameState(session);
                default -> sendMessage(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            sendMessage(session, "Error: " + e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sendMessage(session, "Error: " + throwable.getMessage());
    }

    /**
     * Takes all of the players that are given in the message and starts a game with them
     * @param msg (Contains player names)
     */
    private void handleStart(Map<String, Object> msg) {
        List<String> usernames = (List<String>) msg.get("players");
        GoFishGame game = goFishService.startGame(usernames);
        // Send message that game has started
        broadcast("New game started with " + game.getPlayers().size() + " players.");
        broadcastGameState();
    }

    /**
     * Takes the info given in the msg and uses it to do one turn of actions
     * @param msg (askingPlayer, targetPlayer, value)
     */
    private void handleTurn(Map<String, Object> msg) {
        // Get all data from msg
        String asking = (String) msg.get("askingPlayer");
        String target = (String) msg.get("targetPlayer");
        int value = (int) msg.get("value");

        // Call takeTurn method and broadcast the result
        String result = goFishService.processTurn(asking, target, value);
        broadcast(result);
        broadcastGameState();
    }

    /**
     * Send the game object of the session requested
     * @param session (session which state is required)
     */
    private void sendFullGameState(Session session) {
        GoFishGame game = goFishService.getCurrentGame();
        if (game != null) {
            sendJson(session, game);
        } else {
            sendMessage(session, "No game running.");
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
    private void broadcast(String message) {
        for (Session s : sessions) {
            sendMessage(s, message);
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
    private void broadcastGameState() {
        GoFishGame game = goFishService.getCurrentGame();
        if (game != null) {
            String json;
            try {
                json = mapper.writeValueAsString(game);
                for (Session s : sessions) {
                    s.getBasicRemote().sendText(json);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
