package data.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserStatsController {
    @Autowired
    UserStatsRepository userStatsRepository;
    @Autowired
    AppUserRepository appUserRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /**
     * Gets all of the user stats
     *
     * @return List of User Stat Objects
     */
    @GetMapping(path = {"/UserStats"})
    List<UserStats> getAllUserStats() {
        return userStatsRepository.findAll();
    }

    /**
     * Gets a single user's stats
     *
     * @param id (any active user ID)
     * @return User Stat Object
     */
    @GetMapping(path = {"/UserStats/{id}"})
    UserStats getUserStats(@PathVariable long id) {
        UserStats user = userStatsRepository.findByAppUserId(id);
        if(user == null) {
            throw new RuntimeException("User ID " + id + " not found");
        }
        return user;
    }

    /**
     * Deletes user stats by the user's ID, replaces it with a null value
     * @param id (user's ID)
     */
    @DeleteMapping(path = {"/UserStats/{id}"})
    String deleteUserStats(@PathVariable long id) {
        UserStats stats = userStatsRepository.findByAppUserId(id);
        if (stats == null) {
            return failure;
        }
        AppUser user = stats.getAppUser();
        if (user == null) {
            return failure;
        }

        // Unlink
        user.setUserStats(null);
        appUserRepository.save(user);

        userStatsRepository.delete(stats);
        return success;
    }

    /**
     * Delete by user stat ID, used for cleaning
     * @param id (user stat ID)
     * @return String
     */
    @DeleteMapping(path = {"/UserStats/statID/{id}"})
    String deleteUserStatsByUserStatID(@PathVariable long id) {
        userStatsRepository.deleteById(id);
        return success;
    }

    /**
     * Creates and attatches a new empty UserStats object to a user
     * @param id (user's ID)
     * @return new UserStat
     */
    @PostMapping(path = {"/UserStats/{id}"})
    UserStats addUserStat(@PathVariable long id) {
        AppUser user = appUserRepository.findById(id);
        if (user == null) {
            throw new RuntimeException("User id: " + id + " not found");
        }

        UserStats stats = new UserStats(5);

        GameStats newGame = new GoFishStats();
        newGame.setUserStats(stats);
        stats.addGameStats("GoFish", newGame);

        newGame = new BlackjackStats();
        newGame.setUserStats(stats);
        stats.addGameStats("Blackjack", newGame);

        newGame = new EuchreStats();
        newGame.setUserStats(stats);
        stats.addGameStats("Euchre", newGame);
        userStatsRepository.save(stats);

        // Connect user and stats to each other
        stats.setAppUser(user);
        user.setUserStats(stats);


        // Save info
        appUserRepository.save(user);

        return stats;
    }

    /**
     * This put method can be used to increment any stat for any game, this is done by using the path variables
     * to select which game and stat you want to change
     * ex. /UserStats/4/GoFish/increment/goneFishing
     * @param id (any active user ID)
     * @param gameName (eg. GoFish, Euchre, Blackjack)
     * @param stat (eg. goneFishing, trickTaken, gameWon)
     * @return userStats
     */
    @PutMapping(path = {"/UserStats/{id}/{gameName}/increment/{stat}"})
    UserStats incrementStat(@PathVariable long id, @PathVariable String gameName, @PathVariable String stat) {
        // Look for user and throw error if not found
        UserStats user = userStatsRepository.findByAppUserId(id);
        if(user == null) {
            throw new RuntimeException("User ID " + id + " not found");
        }

        // Gets the stats for the specific game name used
        // Throws exception if game not found
        GameStats stats = user.getGameStats(gameName);
        if (stats == null) {
            throw new RuntimeException("Game name " + gameName + " not found");
        }

        // Place all of the increment operations here
        switch (gameName) {
            case "GoFish":
                switch (stat) {
                    case "goneFishing": ((GoFishStats) stats).addWentFishing(); break;
                    case "questionAsked": ((GoFishStats) stats).addQuestionAsked(); break;
                    case "bookCollected": ((GoFishStats) stats).addBookCollected(); break;
                    case "gameWon": ((GoFishStats) stats).addGameWon(); break;
                    case "gamePlayed": stats.addGamePlayed(); break;
                    default: throw new RuntimeException("Stat " + stat + " not valid");
                }
                break;

            case "Euchre":
                switch (stat) {
                    case "trickTaken": ((EuchreStats) stats).addTrickTaken(); break;
                    case "pickedUp": ((EuchreStats) stats).addTimePickedUp(); break;
                    case "goneAlone": ((EuchreStats) stats).addTimeGoneAlone(); break;
                    case "sweepWon": ((EuchreStats) stats).addSweepWon(); break;
                    case "gameWon": ((EuchreStats) stats).addGameWon(); break;
                    case "gamePlayed": stats.addGamePlayed(); break;
                    default: throw new RuntimeException("Stat " + stat + " not valid");
                }
                break;

            case "Blackjack":
                switch (stat) {
                    case "betWon": ((BlackjackStats) stats).addBetWon(); break;
                    case "doubledDown": ((BlackjackStats) stats).addTimeDoubledDown(); break;
                    case "split": ((BlackjackStats) stats).addTimeSplit(); break;
                    case "hit": ((BlackjackStats) stats).addTimeHit(); break;
                    case "gameWon": ((BlackjackStats) stats).addGameWon(); break;
                    case "gamePlayed": stats.addGamePlayed(); break;
                    default: throw new RuntimeException("Stat " + stat + " not valid");
                }
                break;
        }

        return userStatsRepository.save(user);
    }

    /**
     * This put method can be used to set any stat for any game, this is done by using the path variables
     * to select which game and stat you want to change
     *  ex. /UserStats/4/GoFish/set/goneFishing/10
     * @param id (any active user ID)
     * @param gameName (eg. GoFish, Euchre, Blackjack)
     * @param stat (eg. goneFishing, trickTaken, gameWon)
     * @param amount (what you want to set the stat to)
     * @return UserStats
     */
    @PutMapping(path = {"/UserStats/{id}/{gameName}/set/{stat}/{amount}"})
    UserStats setStat(@PathVariable long id, @PathVariable String gameName, @PathVariable String stat, @PathVariable int amount) {
        // Look for user and throw error if not found
        UserStats user = userStatsRepository.findByAppUserId(id);
        if(user == null) {
            throw new RuntimeException("User ID " + id + " not found");
        }

        // Gets the stats for the specific game name used
        // Throws exception if game not found
        GameStats stats = user.getGameStats(gameName);
        if (stats == null) {
            throw new RuntimeException("Game name not found");
        }

        // Place all of the increment operations here
        switch (gameName) {
            case "GoFish":
                switch (stat) {
                    case "goneFishing": ((GoFishStats) stats).setTimesWentFishing(amount); break;
                    case "questionAsked": ((GoFishStats) stats).setQuestionsAsked(amount); break;
                    case "bookCollected": ((GoFishStats) stats).setBooksCollected(amount); break;
                    case "gameWon": ((GoFishStats) stats).setGamesWon(amount); break;
                    case "gamePlayed": stats.setGamesPlayed(amount); break;
                    default: throw new RuntimeException("Stat " + stat + " not valid");
                }
                break;

            case "Euchre":
                switch (stat) {
                    case "trickTaken": ((EuchreStats) stats).setTricksTaken(amount); break;
                    case "pickedUp": ((EuchreStats) stats).setTimesPickedUp(amount); break;
                    case "goneAlone": ((EuchreStats) stats).setTimesGoneAlone(amount); break;
                    case "sweepWon": ((EuchreStats) stats).setSweepsWon(amount); break;
                    case "gameWon": ((EuchreStats) stats).setGamesWon(amount); break;
                    case "gamePlayed": stats.setGamesPlayed(amount); break;
                    default: throw new RuntimeException("Stat " + stat + " not valid");
                }
                break;

            case "Blackjack":
                switch (stat) {
                    case "betWon": ((BlackjackStats) stats).setBetsWon(amount); break;
                    case "doubledDown": ((BlackjackStats) stats).setTimesDoubledDown(amount); break;
                    case "split": ((BlackjackStats) stats).setTimesSplit(amount); break;
                    case "hit": ((BlackjackStats) stats).setTimesHit(amount); break;
                    case "gameWon": ((BlackjackStats) stats).setGamesWon(amount); break;
                    case "gamePlayed": stats.setGamesPlayed(amount); break;
                    default: throw new RuntimeException("Stat" + stat + " not valid");
                }
                break;
        }

        return userStatsRepository.save(user);
    }

}
