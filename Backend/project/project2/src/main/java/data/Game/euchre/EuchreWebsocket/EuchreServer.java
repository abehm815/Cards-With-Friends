package data.Game.euchre.EuchreWebsocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.Game.euchre.EuchreCard;
import data.Game.euchre.EuchreGame;
import data.Game.euchre.EuchreService;
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

//Test
@Component
@ServerEndpoint("/euchre/{lobbyCode}")
public class EuchreServer {
        private static EuchreService euchreService;
        private static final ObjectMapper mapper = new ObjectMapper();
        private static final Map<String, Set<Session>> lobbySessions = new ConcurrentHashMap<>();

        @Autowired
        public void setEuchreService(EuchreService service) {
            EuchreServer.euchreService = service;
        }

        @OnOpen
        public void onOpen(Session session, @PathParam("lobbyCode") String lobbyCode) {
            euchreService.addPlayerToLobby(lobbyCode, session);
            lobbySessions.computeIfAbsent(lobbyCode, k -> ConcurrentHashMap.newKeySet()).add(session);
            sendMessage(session, "Connected to Euchre lobby: " + lobbyCode);
        }

        @OnMessage
        public void onMessage(Session session, String message, @PathParam("lobbyCode") String lobbyCode) {
            try {
                Map<String, Object> msg = mapper.readValue(message, Map.class);
                String type = (String) msg.get("type");

                switch (type) {
                    case "start" -> handleStart(msg, lobbyCode);
                    case "pass" -> handlePass(msg, lobbyCode);
                    case "chooseSuit" -> handleChooseSuit(msg, lobbyCode);
                    case "pickUp" -> handlePickUp(msg, lobbyCode);
                    case "play" -> handlePlay(msg, lobbyCode);
                    case "state" -> sendFullGameState(session, lobbyCode);
                    case "end" -> handleEnd(msg, lobbyCode);
                    default -> sendMessage(session, "Unknown message type: " + type);
                }
            } catch (Exception e) {
                sendMessage(session, "Error processing message: " + e.getMessage());
            }
        }

        @OnClose
        public void onClose(Session session, @PathParam("lobbyCode") String lobbyCode) {
            Set<Session> sessions = lobbySessions.get(lobbyCode);
            if (sessions != null) sessions.remove(session);
            euchreService.removePlayerFromLobby(lobbyCode, session);
        }

        @OnError
        public void onError(Session session, Throwable throwable) {
            sendMessage(session, "WebSocket error: " + throwable.getMessage());
        }

        private void handleStart(Map<String, Object> msg, String lobbyCode) {
            List<String> usernames = (List<String>) msg.get("players");
            EuchreGame game = euchreService.startGame(lobbyCode, usernames);
            broadcast(lobbyCode, "New Euchre round started. Dealer: " + game.getCurrentDealerUsername());
            broadcastGameState(lobbyCode);
        }

        private void handlePass(Map<String, Object> msg, String lobbyCode) {
            // msg should include "player" (username) but playerPasses in game uses index progression
            String player = (String) msg.get("player");
            String result = euchreService.processPass(lobbyCode, player);
            broadcast(lobbyCode, result);
            broadcastGameState(lobbyCode);
        }

        private void handleChooseSuit(Map<String, Object> msg, String lobbyCode) {
            String player = (String) msg.get("player");
            String suitStr = (String) msg.get("suit");
            char suit = suitStr.charAt(0);
            String result = euchreService.processChooseSuit(lobbyCode, player, suit);
            broadcast(lobbyCode, result);
            broadcastGameState(lobbyCode);
        }

        private void handlePickUp(Map<String, Object> msg, String lobbyCode) {
            String dealer = (String) msg.get("dealer");
            Map<String, Object> dropped = (Map<String, Object>) msg.get("droppedCard");
            int value = (Integer) dropped.get("value");
            char suit = ((String) dropped.get("suit")).charAt(0);
            String result = euchreService.processPickUp(lobbyCode, dealer, value, suit);
            broadcast(lobbyCode, result);
            broadcastGameState(lobbyCode);
        }

        private void handlePlay(Map<String, Object> msg, String lobbyCode) {
            String player = (String) msg.get("player");
            Map<String, Object> cardMap = (Map<String, Object>) msg.get("card");
            int value = (Integer) cardMap.get("value");
            char suit = ((String) cardMap.get("suit")).charAt(0);

            String result = euchreService.processPlay(lobbyCode, player, value, suit);

            broadcast(lobbyCode, result);
            broadcastGameState(lobbyCode);
        }

        private void handleEnd(Map<String, Object> msg, String lobbyCode) {
            euchreService.endGame(lobbyCode);
            broadcast(lobbyCode, "Euchre game ended.");
        }

        private void sendFullGameState(Session session, String lobbyCode) {
            EuchreGame game = euchreService.getGame(lobbyCode);
            if (game != null) {
                sendJson(session, Map.of("type", "gameState", "game", game, "currentTurn", game.getCurrentPlayerUsername()));
            } else {
                sendMessage(session, "No game running for lobby: " + lobbyCode);
            }
        }

        private void sendMessage(Session session, String msg) {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String lobbyCode, String msg) {
            for (Session s : lobbySessions.getOrDefault(lobbyCode, Set.of())) {
                sendMessage(s, msg);
            }
        }

        private void sendJson(Session session, Object obj) {
            try {
                session.getBasicRemote().sendText(mapper.writeValueAsString(obj));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcastGameState(String lobbyCode) {
            EuchreGame game = euchreService.getGame(lobbyCode);
            if (game == null) return;

            try {
                Map<String, Object> state = Map.of(
                        "type", "gameState",
                        "currentTurn", game.getCurrentPlayerUsername(),
                        "trump", game.getWinner() == null ? null : game // keep simple; clients can inspect game object
                );
                String json = mapper.writeValueAsString(Map.of("type", "gameState", "game", game, "currentTurn", game.getCurrentPlayerUsername()));
                for (Session s : lobbySessions.getOrDefault(lobbyCode, Set.of())) {
                    s.getBasicRemote().sendText(json);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
