package data.Game.BlackJack;
import data.User.AppUser;
import data.User.AppUserRepository;
import data.User.Lobby;
import data.User.LobbyRepository;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


public class BlackJackGame {
    @Autowired
    LobbyRepository LobbyRepository;
    @Autowired
    AppUserRepository AppUserRepository;

    private BlackJackDealer dealer;
    private BlackJackDeck deck;
    private List<BlackJackPlayer> players;

    public BlackJackGame() {
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
        Lobby lobby = LobbyRepository.findByJoinCode(joinCode);

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
        dealer.resetHand()

        // Clear all player hands
        for (BlackJackPlayer player : players) {
            player.getHand().clear();
        }
        for (BlackJackPlayer player : players) {
            boolean confirmed = false;
            while(!confirmed) {
                int value = 0;//TODO: get value of players bet from front end
                takeBet(value, player);
                confirmed = true; //TODO:get confirmed from front end after button press
            }
        }
        dealInitialCards();
        // TODO: take inputs for players decidions to hit or stand
        dealer.playTurn(deck);
        //compare hands of players and dealer to decide who won
        compareHandsAndResolveBets(dealer,players);
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
            }
        }

    private void compareHandsAndResolveBets(BlackJackDealer dealer, List<BlackJackPlayer> players) {
        int dealerValue = dealer.getHandValue();
        boolean dealerBust = dealerValue > 21;

        System.out.println("Dealer hand value: " + dealerValue);

        for (BlackJackPlayer player : players) {
            int playerValue = player.getHandValue();
            boolean playerBust = playerValue > 21;

            System.out.print(player.getUsername() + " (" + playerValue + "): ");
            if (playerBust) {
                //player bust-loss
                player.setChips(player.getChips() - player.getBetOnCurrentHand());
            } else if (dealerBust) {
                //dealer bust-win
                player.setChips(player.getChips() + player.getBetOnCurrentHand());
            } else if (playerValue > dealerValue) {
                //player hand better that dealer-win
                player.setChips(player.getChips() + player.getBetOnCurrentHand());
            } else if (playerValue == dealerValue) {
                //dealer and player hand the same-tie
                // no chip change
            } else {//dealer hand better than player-loss
                player.setChips(player.getChips() - player.getBetOnCurrentHand());
            }
        }
    }
}


