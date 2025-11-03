package data.Game.BlackJack;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.Lobby.LobbyRepository;
import data.User.AppUserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
    /**
     * WebSocket endpoint for Blackjack game.
     * Example URI: ws://localhost:8080/ws/blackjack/{lobbyCode}
     */
    @Component
    @ServerEndpoint("/ws/blackjack/{lobbyCode}")
    public class BlackJackWebSocket {

        // Map of lobbyCode -> BlackJackGame instance
        private static final Map<String, BlackJackGame> games = new ConcurrentHashMap<>();

        // Map of session -> player username
        private static final Map<Session, String> sessionPlayers = new ConcurrentHashMap<>();

        private final ObjectMapper mapper = new ObjectMapper();

        // 🔹 Inject Spring-managed repositories
        private static LobbyRepository lobbyRepository;
        private static AppUserRepository appUserRepository;

        @Autowired
        public void setLobbyRepository(LobbyRepository repo) {
            BlackJackWebSocket.lobbyRepository = repo;
        }

        @Autowired
        public void setAppUserRepository(AppUserRepository repo) {
            BlackJackWebSocket.appUserRepository = repo;
        }


        @OnOpen
        public void onOpen(Session session, @PathParam("lobbyCode") String lobbyCode) {
            System.out.println("New connection: " + session.getId() + " in lobby " + lobbyCode);

            //Create or retrieve a BlackJackGame and inject dependencies
            games.computeIfAbsent(lobbyCode, code -> {
                BlackJackGame game = new BlackJackGame(code);
                game.setLobbyRepository(lobbyRepository);
                game.setAppUserRepository(appUserRepository);
                game.setBroadcastFunction(json -> broadcastToLobby(json, code));
                return game;
            });
        }


        @OnMessage
        public void onMessage(Session session, String message, @PathParam("lobbyCode") String lobbyCode) throws IOException {
            BlackJackGame game = games.get(lobbyCode);
            if (game == null) return;

            // Expecting JSON: { "action":"JOIN", "player":"Alice", "value":100 }
            var json = mapper.readTree(message);
            String action = json.get("action").asText();
            String player = json.get("player").asText();
            int value = json.has("value") ? json.get("value").asInt() : 0;

            sessionPlayers.put(session, player);

            switch (action.toUpperCase()) {
                case "JOIN":
                    break;

                case "START":
                    game.initializeGameFromLobby(lobbyCode);
                    break;

                case "BET":
                    game.handlePlayerBet(player, value);
                    break;

                case "HIT":
                    game.handlePlayerDecision(player,action);
                    break;

                case "STAND":
                    game.handlePlayerDecision(player, action);
                    break;

                case "DOUBLE":
                    game.handlePlayerDecision(player, action);
                    break;

                case "STARTROUND":
                    game.startRound();
                    break;

                case"SPLIT":
                    game.handlePlayerDecision(player,action);
                    break;

                default:
                    game.handlePlayerDecision(player,action);
            }

            // Broadcast updated game state to all sessions in this lobby
            broadcastGameState(game, lobbyCode);
        }

        @OnClose
        public void onClose(Session session, @PathParam("lobbyCode") String lobbyCode) {
            System.out.println("Connection closed: " + session.getId());
            sessionPlayers.remove(session);
        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            throwable.printStackTrace();
        }



        private void broadcastToLobby(String json, String lobbyCode) {
            sessionPlayers.keySet().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }



        private void broadcastGameState(BlackJackGame game, String lobbyCode) {
            String json;
            try {
                json = mapper.writeValueAsString(game.toDTO());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Send to all sessions
            sessionPlayers.keySet().forEach(session -> {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
