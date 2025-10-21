package data.Game.goFish;

import data.Game.MyCard;

import java.util.List;

public class GoFishGame {
    private List<GoFishPlayer> players;
    private GoFishDeck deck;
    private int currentPlayerIndex;

    /**
     * Basic constructor that takes a list of players in the game
     * @param players (List of players)
     */
    public GoFishGame(List<GoFishPlayer> players) {
        this.players = players;
        this.deck = new GoFishDeck();
        this.currentPlayerIndex = 0;
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

    public String takeTurn(GoFishPlayer askingPlayer, GoFishPlayer targetPlayer, int value) {
        // Search target player's hand for a card of rank value
        MyCard targetCard = targetPlayer.checkForCard(value);

        // If target card is null, that means it was not found, Go Fish!
        if (targetCard == null) {
            // Draw Card
            askingPlayer.addCard(deck.dealCard());
            // Check if a match was found
            askingPlayer.cleanMatchesInHand();
            // Increment Turn
            nextTurn();
            return askingPlayer.getUsername() + " went fishing!";
        } else {
            // Target Player loses card
            targetPlayer.removeCard(targetCard);
            // Asking Player gains card
            askingPlayer.addCard(targetCard);
            // Clean matches in askingPlayer's hand
            askingPlayer.cleanMatchesInHand();
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
        GoFishPlayer winner = new GoFishPlayer("holder");
        for (GoFishPlayer player : players) {
            if (player.getCompletedBooks().size() > winner.getCompletedBooks().size()) {
                winner = player;
            }
        }
        return winner;
    }
}
