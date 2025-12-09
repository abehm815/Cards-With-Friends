package data.Game.euchre;

import data.Game.euchre.history.EuchreMatchHistoryRepository;
import data.User.AppUser;
import data.User.AppUserRepository;
import data.User.Stats.EuchreStats;
import data.User.Stats.GameStats;
import data.User.Stats.UserStats;
import data.User.Stats.UserStatsRepository;
import jakarta.transaction.Transactional;
import jakarta.websocket.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service similar to GoFishService adapted to Euchre.
 * Responsibilities:
 *  - create and store active EuchreGame instances
 *  - manage lobby sessions (map of lobbyCode -> sessions)
 *  - process basic actions: start, pass, choose suit, pick up, play
 *  - persist stats on endGame
 */
@Service
public class EuchreService {
    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private EuchreMatchHistoryRepository euchreMatchHistoryRepository;

    private final Map<String, EuchreGame> activeGames = new ConcurrentHashMap<>();
    private final Map<String, Set<Session>> lobbyPlayers = new ConcurrentHashMap<>();

    @Transactional
    public EuchreGame startGame(String lobbyCode, List<String> usernames) {
        // Add players to list, if username not recognized use "Guest"
        List<EuchrePlayer> players = new ArrayList<>();
        for (String name : usernames) {
            AppUser user = appUserRepository.findByUsernameWithStats(name);
            if (user != null) {
                players.add(new EuchrePlayer(user));
            } else {
                // fallback to simple player
                players.add(new EuchrePlayer(name));
            }
        }

        EuchreGame game = new EuchreGame(players);

        // Inititalize Match History
        game.initMatchHistory(lobbyCode);
        game.getMatchHistory().setStartTime(LocalDateTime.now());

        game.startGame();
        activeGames.put(lobbyCode, game);
        return game;
    }

    public void addPlayerToLobby(String lobbyCode, Session session) {
        lobbyPlayers.computeIfAbsent(lobbyCode, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removePlayerFromLobby(String lobbyCode, Session session) {
        Set<Session> sessions = lobbyPlayers.get(lobbyCode);
        if (sessions != null) sessions.remove(session);
    }

    public EuchreGame getGame(String lobbyCode) {
        return activeGames.get(lobbyCode);
    }

    @Transactional
    public void endGame(String lobbyCode) {
        EuchreGame game = activeGames.get(lobbyCode);
        if (game == null) return;

        // Persist stats per player (pattern copied from GoFishService)
        for (EuchrePlayer player : game.getPlayers()) {
            AppUser detached = player.getUserRef();
            if (detached == null) continue;
            AppUser managed = appUserRepository.findById(detached.getUserID());
            if (managed == null) continue;

            if (managed.getUserStats() == null) {
                managed.setUserStats(new UserStats());
                managed.getUserStats().setAppUser(managed);
            }

            GameStats detachedEuchre = detached.getUserStats() != null ? detached.getUserStats().getGameStats("Euchre") : null;
            GameStats managedEuchre = managed.getUserStats().getGameStats("Euchre");

            if (detachedEuchre instanceof EuchreStats && managedEuchre instanceof EuchreStats) {
                copyEuchreStats((EuchreStats) detachedEuchre, (EuchreStats) managedEuchre);
            } else if (detachedEuchre instanceof EuchreStats && managedEuchre == null) {
                EuchreStats newManaged = new EuchreStats();
                copyEuchreStats((EuchreStats) detachedEuchre, newManaged);
                newManaged.setUserStats(managed.getUserStats());
                managed.getUserStats().addGameStats("Euchre", newManaged);
            }

            appUserRepository.save(managed);
        }

        // Save match history
        saveMatchHistory(game, game.getWinner().getTeamMembersAsStrings());

        activeGames.remove(lobbyCode);
    }

    private void copyEuchreStats(EuchreStats src, EuchreStats dst) {
        // copy relevant fields existing on EuchreStats
        dst.setGamesPlayed(src.getGamesPlayed());
        dst.setGamesWon(src.getGamesWon());
        dst.setTricksTaken(src.getTricksTaken());
        dst.setSweepsWon(src.getSweepsWon());
        dst.setTimesGoneAlone(src.getTimesGoneAlone());
        dst.setTimesPickedUp(src.getTimesPickedUp());
    }

    // -- Game action processors --

    public String processPass(String lobbyCode, String username) {
        EuchreGame game = activeGames.get(lobbyCode);
        if (game == null) return "No active game for lobby " + lobbyCode;
        if (!username.equals(game.getCurrentPlayerUsername())) return username + " is not the current player! It is " + game.getCurrentPlayerUsername() + "'s turn!";
        game.playerPasses();
        return username + " passed.";
    }

    public String processChooseSuit(String lobbyCode, String username, char suit) {
        EuchreGame game = activeGames.get(lobbyCode);
        if (game == null) return "No active game for lobby " + lobbyCode;

        // only allows current player to choose suit
        game.playerChoosesSuit(suit);
        return username + " chose " + game.charSuitToString(suit) + " as trump.";
    }

    public String processPickUp(String lobbyCode, String dealerUsername, int value, char suit) {
        EuchreGame game = activeGames.get(lobbyCode);
        if (game == null) return "No active game for lobby " + lobbyCode;

        EuchrePlayer dealer = findPlayerByUsername(game, dealerUsername);
        if (dealer == null) return "Dealer not found: " + dealerUsername;
        if (!dealerUsername.equals(game.getCurrentDealerUsername())) return dealerUsername + " is not the current dealer! The dealer is " + game.getCurrentDealerUsername();

        // find the actual card object in dealer's hand that matches value and suit
        EuchreCard dropped = findCardInHand(dealer, value, suit);
        if (dropped == null) return "Dropped card not found in dealer's hand.";

        boolean success = game.playerPicksUp(dropped);
        return success ? dealerUsername + " picked up the option card." : "Pick up failed.";
    }

    public String processPlay(String lobbyCode, String username, int value, char suit) {
        EuchreGame game = activeGames.get(lobbyCode);
        if (game == null) return "No active game for lobby " + lobbyCode;

        EuchrePlayer player = findPlayerByUsername(game, username);
        if (player == null) return "Player not found: " + username;
        if (!player.getUsername().equals(game.getCurrentPlayerUsername())) return username + " is not the current player! It is " + game.getCurrentPlayerUsername() + "'s turn!";

        EuchreCard card = findCardInHand(player, value, suit);
        if (card == null) return username + " does not have that card.";

        String result = game.takeTurn(player, card);

        // If trick complete (4 cards) give trick
        if (game.getCurrentTrick().size() == 4) {
            if (game.getPlayers() != null) {
                try {
                    game.giveTrick();
                } catch (Exception ex) {
                    // ignore; keep server robust
                }
            }
        }

        // If all tricks have been played, add them up and give points
        if (game.getPlayers().get(0).getHand().isEmpty()) {
            game.givePoints();

            // Check for end of game condition
            EuchreTeam winnerTeam = game.getWinner();
            if (winnerTeam != null) {
                // We have a winning team; persist and remove game
                endGame(lobbyCode);
                return result + " | Game over! Winning team score: " + winnerTeam.getScore();
            } else {
                // Winner was not found, begin new round
                game.startNewRound();
            }
        }



        return result;
    }

    // -- helpers --

    private EuchrePlayer findPlayerByUsername(EuchreGame game, String username) {
        for (EuchrePlayer p : game.getPlayers()) {
            if (p.getUsername().equals(username)) return p;
        }
        return null;
    }

    private EuchreCard findCardInHand(EuchrePlayer player, int value, char suit) {
        for (EuchreCard c : player.getHand()) {
            if (c.getValue() == value && c.getSuit() == suit) return c;
        }
        return null;
    }

    /**
     * Saves the data from the game
     * @param game current game
     * @param winningTeam winning team
     */
    @Transactional
    public void saveMatchHistory(EuchreGame game, List<String> winningTeam) {
        game.getMatchHistory().setWinningPlayers(winningTeam);
        game.getMatchHistory().setEndTime(LocalDateTime.now());
        euchreMatchHistoryRepository.save(game.getMatchHistory());
    }
}
