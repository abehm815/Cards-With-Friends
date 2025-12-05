package data.Lobby;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket endpoint for broadcasting the list of all lobbies to connected clients.
 * <p>
 * This endpoint listens on <code>/ws/lobbies</code>. Whenever a client connects,
 * they immediately receive the current list of lobbies. The list is also
 * broadcast to all connected clients whenever the {@link #broadcastLobbyList()} method is called.
 */
@Component
@ServerEndpoint("/ws/lobbies")
public class LobbyListWebSocket {

    /** Service to retrieve lobby data */
    private static LobbyService lobbyService; // New service

    /** Thread-safe set of all currently connected WebSocket sessions */
    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    /** JSON object mapper */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Spring-based setter for injecting the {@link LobbyService}.
     *
     * @param service the lobby service
     */
    @Autowired
    public void setLobbyService(LobbyService service) {
        LobbyListWebSocket.lobbyService = service;
    }

    /**
     * Called when a new WebSocket connection is opened.
     * Adds the session to the set of active sessions and sends the current lobby list.
     *
     * @param session the new WebSocket session
     * @throws IOException if sending the lobby list fails
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        sendLobbyList(session);
    }

    /**
     * Sends the current list of lobbies to a specific session.
     *
     * @param session the WebSocket session to send to
     * @throws IOException if sending the message fails
     */
    private void sendLobbyList(Session session) throws IOException {
        List<LobbyDTO> lobbies = lobbyService.getAllLobbyDTOs();
        String json = mapper.writeValueAsString(Map.of(
                "type", "LOBBY_LIST",
                "lobbies", lobbies
        ));
        session.getBasicRemote().sendText(json);
    }

    /**
     * Broadcasts the current list of lobbies to all connected sessions.
     */
    public static void broadcastLobbyList() {
        try {
            List<LobbyDTO> lobbies = lobbyService.getAllLobbyDTOs();
            String json = mapper.writeValueAsString(Map.of(
                    "type", "LOBBY_LIST",
                    "lobbies", lobbies
            ));
            for (Session session : sessions) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(json);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * Removes the session from the set of active sessions.
     *
     * @param session the WebSocket session that was closed
     */
    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    /**
     * Called when a WebSocket error occurs.
     *
     * @param session   the WebSocket session in which the error occurred
     * @param throwable the error that occurred
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }
}