package data.Game.Crazy8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import data.Game.Crazy8.history.Crazy8MatchHistoryRepository;
import data.Game.Crazy8.history.Crazy8MatchHistoryEntity;
import data.Game.Crazy8.history.Crazy8MatchEventEntity;
import data.Lobby.Lobby;
import data.Lobby.LobbyRepository;
import data.User.AppUser;
import data.User.AppUserRepository;
import data.User.Stats.Crazy8Stats;
import data.User.Stats.GameStats;
import data.User.Stats.UserStatsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Crazy8Game {
    @Autowired
    LobbyRepository LobbyRepository;
    @Autowired
    AppUserRepository AppUserRepository;
    @Autowired
    UserStatsRepository userStatsRepository;

    @Autowired
    private Crazy8MatchHistoryRepository Crazy8MatchHistoryRepository;

    private Crazy8MatchHistoryEntity matchHistory;

    public void setLobbyRepository(LobbyRepository repo) {
        this.LobbyRepository = repo;
    }

    public void setAppUserRepository(AppUserRepository repo) {
        this.AppUserRepository = repo;
    }

    public void setUserStatsRepository(UserStatsRepository repo) {
        this.userStatsRepository = repo;
    }

    public void setCrazy8MatchHistoryRepository (Crazy8MatchHistoryRepository repo){ this.Crazy8MatchHistoryRepository = repo; }

    private Consumer<String> broadcastFunction;
    private static final ObjectMapper mapper = new ObjectMapper();

    public void setBroadcastFunction(Consumer<String> broadcastFunction) {
        this.broadcastFunction = broadcastFunction;
    }

    private Crazy8Deck drawDeck;
    private List<Crazy8Card> playedCards;
    private Crazy8Card upCard;
    private List<Crazy8Player> players;
    private String lobbyCode;
    private int currentPlayerIndex = 0;
    private boolean roundInProgress = false;
    private boolean clockwise = true; // Direction of play
    private int drawStack = 0; // For stacking draw cards
    private char currentColor; // Current color in play (for wild cards)
    private boolean waitingForColorChoice = false; // Flag to indicate waiting for color
    private String playerChoosingColor = null; // Username of player choosing color

    public Crazy8Game(String lobbyCode) {
        this.lobbyCode = lobbyCode;
        this.players = new ArrayList<>();
        drawDeck = new Crazy8Deck(1);
        this.playedCards = new ArrayList<>();
        initMatchHistory(lobbyCode);
        matchHistory.setStartTime(LocalDateTime.now());
    }

    /**
     * Initializes a crazy 8 game by loading all users from the specified lobby
     * and creating player objects for them.
     */
    public void initializeGameFromLobby(String joinCode) {
        this.lobbyCode = joinCode;

        Lobby lobby = LobbyRepository.findByJoinCodeWithUsers(joinCode);

        if (lobby == null) {
            System.out.println("Lobby not found with join code: " + joinCode);
            return;
        }

        List<AppUser> users = lobby.getUsers();
        List<String> userNames = new ArrayList<>();
        for (AppUser user : users) {
            userNames.add(user.getUsername());
        }

        players.clear();

        for (String name : userNames) {
            AppUser user = AppUserRepository.findByUsernameWithStats(name);
            players.add(new Crazy8Player(user));
        }

        System.out.println("Initialized game with " + players.size() + " players.");
        broadcastGameState("Game initialized");
    }

    public void startRound() {
        drawDeck = new Crazy8Deck(1); // Fresh deck
        drawDeck.shuffle();
        playedCards.clear();
        currentPlayerIndex = 0;
        roundInProgress = true;
        clockwise = true;
        drawStack = 0;

        // Clear all player hands
        for (Crazy8Player player : players) {
            player.getHand().clear();
        }

        System.out.println("New round started, dealing cards");

        // Deal 7 cards to each player (standard Uno rules)
        for (Crazy8Player player : players) {
            for(int i = 0; i < 7; i++) {
                player.addCard(drawDeck.dealCard(false));
            }
        }

        // Draw initial up card (keep drawing if it's a special card)
        do {
            upCard = drawDeck.dealCard(false);
        } while (upCard.getValue() >= 11); // Avoid starting with special cards

        currentColor = upCard.getSuit();
        playedCards.add(upCard);

        updatePlayableCards();
        broadcastGameState("Round started - cards dealt");
    }

    public void handlePlayerDecision(String username, String decision, char cardcolor, int value) {
        if (!roundInProgress) {
            broadcastError(username, "No round in progress");
            return;
        }

        // Handle color choice separately
        if (decision.equalsIgnoreCase("CHOOSECOLOR")) {
            handleColorChoice(username, cardcolor);
            return;
        }

        // Don't allow other actions while waiting for color choice
        if (waitingForColorChoice) {
            broadcastError(username, "Waiting for " + playerChoosingColor + " to choose a color");
            return;
        }

        Crazy8Player currentPlayer = players.get(currentPlayerIndex);

        if (!currentPlayer.getUsername().equals(username)) {
            broadcastError(username, "Not your turn");
            return;
        }

        Crazy8Stats currentPlayerStats = currentPlayer.getCrazy8Stats();

        switch (decision.toUpperCase()) {
            case "PLAYCARD":
                playCard(currentPlayer, cardcolor, value);
                break;

            case "DRAW":
                drawCard(currentPlayer);
                break;

            case "LEAVE":
                playerLeave(username);
                break;

            default:
                System.out.println("Invalid decision: " + decision);
                broadcastError(username, "Invalid action");
        }

        // Broadcast game state after every action (unless waiting for color)
        if (!waitingForColorChoice) {
            broadcastGameState("Action completed");
        }
    }

    private void handleColorChoice(String username, char color) {
        if (!waitingForColorChoice) {
            broadcastError(username, "No color choice needed");
            return;
        }

        if (!username.equals(playerChoosingColor)) {
            broadcastError(username, "Not your turn to choose color");
            return;
        }

        // Validate color
        if (color != 'R' && color != 'G' && color != 'B' && color != 'Y') {
            broadcastError(username, "Invalid color. Must be R, G, B, or Y");
            return;
        }

        // Set the chosen color
        currentColor = color;
        upCard.setSuit(color); // changes upcard from original color to color that is chosen
        waitingForColorChoice = false;
        playerChoosingColor = null;

        System.out.println(username + " chose color: " + color);

        logEvent(username,"player chose color: " + getColorName(color), null, getColorName(color), null, players.get(currentPlayerIndex).getHandSize());

        // Continue with the game
        advanceTurn();
        updatePlayableCards();
        broadcastGameState("Color chosen: " + getSuitName(color));
    }

    private void playCard(Crazy8Player player, char color, int value) {
        // Find the card in player's hand
        Crazy8Card cardToPlay = null;
        for (Crazy8Card card : player.getHand()) {
            if (card.getSuit() == color && card.getValue() == value) {
                cardToPlay = card;
                break;
            }
        }

        if (cardToPlay == null) {
            broadcastError(player.getUsername(), "Card not found in hand");
            return;
        }

        // Validate if card can be played
        if (!isValidPlay(cardToPlay)) {
            broadcastError(player.getUsername(), "Invalid card play");
            return;
        }

        // Handle draw stack - player must draw if there's a draw stack unless playing draw card
        if (drawStack > 0 && cardToPlay.getValue() != 13 && cardToPlay.getValue() != 14) {
            broadcastError(player.getUsername(), "Must play draw card or draw " + drawStack + " cards");
            return;
        }

        // Remove card from hand and add to played pile
        player.removeCard(cardToPlay);
        playedCards.add(cardToPlay);
        upCard = cardToPlay;
        currentColor = upCard.getSuit(); // BUG FIX if value is the same on the cards and colors diffrent current color must be updated
        logEvent(player.getUsername(),"player placed a card", cardToPlay.toString(), null, null, player.getHandSize());
        // Update stats for cards played
        Crazy8Stats playerStats = player.getCrazy8Stats();
        if (playerStats != null) {
            playerStats.addCardPlaced(); // Track total cards placed

            // Track specific special cards
            int cardValue = cardToPlay.getValue();
            switch (cardValue) {
                case 8:  // Wild/Crazy 8
                    playerStats.addCrazy8Played();
                    break;
                case 11: // Reverse
                    playerStats.addReversePlayed();
                    break;
                case 12: // Skip
                    playerStats.addSkipPlayed();
                    break;
                case 13: // Draw 2
                    playerStats.addPlus2Played();
                    break;
                case 14: // Draw 4
                    playerStats.addPlus4Played();
                    break;
            }
        }

        // Handle special cards
        handleSpecialCard(cardToPlay, player);

        // Check for win (but only if not waiting for color choice)
        if (player.getHandSize() == 0) {
            endRound(player);
            return;
        }

        // Only advance turn and broadcast if NOT waiting for color choice
        if (!waitingForColorChoice) {
            advanceTurn();
            updatePlayableCards();
            broadcastGameState("Card played: " + cardToPlay.toString());
        } else {
            // Broadcast that card was played and waiting for color
            broadcastGameState("Card played: " + cardToPlay.toString() + " - waiting for color choice");
        }
    }

    private boolean isValidPlay(Crazy8Card card) {
        // Wild cards (8 and 14) can always be played
        if (card.getValue() == 8 || card.getValue() == 14) {
            return true;
        }

        // Check if matches current color or value
        return card.getSuit() == currentColor || card.getValue() == upCard.getValue();
    }

    private void handleSpecialCard(Crazy8Card card, Crazy8Player player) {
        int value = card.getValue();

        switch (value) {
            case 8: // Wild card - pick color
                waitingForColorChoice = true;
                playerChoosingColor = player.getUsername();
                broadcastColorChoiceRequest(player.getUsername());
                // Don't advance turn yet - wait for color choice
                break;

            case 11: // Reverse
                clockwise = !clockwise;
                if (players.size() == 2) {
                    // In 2 player game, reverse acts as skip
                    advanceTurn();
                }
                break;

            case 12: // Skip
                advanceTurn(); // Skip next player
                break;

            case 13: // Draw 2
                drawStack += 2;
                break;

            case 14: // Draw 4 and pick color
                drawStack += 4;
                waitingForColorChoice = true;
                playerChoosingColor = player.getUsername();
                broadcastColorChoiceRequest(player.getUsername());
                // Don't advance turn yet - wait for color choice
                break;
        }
    }

    private void broadcastColorChoiceRequest(String username) {
        if (broadcastFunction == null) return;

        try {
            ObjectNode json = mapper.createObjectNode();
            json.put("type", "colorChoiceRequest");
            json.put("username", username);
            json.put("message", username + " must choose a color (R, G, B, or Y)");

            broadcastFunction.accept(mapper.writeValueAsString(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSuitName(char color) {
        switch (color) {
            case 'R': return "Red";
            case 'G': return "Green";
            case 'B': return "Blue";
            case 'Y': return "Yellow";
            default: return "Unknown";
        }
    }

    private void drawCard(Crazy8Player player) {
        // Manual draw - player chooses to draw even though they might have playable cards
        // This is optional strategy (bluffing or trying to get a better card)

        Crazy8Stats playerStats = player.getCrazy8Stats();

        if (drawStack > 0) {
            // Player is drawing from a draw stack (forced)
            for (int i = 0; i < drawStack; i++) {
                if (drawDeck.size() > 0) {
                    Crazy8Card tempcard = drawDeck.dealCard(false);
                    player.addCard(tempcard);
                    logEvent(player.getUsername(),"player drew a card", tempcard.toString() , null, i+1 +" of "+ drawStack , player.getHandSize());
                    if (playerStats != null) {
                        playerStats.addCardDrawn();
                    }
                } else {
                    reshuffleDeck();
                    if (drawDeck.size() > 0) {
                        Crazy8Card tempcard = drawDeck.dealCard(false);
                        player.addCard(tempcard);
                        logEvent(player.getUsername(),"player drew a card", tempcard.toString() , null, i +" of "+ drawStack , player.getHandSize());
                        if (playerStats != null) {
                            playerStats.addCardDrawn();
                        }
                    }
                }
            }
            drawStack = 0;
            advanceTurn();
            updatePlayableCards();
            broadcastGameState("Drew cards from draw stack");
        } else {
            // Voluntary draw - draw one card
            if (drawDeck.size() > 0) {
                Crazy8Card drawnCard = drawDeck.dealCard(false);
                player.addCard(drawnCard);
                logEvent(player.getUsername(),"player drew a card", drawnCard.toString() , null, "1 of 1", player.getHandSize());
                if (playerStats != null) {
                    playerStats.addCardDrawn();
                }

                // Check if drawn card can be played immediately
                if (isValidPlay(drawnCard)) {
                    drawnCard.setIsPlayable(true);
                    broadcastGameState("Manual draw - card is playable");
                    // Player can choose to play it or not
                } else {
                    advanceTurn();
                    updatePlayableCards();
                    broadcastGameState("Manual draw - turn passed");
                }
            } else {
                reshuffleDeck();
                if (drawDeck.size() > 0) {
                    Crazy8Card drawnCard = drawDeck.dealCard(false);
                    player.addCard(drawnCard);
                    logEvent(player.getUsername(),"player drew a card", drawnCard.toString() , null, "1 of 1", player.getHandSize());
                    if (playerStats != null) {
                        playerStats.addCardDrawn();
                    }

                    if (isValidPlay(drawnCard)) {
                        drawnCard.setIsPlayable(true);
                        broadcastGameState("Manual draw - card is playable");
                    } else {
                        advanceTurn();
                        updatePlayableCards();
                        broadcastGameState("Manual draw - turn passed");
                    }
                }
            }
        }
    }

    private void reshuffleDeck() {
        if (playedCards.size() <= 1) {
            System.out.println("No cards to reshuffle");
            return;
        }

        // Keep the current up card, reshuffle the rest
        Crazy8Card currentUpCard = playedCards.remove(playedCards.size() - 1);

        for (Crazy8Card card : playedCards) {
            drawDeck.dealCard(false); // Add back to deck
        }

        playedCards.clear();
        playedCards.add(currentUpCard);
        drawDeck.shuffle();

        System.out.println("Deck reshuffled");
    }

    private void advanceTurn() {
        if (clockwise) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } else {
            currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
        }
    }

    private void updatePlayableCards() {
        Crazy8Player currentPlayer = players.get(currentPlayerIndex);

        boolean hasPlayableCard = false;
        for (Crazy8Card card : currentPlayer.getHand()) {
            boolean playable = isValidPlay(card);
            card.setIsPlayable(playable);
            if (playable) {
                hasPlayableCard = true;
            }
        }

        // Auto-draw if player has no playable cards
        if (!hasPlayableCard && roundInProgress) {
            System.out.println("No playable cards - auto-drawing for " + currentPlayer.getUsername());
            autoDrawCard(currentPlayer);
        }
    }

    private void autoDrawCard(Crazy8Player player) {
        Crazy8Stats playerStats = player.getCrazy8Stats();

        if (drawStack > 0) {
            // Player must draw from the draw stack
            int cardsDrawn = drawStack;
            for (int i = 0; i < drawStack; i++) {
                if (drawDeck.size() > 0) {
                    Crazy8Card tempcard = drawDeck.dealCard(false);
                    player.addCard(tempcard);
                    logEvent(player.getUsername(),"player drew a card", tempcard.toString() , null, i +" of "+ drawStack , player.getHandSize());
                    if (playerStats != null) {
                        playerStats.addCardDrawn();
                    }
                } else {
                    reshuffleDeck();
                    if (drawDeck.size() > 0) {
                        Crazy8Card tempcard = drawDeck.dealCard(false);
                        player.addCard(tempcard);
                        logEvent(player.getUsername(),"player drew a card", tempcard.toString() , null, i +" of "+ drawStack , player.getHandSize());
                        if (playerStats != null) {
                            playerStats.addCardDrawn();
                        }
                    }
                }
            }
            drawStack = 0;
            advanceTurn();
            updatePlayableCards();
            broadcastGameState("Auto-drew " + cardsDrawn + " cards (draw stack)");
        } else {
            // Normal auto-draw - draw one card
            if (drawDeck.size() > 0) {
                Crazy8Card drawnCard = drawDeck.dealCard(false);
                player.addCard(drawnCard);
                logEvent(player.getUsername(),"player drew a card", drawnCard.toString() , null, "1 of 1" , player.getHandSize());
                if (playerStats != null) {
                    playerStats.addCardDrawn();
                }

                // Check if drawn card can be played
                if (isValidPlay(drawnCard)) {
                    drawnCard.setIsPlayable(true);
                    broadcastGameState("Auto-drew card - can play it");
                    // Don't advance turn - player can play the drawn card
                } else {
                    // Can't play drawn card, move to next player
                    advanceTurn();
                    updatePlayableCards();
                    broadcastGameState("Auto-drew card - turn passed");
                }
            } else {
                reshuffleDeck();
                if (drawDeck.size() > 0) {
                    Crazy8Card drawnCard = drawDeck.dealCard(false);
                    player.addCard(drawnCard);
                    logEvent(player.getUsername(),"player drew a card", drawnCard.toString() , null, "1 of 1", player.getHandSize());
                    if (playerStats != null) {
                        playerStats.addCardDrawn();
                    }

                    if (isValidPlay(drawnCard)) {
                        drawnCard.setIsPlayable(true);
                        broadcastGameState("Auto-drew card - can play it");
                    } else {
                        advanceTurn();
                        updatePlayableCards();
                        broadcastGameState("Auto-drew card - turn passed");
                    }
                } else {
                    // No cards left at all
                    advanceTurn();
                    updatePlayableCards();
                    broadcastGameState("No cards to draw - turn passed");
                }
            }
        }
    }

    private void playerLeave(String username) {
        players.removeIf(p -> p.getUsername().equals(username));

        if (players.size() < 2) {
            roundInProgress = false;
            broadcastGameState("Not enough players - round ended");
        } else {
            // Adjust current player index if needed
            if (currentPlayerIndex >= players.size()) {
                currentPlayerIndex = 0;
            }
            updatePlayableCards();
            logEvent(username,"player left the game", null , null, null ,null);
            broadcastGameState("Player left: " + username);
        }
    }

    private void endRound(Crazy8Player winner) {
        roundInProgress = false;

        // Update stats - only winner gets game won incremented
        Crazy8Stats winnerStats = winner.getCrazy8Stats();
        if (winnerStats != null) {
            winnerStats.addGameWon();
        }
        waitingForColorChoice = false;

        // Save all stats using transactional update
        matchHistory.setWinner(winner.getUsername());
        updateStatsCrazy8();
        broadcastGameState("Round ended - Winner: " + winner.getUsername());
    }

    @Transactional
    public void updateStatsCrazy8() {
        for (Crazy8Player player : players) {
            AppUser detached = player.getUserRef(); // in-memory player object
            if (detached == null) continue;

            // Load managed user from DB
            AppUser managed = AppUserRepository.findByIdWithStats(detached.getUserID());
            if (managed == null) continue;

            // Ensure managed user has UserStats
            if (managed.getUserStats() == null) {
                managed.setUserStats(new data.User.Stats.UserStats());
                managed.getUserStats().setAppUser(managed);
            }

            // Get or create Crazy8Stats
            GameStats detachedStats = detached.getUserStats().getGameStats("Crazy8");
            GameStats managedStats = managed.getUserStats().getGameStats("Crazy8");

            if (detachedStats instanceof Crazy8Stats && managedStats instanceof Crazy8Stats) {
                copyCrazy8Stats((Crazy8Stats) detachedStats, (Crazy8Stats) managedStats);
            } else if (detachedStats instanceof Crazy8Stats && managedStats == null) {
                Crazy8Stats newStats = new Crazy8Stats();
                copyCrazy8Stats((Crazy8Stats) detachedStats, newStats);
                newStats.setUserStats(managed.getUserStats());
                managed.getUserStats().addGameStats("Crazy8", newStats);
            }
            AppUserRepository.save(managed);
            saveMatchHistory();
        }
    }

    private void copyCrazy8Stats(Crazy8Stats src, Crazy8Stats dst) {
        dst.setTimesDrewCard(src.getTimesDrewCard());
        dst.setCardsPlaced(src.getCardsPlaced());
        dst.setCrazy8Played(src.getCrazy8Played());
        dst.setSkipsPlayed(src.getSkipsPlayed());
        dst.setReversePlayed(src.getReversePlayed());
        dst.setPlus2Played(src.getPlus2Played());
        dst.setPlus4Played(src.getPlus4Played());
        dst.setGamesWon(src.getGamesWon());
    }

    private void broadcastGameState(String message) {
        if (broadcastFunction == null) return;

        try {
            ObjectNode json = mapper.createObjectNode();
            json.put("type", "gameState");
            json.put("message", message);
            json.put("currentPlayer", players.get(currentPlayerIndex).getUsername());
            json.put("roundInProgress", roundInProgress);
            json.put("currentColor", String.valueOf(currentColor));
            json.put("drawStack", drawStack);
            json.put("deckSize", drawDeck.size());

            if (upCard != null) {
                ObjectNode upCardJson = json.putObject("upCard");
                upCardJson.put("value", upCard.getValue());
                upCardJson.put("color", String.valueOf(upCard.getSuit()));
            }

            // Add player info (hand sizes only for opponents, full hand for current player)
            var playersArray = json.putArray("players");
            for (Crazy8Player player : players) {
                ObjectNode playerJson = playersArray.addObject();
                playerJson.put("username", player.getUsername());
                playerJson.put("handSize", player.getHandSize());

                // Only send full hand for current player
                if (player == players.get(currentPlayerIndex)) {
                    var handArray = playerJson.putArray("hand");
                    for (Crazy8Card card : player.getHand()) {
                        ObjectNode cardJson = handArray.addObject();
                        cardJson.put("value", card.getValue());
                        cardJson.put("color", String.valueOf(card.getSuit()));
                        cardJson.put("isPlayable", card.getIsSPlayable());
                    }
                }
            }

            broadcastFunction.accept(mapper.writeValueAsString(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastError(String username, String error) {
        if (broadcastFunction == null) return;

        try {
            ObjectNode json = mapper.createObjectNode();
            json.put("type", "error");
            json.put("username", username);
            json.put("error", error);

            broadcastFunction.accept(mapper.writeValueAsString(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Getters for testing
    public List<Crazy8Player> getPlayers() {
        return players;
    }

    public Crazy8Card getUpCard() {
        return upCard;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isRoundInProgress() {
        return roundInProgress;
    }

    public void initMatchHistory(String matchId) {
        Crazy8MatchHistoryEntity history = new Crazy8MatchHistoryEntity();
        history.setMatchId(matchId);
        history.setStartTime(LocalDateTime.now());
        this.matchHistory = history;
    }

    public void logEvent(
            String player,
            String action,
            String cardPlayed,
            String chosenColor,
            String cardsDrawn,
            Integer playerHandCount
    ) {
        Crazy8MatchEventEntity event = new Crazy8MatchEventEntity();
        event.setMatchHistory(this.matchHistory);
        event.setTimestamp(LocalDateTime.now());

        event.setPlayer(player);
        event.setAction(action);

        event.setCardPlayed(cardPlayed);
        event.setChosenColor(chosenColor);

        event.setCardsDrawn(cardsDrawn);

        event.setPlayerHandCount(playerHandCount);

        this.matchHistory.getEvents().add(event);
    }

    /**
     * Saves the data from the game
     */
    @Transactional
    public void saveMatchHistory() {
        matchHistory.setEndTime(LocalDateTime.now());
        Crazy8MatchHistoryRepository.save(matchHistory);
    }

    public String getColorName(char c) {
        switch (Character.toUpperCase(c)) {
            case 'R':
                return "RED";
            case 'G':
                return "GREEN";
            case 'B':
                return "BLUE";
            case 'Y':
                return "YELLOW";
            default:
                return "UNKNOWN";
        }
    }
}