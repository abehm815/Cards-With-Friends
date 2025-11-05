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
            for (List<BlackJackCard> hand : player.getHands()) {
                hand.clear();
            }
            player.setHasBet(false);
            player.setHasStood(false);
            player.resetAllBets();
            player.resetAllHasStood();
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
            for(BlackJackPlayer user : players){
                if(user.calculateHandValue(user.getHand())==21){
                    user.setHasStood(true);
                }
            }
            currentPlayerIndex--;
            advanceTurn();
            notifyCurrentPlayerTurn();
        }
    }

    public void handlePlayerDecision(String username, String decision) {
        if (!roundInProgress) return;

        BlackJackPlayer currentPlayer = players.get(currentPlayerIndex);

        switch (decision.toUpperCase()) {

            case "HIT":
                playerHit(username);
                if (currentPlayer.getHandValue() > 21 && currentPlayer.isOnLastHand()) {
                    System.out.println(username + " busts!");
                    advanceTurn();
                }
                else if(currentPlayer.getHandValue() > 21 && !currentPlayer.isOnLastHand()) {
                    System.out.println(username + " busts!");
                    currentPlayer.moveToNextHand();
                }
                else if(currentPlayer.getHandValue() == 21 &&  !currentPlayer.isOnLastHand()) {
                    currentPlayer.setHasStood(true);
                    currentPlayer.moveToNextHand();
                }
                else if(currentPlayer.getHandValue() == 21 && currentPlayer.isOnLastHand()) {
                    currentPlayer.setHasStood(true);
                    advanceTurn();
                }
                break;

            case "STAND":
                if (currentPlayer.isOnLastHand()){
                    playerStand(currentPlayer.getUsername());
                    advanceTurn();
                    System.out.println(username + " stands.");
                }
                else if (!currentPlayer.isOnLastHand()){
                    playerStand(currentPlayer.getUsername());
                    System.out.println(username + " stands.");
                    currentPlayer.moveToNextHand();
                }
                break;

            case "DOUBLE":
                if  (currentPlayer.isOnLastHand()) {
                    playerDouble(currentPlayer.getUsername());
                    System.out.println(username + " doubles.");
                    advanceTurn();
                }
               else if(!currentPlayer.isOnLastHand()) {
                    playerDouble(currentPlayer.getUsername());
                    System.out.println(username + " doubles.");
                    currentPlayer.moveToNextHand();
                }
                break;


            case "SPLIT":
                playerSplit(currentPlayer.getUsername());
                break;

            default:
                System.out.println("Invalid decision: " + decision);
        }
    }
    private void advanceTurn() {
        // Move to next player who hasn't stood yet
        do {
            currentPlayerIndex++;
        } while (currentPlayerIndex < players.size() && players.get(currentPlayerIndex).getHasStoodForHand());

        // If all players are done, let dealer play and end round
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
        if (player == null || player.getHasStoodForHand() || !roundInProgress) return;

        player.setHasStood(true);
        System.out.println(username + " stands.");
    }


    public void playerHit(String username) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null || player.getHasStoodForHand() || !roundInProgress) return;

        player.addCard(deck.dealCard(true));
        System.out.println(username + " hits. Hand value: " + player.getHandValue());

    }
    public void playerDouble(String username) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null || player.getHasStoodForHand() || !roundInProgress) return;


        player.setChips(player.getChips() - player.getBetOnCurrentHand());
        player.setBetOnCurrentHand(player.getBetOnCurrentHand()*2);
        player.addCard(deck.dealCard(true));
        playerStand(username);
    }

    public void playerSplit(String username) {
        BlackJackPlayer player = getPlayer(username);
        if (player == null || !roundInProgress) return;

        List<BlackJackCard> hand = player.getHand();

        if (!player.canSplit()) {
            System.out.println(username + " cannot split — cards are not the same rank.");
            return;
        }

        int originalBet = player.getBetOnCurrentHand();
        if (player.getChips() < originalBet) {
            System.out.println(username + " does not have enough chips to split.");
            return;
        }

        // Deduct chips for second hand
        player.setChips(player.getChips() - originalBet);

        // Split cards into two hands
        BlackJackCard firstCard = hand.get(0);
        BlackJackCard secondCard = hand.get(1);

        hand.clear();
        hand.add(firstCard); // first hand keeps first card
        hand.add(deck.dealCard(true));
        player.setBetOnCurrentHand(originalBet); // original bet stays

        // Create second hand
        List<BlackJackCard> secondHand = new ArrayList<>();
        secondHand.add(secondCard);
        secondHand.add(deck.dealCard(true)); // deal one card to second hand
        player.addHand(secondHand, originalBet); // second hand bet = original bet


        System.out.println(username + " splits into two hands with separate bets.");
    }


    private void compareHandsAndResolveBets() {
        // Reveal dealer cards
        dealer.getHand().forEach(card -> card.setIsShowing(true));
        int dealerValue = dealer.getHandValue();
        boolean dealerBust = dealerValue > 21;

        System.out.println("Dealer hand value: " + dealerValue);

        for (BlackJackPlayer player : players) {
            List<List<BlackJackCard>> hands = player.getHands();
            List<Integer> bets = player.getBets();

            for (int i = 0; i < hands.size(); i++) {
                List<BlackJackCard> hand = hands.get(i);
                int bet = (bets != null && i < bets.size()) ? bets.get(i) : 0;
                int playerValue = player.getHandValue(hand);
                boolean playerBust = playerValue > 21;

                System.out.print(player.getUsername() + " Hand " + (i + 1) + " (" + playerValue + "): ");

                if (playerBust) {
                    System.out.println("Bust! Lost bet of " + bet);
                    // Loss: do nothing since chips already deducted when bet was placed
                } else if (dealerBust) {
                    System.out.println("Dealer bust! Won " + bet * 2);
                    player.setChips(player.getChips() + 2 * bet);
                } else if (playerValue > dealerValue) {
                    System.out.println("Win! Won " + bet * 2);
                    player.setChips(player.getChips() + 2 * bet);
                } else if (playerValue == dealerValue) {
                    System.out.println("Tie! Bet returned: " + bet);
                    player.setChips(player.getChips() + bet);
                } else {
                    System.out.println("Loss! Lost bet of " + bet);

                }
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

        // Dealer info
        dto.put("dealer", Map.of(
                "hand", dealer.getHand(),
                "handValue", dealer.getHandValue()
        ));

        // Players info
        dto.put("players", players.stream().map(player -> {
            List<Map<String, Object>> playerHands = new ArrayList<>();
            List<List<BlackJackCard>> hands = player.getHands();
            List<Integer> bets = player.getBets();

            int originalHandIndex = 0; // save original hand index
            try {
                java.lang.reflect.Field currentHandIndexField = BlackJackPlayer.class.getDeclaredField("currentHandIndex");
                currentHandIndexField.setAccessible(true);
                originalHandIndex = (int) currentHandIndexField.get(player);

                for (int i = 0; i < hands.size(); i++) {
                    // Temporarily set current hand index
                    currentHandIndexField.set(player, i);

                    List<BlackJackCard> hand = hands.get(i);
                    int handValue = player.getHandValue(hand);
                    int bet = (bets != null && i < bets.size()) ? bets.get(i) : 0;
                    boolean hasStood = player.getHasStoodForHand();
                    boolean canSplit = player.canSplit();

                    playerHands.add(Map.of(
                            "handIndex", i,
                            "hand", hand,
                            "handValue", handValue,
                            "bet", bet,
                            "hasStood", hasStood,
                            "canSplit", canSplit
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Restore original current hand index
                try {
                    java.lang.reflect.Field currentHandIndexField = BlackJackPlayer.class.getDeclaredField("currentHandIndex");
                    currentHandIndexField.setAccessible(true);
                    currentHandIndexField.set(player, originalHandIndex);
                } catch (Exception ignored) {}
            }

            return Map.of(
                    "username", player.getUsername(),
                    "chips", player.getChips(),
                    "hands", playerHands,
                    "hasBet", player.getHasBet()
            );
        }).collect(Collectors.toList()));

        // Current turn
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



