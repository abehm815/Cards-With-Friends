package data.Lobby;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.User.*;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for real-time communication within a lobby.
 * <p>
 * This endpoint allows users to join a lobby, send messages, start games,
 * and receive broadcast updates about other users joining or leaving.
 * <p>
 * WebSocket path: {@code /ws/lobby/{joinCode}/{username}}
 */
@Component
@ServerEndpoint(value = "/ws/lobby/{joinCode}/{username}")
public class LobbyWebSocket {

    private static LobbyRepository lobbyRepository;
    private static AppUserRepository userRepository;

    private static final Map<String, Set<Session>> lobbySessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Spring injection for static repositories.
     * <p>
     * Required because WebSocket endpoints are managed by the container,
     * not Spring, so static fields are used to access Spring beans.
     *
     * @param lobbyRepo the {@link LobbyRepository} bean
     * @param userRepo  the {@link AppUserRepository} bean
     */
    @Autowired
    public void setStaticRepositories(LobbyRepository lobbyRepo, AppUserRepository userRepo) {
        LobbyWebSocket.lobbyRepository = lobbyRepo;
        LobbyWebSocket.userRepository = userRepo;
    }

    /**
     * Called when a new WebSocket connection is opened.
     *
     * @param session  the WebSocket session
     * @param joinCode the lobby join code
     * @param username the username of the connecting user
     * @throws IOException if sending messages to sessions fails
     */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("joinCode") String joinCode,
                       @PathParam("username") String username) throws IOException {

        // Validate lobby existence
        Lobby lobby = lobbyRepository.findByJoinCode(joinCode);
        if (lobby == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Lobby not found."));
            return;
        }

        // Validate user existence
        AppUser user = userRepository.findByUsername(username);
        if (user == null) {
            session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "User not found."));
            return;
        }

        // Register the session
        Set<Session> sessionsForLobby = lobbySessions.get(joinCode);
        if (sessionsForLobby == null) {
            sessionsForLobby = ConcurrentHashMap.newKeySet();
            lobbySessions.put(joinCode, sessionsForLobby);
        }
        // Add the current session to the lobby's session set
        sessionsForLobby.add(session);


        // Broadcast join message to the lobby
        broadcastJson(joinCode, Map.of(
                "type", "JOIN",
                "username", username,
                "message", username + " joined the lobby."
        ));
    }
    /**
     * Handles messages sent by clients.
     * <p>
     * Supports "START" messages to start the game and "MESSAGE" messages
     * for chat functionality.
     *
     * @param session  the WebSocket session
     * @param joinCode the lobby join code
     * @param username the username sending the message
     * @param message  the JSON-formatted message
     * @throws IOException if broadcasting fails
     */
    @OnMessage
    public void onMessage(Session session,
                          @PathParam("joinCode") String joinCode,
                          @PathParam("username") String username,
                          String message) throws IOException {

        Map<String, Object> msg = mapper.readValue(message, Map.class);
        String type = (String) msg.get("type");

        if (type == null) {
            return;
        }

        switch (type.toUpperCase()) {
            case "START":
                // Broadcast start event
                broadcastJson(joinCode, Map.of(
                        "type", "START",
                        "username", username,
                        "message", username + " has started the game."
                ));
                System.out.println("Game started by " + username + " in lobby " + joinCode);
                break;

            case "MESSAGE":
                // Regular chat message
                broadcastJson(joinCode, Map.of(
                        "type", "MESSAGE",
                        "username", username,
                        "message", msg.get("message")
                ));
                break;

            default:
                System.out.println("Unknown message type: " + type);
                break;
        }
    }

    /**
     * Called when a WebSocket connection is closed.
     * Removes the session from the lobby and broadcasts a "LEAVE" message.
     *
     * @param session  the WebSocket session
     * @param joinCode the lobby join code
     * @param username the username leaving
     * @throws IOException if broadcasting fails
     */
    @OnClose
    public void onClose(Session session,
                        @PathParam("joinCode") String joinCode,
                        @PathParam("username") String username) throws IOException {
        Set<Session> sessions = lobbySessions.get(joinCode);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                lobbySessions.remove(joinCode);
            }
        }

        broadcastJson(joinCode, Map.of(
                "type", "LEAVE",
                "username", username,
                "message", username + " left the lobby."
        ));
    }

    /**
     * Called when an error occurs on the WebSocket connection.
     *
     * @param session   the WebSocket session
     * @param throwable the thrown error
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    /**
     * Broadcasts a JSON message to all users in a lobby.
     *
     * @param joinCode the lobby join code
     * @param message  the message as a map
     * @throws IOException if sending messages fails
     */
    private void broadcastJson(String joinCode, Map<String, Object> message) throws IOException {
        Set<Session> sessions = lobbySessions.get(joinCode);
        if (sessions == null) return;

        String jsonMessage = mapper.writeValueAsString(message);

        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(jsonMessage);
            }
        }
    }
}