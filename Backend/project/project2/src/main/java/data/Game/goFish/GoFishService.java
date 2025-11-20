package data.Game.goFish;

import data.User.AppUser;
import data.User.AppUserRepository;
import data.User.Stats.GameStats;
import data.User.Stats.GoFishStats;
import data.User.Stats.UserStats;
import data.User.Stats.UserStatsRepository;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer responsible for managing Go Fish games, including:
 * <ul>
 *     <li>Lobby management</li>
 *     <li>Starting and tracking active games</li>
 *     <li>Persisting user statistics at the end of games</li>
 *     <li>Processing individual player turns</li>
 * </ul>
 *
 * <p>This service interfaces with repositories that load {@link AppUser},
 * {@link UserStats}, and associated {@link GameStats}, and orchestrates
 * the flow of a Go Fish multiplayer session.</p>
 */
@Service
public class GoFishService {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    // Map joinCode -> game instance
    private final Map<String, GoFishGame> activeGames = new ConcurrentHashMap<>();

    // Track players in lobby
    private final Map<String, Set<Session>> lobbyPlayers = new ConcurrentHashMap<>();

    /**
     * Handles starting the game
     * @param usernames (list of usernames to start with)
     * @return (game)
     */
    @Transactional
    public GoFishGame startGame(String lobbyCode, List<String> usernames) {
        // Get usernames of players and put them into a list of new GoFishPlayers
        List<GoFishPlayer> players = new ArrayList<>();
        for (String name : usernames) {
            AppUser user = appUserRepository.findByUsernameWithStats(name);
            players.add(new GoFishPlayer(user));
        }

        // Start a new game with the players
        GoFishGame game = new GoFishGame(players);

        // Deal cards to all players
        game.dealCards();

        // Clean matches in hand
        for (GoFishPlayer p : players) {
            p.cleanMatchesInHand();
        }

        // Add game to list of active games
        activeGames.put(lobbyCode, game);
        return game;
    }

    /**
     * Adds a player to a specific lobby
     * @param lobbyCode (code of lobby to add player to)
     * @param session (player)
     */
    public void addPlayerToLobby(String lobbyCode, Session session) {
        lobbyPlayers.computeIfAbsent(lobbyCode, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * Removes a player to a specific lobby
     * @param lobbyCode (code of lobby to remove player from)
     * @param session (player)
     */
    public void removePlayerFromLobby(String lobbyCode, Session session) {
        Set<Session> sessions = lobbyPlayers.get(lobbyCode);
        if (sessions != null) sessions.remove(session);
    }

    /**
     * Retrieves the current game
     * @param lobbyCode (lobbyCode of game)
     * @return GoFishGame
     */
    public GoFishGame getGame(String lobbyCode) {
        return activeGames.get(lobbyCode);
    }

    /**
     * Saves all players' stats at end of a game.
     * @param lobbyCode (which game to save the results of)
     */
    @Transactional
    public void endGame(String lobbyCode) {
        GoFishGame game = activeGames.get(lobbyCode);
        if (game == null) return;

        for (GoFishPlayer player : game.getPlayers()) {
            AppUser detached = player.getUserRef(); // the instance in the game
            if (detached == null) continue;
            // load the managed user from DB
            AppUser managed = appUserRepository.findById(detached.getUserID());
            if (managed == null) continue;

            // Ensure managed has UserStats
            if (managed.getUserStats() == null) {
                managed.setUserStats(new UserStats());
                managed.getUserStats().setAppUser(managed);
            }

            // copy overall fields on UserStats -> GameStats for GoFish
            GameStats detachedGoFish = detached.getUserStats().getGameStats("GoFish");
            GameStats managedGoFish = managed.getUserStats().getGameStats("GoFish");

            if (detachedGoFish instanceof GoFishStats && managedGoFish instanceof GoFishStats) {
                copyGoFishStats((GoFishStats) detachedGoFish, (GoFishStats) managedGoFish);
                // save the managed GoFishStats (cascade from app user or save directly)
            } else if (detachedGoFish instanceof GoFishStats && managedGoFish == null) {
                // create new managed GoFishStats based on detached
                GoFishStats newManaged = new GoFishStats();
                copyGoFishStats((GoFishStats) detachedGoFish, newManaged);
                newManaged.setUserStats(managed.getUserStats());
                managed.getUserStats().addGameStats("GoFish", newManaged);
            }

            // persist managed user (cascades to userStats -> gameStats)
            appUserRepository.save(managed);
        }

        activeGames.remove(lobbyCode);
    }

    // helper method
    private void copyGoFishStats(GoFishStats src, GoFishStats dst) {
        dst.setTimesWentFishing(src.getTimesWentFishing());
        dst.setQuestionsAsked(src.getQuestionsAsked());
        dst.setBooksCollected(src.getBooksCollected());
        dst.setGamesWon(src.getGamesWon());
        dst.setGamesPlayed(src.getGamesPlayed()); // setGamesPlayed defined on GameStats
    }

    /**
     * Simulates a single turn of Go Fish
     * @param lobbyCode (game to call turn in)
     * @param askingUsername (asking player)
     * @param targetUsername (target player)
     * @param value (value of card)
     * @return (result)
     */
    @Transactional
    public String processTurn(String lobbyCode, String askingUsername, String targetUsername, int value) {
        GoFishGame game = activeGames.get(lobbyCode);
        if (game == null) { return "Game not found for lobby code " + lobbyCode + "!"; }

        GoFishPlayer asking = findPlayer(game, askingUsername);
        GoFishPlayer target = findPlayer(game, targetUsername);

        if (asking == null || target == null) {
            return "Invalid player(s)";
        }

        String result = game.takeTurn(asking, target, value);

        if (game.isGameOver()) {
            // Determine the winner first, which triggers the stats increment
            String winnerName = game.getWinner().getUsername();

            // Now persist those updated stats
            endGame(lobbyCode);

            result += " | Game over! Winner: " + winnerName;
        }

        return result;
    }

    /**
     * Finds players based off of username
     * @param username (username of player)
     * @return (GoFishPlayer
     */
    private GoFishPlayer findPlayer(GoFishGame game, String username) {
        for (GoFishPlayer p : game.getPlayers()) {
            if (p.getUsername().equals(username)) {
                return p;
            }
        }
        return null;
    }
}