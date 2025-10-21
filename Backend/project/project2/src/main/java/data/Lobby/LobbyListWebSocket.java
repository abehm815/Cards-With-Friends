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

@Component
@ServerEndpoint("/ws/lobbies")
public class LobbyListWebSocket {

    private static LobbyService lobbyService; // New service

    private static final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public void setLobbyService(LobbyService service) {
        LobbyListWebSocket.lobbyService = service;
    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        sendLobbyList(session);
    }

    private void sendLobbyList(Session session) throws IOException {
        List<LobbyDTO> lobbies = lobbyService.getAllLobbyDTOs();
        String json = mapper.writeValueAsString(Map.of(
                "type", "LOBBY_LIST",
                "lobbies", lobbies
        ));
        session.getBasicRemote().sendText(json);
    }

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

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }
}