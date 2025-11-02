package data.Game.goFish;

import data.User.AppUser;
import data.User.AppUserRepository;
import data.User.Stats.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoFishService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    private GoFishGame game;
    private boolean gameStarted = false;

    /**
     * Handles starting the game
     * @param usernames (list of usernames to start with)
     * @return (game)
     */
    public GoFishGame startGame(List<String> usernames) {
        // Get usernames of players and put them into a list of new GoFishPlayers
        List<GoFishPlayer> players = new ArrayList<>();
        for (String name : usernames) {
            AppUser user = appUserRepository.findByUsername(name);
            players.add(new GoFishPlayer(user));
        }

        // Start a new game with the players
        game = new GoFishGame(players);

        // Deal cards to all players
        game.dealCards();

        // Updated boolean
        this.gameStarted = true;

        // Clean matches in hand
        for (GoFishPlayer p : players) {
            p.cleanMatchesInHand();
        }

        return game;
    }

    /**
     * Returns whether the game has started
     * @return (gameStarted)
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Returns the game object
     * @return (currentGame)
     */
    public GoFishGame getCurrentGame() {
        return game;
    }

    /**
     * Saves all players' stats at end of a game.
     */
    public void saveGameResults(GoFishGame game) {
        for (GoFishPlayer p : game.getPlayers()) {
            if (p.getUserRef() != null) {
                long id = p.getUserRef().getUserID();
                AppUser user = appUserRepository.findById(id);
                if (user != null && user.getUserStats() != null) {
                    userStatsRepository.save(user.getUserStats());
                }
            }
        }
    }

    /** Execute one turn and return the result message. */
    public String processTurn(String askingUsername, String targetUsername, int value) {
        if (!gameStarted || game == null) {
            return "Game not started yet!";
        }

        GoFishPlayer asking = findPlayer(askingUsername);
        GoFishPlayer target = findPlayer(targetUsername);

        if (asking == null || target == null) {
            return "Invalid player(s)";
        }

        String result = game.takeTurn(asking, target, value);

        if (game.isGameOver()) {
            saveGameResults(this.game);
            result += " | Game over! Winner: " + game.getWinner().getUsername();
            gameStarted = false;
        }

        return result;
    }

    /**
     * Finds players based off of username
     * @param username (username of player)
     * @return (GoFishPlayer
     */
    private GoFishPlayer findPlayer(String username) {
        for (GoFishPlayer p : game.getPlayers()) {
            if (p.getUsername().equals(username)) {
                return p;
            }
        }
        return null;
    }
}