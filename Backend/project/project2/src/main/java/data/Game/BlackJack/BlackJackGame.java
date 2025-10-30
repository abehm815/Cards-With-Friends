package data.Game.BlackJack;
import com.fasterxml.jackson.databind.ObjectMapper;
import data.User.AppUser;
import data.User.AppUserRepository;
import data.Lobby.Lobby;
import data.Lobby.LobbyRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class BlackJackGame {
    @Autowired
    LobbyRepository LobbyRepository;
    @Autowired
    AppUserRepository AppUserRepository;

    private Consumer<String> broadcastFunction;
    private static final ObjectMapper mapper = new ObjectMapper();

    public void setBroadcastFunction(Consumer<String> broadcastFunction) {
        this.broadcastFunction = broadcastFunction;
    }

    private BlackJackDealer dealer;
    private BlackJackDeck deck;
    private List<BlackJackPlayer> players;
    private String lobbyCode;
    private int currentPlayerIndex = 0; // Track whose turn it is
    private boolean roundInProgress = false;

    public BlackJackGame(String lobbyCode) {
        this.players = new ArrayList<>();
        deck = new BlackJackDeck(6);
        this.dealer = new BlackJackDealer();
    }

    /**
     * Initializes a blackjack game by loading all users from the specified lobby
     * and creating player objects for them.
     *
     * @param joinCode the joinCode of the lobby to load players from
     */
    public void initializeGameFromLobby(String joinCode) {
        this.lobbyCode = joinCode;
        //Lobby lobby = LobbyRepository.findByJoinCode(joinCode);
        Lobby lobby = LobbyRepository.findByJoinCodeWithUsers(joinCode);

        if (lobby == null) {
            System.out.println("Lobby not found with join code: " +  joinCode );
            return;
        }

        // Clear any existing players before reloading
        players.clear();

        // Loop through all users in the lobby and create player objects
        for (AppUser user : lobby.getUsers()) {
            BlackJackPlayer player = new BlackJackPlayer(user.getUsername(), 1000); // starting chips
            players.add(player);
        }

        System.out.println("Initialized game with " + players.size() + " players.");
    }

    public void startRound() {
        deck.shuffle();
        dealer.resetHand();
        currentPlayerIndex = 0;
        roundInProgress = true;

        // Clear all player hands
        for (BlackJackPlayer player : players) {
            player.getHand().clear();
            player.setBetOnCurrentHand(0);
            player.setHasStood(false);
        }
        System.out.println("New round started. Waiting for all players to place bets...");
    }

    public void handlePlayerBet(String username, int amount) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null) return;

        takeBet(amount, player);
        player.setHasBet(true);
        System.out.println(username + " placed a bet of " + amount);

        // Check if all players have placed bets
        boolean allBetsPlaced = players.stream().allMatch(BlackJackPlayer::getHasBet);

        if (allBetsPlaced) {
            System.out.println("All bets placed. Dealing cards...");
            dealInitialCards();
            notifyCurrentPlayerTurn();
        }
    }

    public void handlePlayerDecision(String username, String decision) {
        if (!roundInProgress) return;

        BlackJackPlayer currentPlayer = players.get(currentPlayerIndex);

        switch (decision.toUpperCase()) {
            case "HIT":
                playerHit(username);
                if (currentPlayer.getHandValue() > 21) {
                    System.out.println(username + " busts!");
                    advanceTurn();
                }
                else if(currentPlayer.getHandValue() == 21) {
                    currentPlayer.setHasStood(true);
                    advanceTurn();
                }
                break;

            case "STAND":
                playerStand(currentPlayer.getUsername());
                System.out.println(username + " stands.");
                advanceTurn();
                break;

            case "DOUBLE":
                playerDouble(currentPlayer.getUsername());
                System.out.println(username + " doubles.");
                advanceTurn();
                break;
/*
            case "SPLIT":
                playerSplit(currentPlayer.getUsername());

 */

            default:
                System.out.println("Invalid decision: " + decision);
        }
    }
    private void advanceTurn() {
        currentPlayerIndex++;

        if (currentPlayerIndex >= players.size()) {
            dealer.playTurn(deck);
            compareHandsAndResolveBets();
            roundInProgress = false;
            System.out.println("Round complete. Results computed.");
        } else {
            notifyCurrentPlayerTurn();
        }
    }

    private void notifyCurrentPlayerTurn() {
        String username = players.get(currentPlayerIndex).getUsername();
        System.out.println("It's now " + username + "'s turn.");

        // Build JSON message
        Map<String, Object> message = new HashMap<>();
        message.put("type", "TURN_NOTIFICATION");
        message.put("lobbyCode", lobbyCode);
        message.put("currentTurn", username);

        try {
            String json = mapper.writeValueAsString(message);
            if (broadcastFunction != null) {
                broadcastFunction.accept(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealInitialCards() {
        for (int i = 0; i < 2; i++) {
            for (BlackJackPlayer player : players) {
                player.addCard(deck.dealCard(true));
            }
            if (i==1){dealer.addCard(deck.dealCard(true));}
            else{
                dealer.addCard(deck.dealCard(false));
            }
        }
    }

    private void takeBet(int bet,BlackJackPlayer player) {
            if (bet <= 0 || bet > player.getChips()) {
                System.out.println(player.getUsername() + " has invalid bet: " + bet);
                player.setBetOnCurrentHand(0);
            } else {
                player.setBetOnCurrentHand(bet);
                System.out.println(player.getUsername() + " bet " + bet + " chips.");
                player.setChips(player.getChips() - bet);
            }
        }

    public void playerStand(String username) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null || player.getHasStood() || !roundInProgress) return;

        player.setHasStood(true);
        System.out.println(username + " stands.");
    }

    private void nextTurn() {
        // Move to next player who hasn’t stood/busted
        do {
            currentPlayerIndex++;
        } while (currentPlayerIndex <= players.size() &&
                (players.get(currentPlayerIndex).getHasStood() || players.get(currentPlayerIndex).getHandValue() > 21));

        if (currentPlayerIndex > players.size()) {
            // All players done — dealer plays
            dealer.playTurn(deck);
            compareHandsAndResolveBets();
            roundInProgress = false;
        }
    }

    public void playerHit(String username) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null || player.getHasStood() || !roundInProgress) return;

        player.addCard(deck.dealCard(true));
        System.out.println(username + " hits. Hand value: " + player.getHandValue());

    }
    public void playerDouble(String username) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null || player.getHasStood() || !roundInProgress) return;


        player.setChips(player.getChips() - player.getBetOnCurrentHand());
        player.setBetOnCurrentHand(player.getBetOnCurrentHand()*2);
        player.addCard(deck.dealCard(true));
        playerStand(username);
    }

    /*public void playerSplit(String username) {
        BlackJackPlayer player = getPlayer(username);
        player.getHand();
        //TODO create split hand logic so player can have 2 hands
    }
     */

    private void compareHandsAndResolveBets() {
        dealer.getHand().forEach(card -> card.setIsShowing(true));
        int dealerValue = dealer.getHandValue();
        boolean dealerBust = dealerValue > 21;

        System.out.println("Dealer hand value: " + dealerValue);

        for (BlackJackPlayer player : players) {
            int playerValue = player.getHandValue();
            boolean playerBust = playerValue > 21;

            System.out.print(player.getUsername() + " (" + playerValue + "): ");
            if (playerBust) {
                //player bust-loss
            } else if (dealerBust) {
                //dealer bust-win
                player.setChips(player.getChips() + 2*(player.getBetOnCurrentHand()));
            } else if (playerValue > dealerValue) {
                //player hand better that dealer-win
                player.setChips(player.getChips() + 2*(player.getBetOnCurrentHand()));
            } else if (playerValue == dealerValue) {
                //dealer and player hand the same-tie
                // no chip change
                player.setChips(player.getChips() + player.getBetOnCurrentHand());
            } else {//dealer hand better than player-loss
               // player.setChips(player.getChips() - player.getBetOnCurrentHand());
            }
        }
    }

    public BlackJackPlayer getPlayer(String username) {
        for (BlackJackPlayer player : players) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }
        return null; // no match found
    }

    public boolean isRoundInProgress() {
        return roundInProgress;
    }

    public List<BlackJackPlayer> getPlayers() {
        return players;
    }

    public BlackJackDealer getDealer() {
        return dealer;
    }

    public String getLobbyCode() {
        return lobbyCode;
    }

    public Map<String, Object> toDTO() {
        Map<String, Object> dto = new HashMap<>();
        dto.put("lobbyCode", lobbyCode);
        dto.put("roundInProgress", roundInProgress);
        dto.put("dealer", Map.of(
                "hand", dealer.getHand(),
                "handValue", dealer.getHandValue()
        ));
        dto.put("players", players.stream().map(p -> Map.of(
                "username", p.getUsername(),
                "chips", p.getChips(),
                "hand", p.getHand(),
                "handValue", p.getHandValue(),
                "bet", p.getBetOnCurrentHand(),
                "hasStood", p.getHasStood(),
                "hasBet", p.getHasBet()
        )).collect(Collectors.toList()));
        dto.put("currentTurn", currentPlayerIndex < players.size()
                ? players.get(currentPlayerIndex).getUsername()
                : null);
        return dto;
    }
    public void setLobbyRepository(LobbyRepository repo) {
        this.LobbyRepository = repo;
    }

    public void setAppUserRepository(AppUserRepository repo) {
        this.AppUserRepository = repo;
    }

    }


