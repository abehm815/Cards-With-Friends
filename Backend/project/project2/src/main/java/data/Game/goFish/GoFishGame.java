package data.Game.goFish;

import data.Game.MyCard;
import data.Game.goFish.history.GoFishMatchEventEntity;
import data.Game.goFish.history.GoFishMatchHistoryEntity;
import data.User.Stats.GoFishStats;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a full game of Go Fish, managing players, the deck,
 * turn order, card requests, and end-game logic.
 *
 * <p>This class handles dealing cards, executing player turns,
 * refreshing empty hands, determining when the game ends,
 * and computing the winner based on completed books.</p>
 */
public class GoFishGame {
    private List<GoFishPlayer> players;
    private GoFishDeck deck;
    private int currentPlayerIndex;
    private GoFishMatchHistoryEntity matchHistory;

    /**
     * Basic constructor that takes a list of players in the game
     * @param players (List of players)
     */
    public GoFishGame(List<GoFishPlayer> players) {
        this.players = players;
        this.deck = new GoFishDeck();
        deck.shuffle();
        this.currentPlayerIndex = 0;
        dealCards();
    }

    /**
     * Gets the current player by the currentPlayerIndex
     * @return username
     */
    public String getCurrentPlayerUsername() {
        return players.get(currentPlayerIndex).getUsername();
    }

    /**
     * Deals cards to all players in game, hand size changes depending on number
     * of players
     */
    public void dealCards() {
        int cardsPerPlayer = players.size() <= 3 ? 7 : 5;
        for (int i = 0; i < cardsPerPlayer; i++) {
            for (GoFishPlayer player : players) {
                player.addCard(deck.dealCard());
            }
        }
    }

    /**
     * Takes all the information needed for a turn and sees if a player has those cards
     * @param askingPlayer (Player asking for a card)
     * @param targetPlayer (Player we are searching for value)
     * @param value (Value of the card)
     * @return (Message on if the asking player went fishing or found the card)
     */
    public String takeTurn(GoFishPlayer askingPlayer, GoFishPlayer targetPlayer, int value) {
        // Get user's stats to allow for updates
        GoFishStats askingPlayerStats = askingPlayer.getStats();
        GoFishStats targetPlayerStats = targetPlayer.getStats();

        // Question asked stat increment
        if (askingPlayerStats != null) { askingPlayerStats.addQuestionAsked(); }

        // Search asking player's hand for a card of correct value
        // You cannot ask for a card you do not have
        if (askingPlayer.checkForCard(value) == null) {
            return askingPlayer.getUsername() + " does not have the card of value " + value + "!";
        }

        // Search target player's hand for a card of rank value
        MyCard targetCard = targetPlayer.checkForCard(value);

        // If target card is null, that means it was not found, Go Fish!
        if (targetCard == null) {
            // Draw Card
            MyCard drawn = deck.dealCard();

            // Checks if Deck is empty
            if (drawn == null) {
                logEvent(askingPlayer.getUsername(), "goFishing", targetPlayer.getUsername(), value, "Deck Empty!");
                return askingPlayer.getUsername() + " went fishing, but the deck is empty!";
            }

            // Add card to player's hand and check for matches
            askingPlayer.addCard(drawn);
            askingPlayer.cleanMatchesInHand(matchHistory);

            // Increment Turn and add to went fishing stat
            if (askingPlayerStats != null) { askingPlayerStats.addWentFishing(); }
            nextTurn();
            logEvent(askingPlayer.getUsername(), "goFishing", targetPlayer.getUsername(), value, drawn.toString());
            return askingPlayer.getUsername() + " went fishing and drew a " + drawn.toString();
        } else {
            // Target Player loses card
            targetPlayer.removeCard(targetCard);
            // Check if target player's hand is empty
            checkRefresh(targetPlayer);
            // Asking Player gains card
            askingPlayer.addCard(targetCard);
            // Clean matches in askingPlayer's hand
            askingPlayer.cleanMatchesInHand(matchHistory);
            // Check if asking player's hand is empty
            checkRefresh(askingPlayer);
            logEvent(askingPlayer.getUsername(), "ask", targetPlayer.getUsername(), value, targetCard.toString());
            return askingPlayer.getUsername() + " received a " + targetCard.toString() + " from " + targetPlayer.getUsername();
        }
    }

    /**
     * Updates turn by incrementing it
     */
    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    /**
     * Checks a player's hand and sees if it needs refreshed
     * @param player (player whos hand we are checking)
     */
    public void checkRefresh(GoFishPlayer player) {
        if (player.getHandSize() == 0) {
            // Add cards to players hand if they are out
            int cardsPerPlayer = players.size() <= 3 ? 7 : 5;
            for (int i = 0; i < cardsPerPlayer; i++) {
                MyCard drawn = deck.dealCard();
                // Check if card is null
                if (drawn != null) {
                    player.addCard(drawn);
                }
            }
            // Clean any new matches made
            player.cleanMatchesInHand(matchHistory);
        }
    }

    /**
     * Gets username of given player
     * @param player (GoFishPlayer)
     * @return username of given player
     */
    public String getUserName(GoFishPlayer player) {
        return player.getUsername();
    }

    /**
     * Checks if game is over
     * @return true if game is over, false otherwise
     */
    public boolean isGameOver() {
        // Checks if every player's hand is empty
        for (GoFishPlayer player : players) {
            if (player.getHandSize() != 0) {
                return false;
            }
        }
        // If we reach here, every player's hand must be empty
        return deck.isEmpty();
    }

    /**
     * Returns all players in current game
     * @return List of players
     */
    public List<GoFishPlayer> getPlayers() {
        return players;
    }

    /**
     * Goes through all the players and sees which one has the most books collected
     * @return Winning player
     */
    public GoFishPlayer getWinner() {
        GoFishPlayer winner = players.get(0);
        for (GoFishPlayer player : players) {
            if (player.getCompletedBooks().size() > winner.getCompletedBooks().size()) {
                winner = player;
            }
        }

        // Update wins and games played stats
        if(this.isGameOver()) {
            for (GoFishPlayer player : players) {
                GoFishStats stats = player.getStats();
                if (stats != null) {
                    stats.addGamePlayed();
                    if (player == winner) { stats.addGameWon(); }
                }
            }
        }

        return winner;
    }

    /**
     * Initializes a new match history using the lobby id as match id
     * @param matchId lobby id
     */
    public void initMatchHistory(String matchId) {
        GoFishMatchHistoryEntity history = new GoFishMatchHistoryEntity();
        history.setMatchId(matchId);
        history.setStartTime(LocalDateTime.now());
        this.matchHistory = history;
    }

    /**
     * Helper for logging an event
     * @param player player who did the action
     * @param action what action the player did
     * @param target who the player targeted
     * @param cardValue value of card
     * @param cardDrawn card drawn (for fishing)
     */
    public void logEvent(String player, String action, String target, Integer cardValue, String cardDrawn) {
        GoFishMatchEventEntity event = new GoFishMatchEventEntity();
        event.setMatchHistory(this.matchHistory);
        event.setTimestamp(LocalDateTime.now());
        event.setPlayer(player);
        event.setAction(action);
        event.setTarget(target);
        event.setCardValue(cardValue);
        event.setCardDrawn(cardDrawn);

        this.matchHistory.getEvents().add(event);
    }

    /**
     * Gets match history
     * @return match history
     */
    public GoFishMatchHistoryEntity getMatchHistory() {
        return matchHistory;
    }

    /**
     * Helps with persisting match history
     */
    public void cleanMatchesHelper() {
        // Clean matches in hand
        for (GoFishPlayer p : players) {
            p.cleanMatchesInHand(matchHistory);
        }
    }
}
