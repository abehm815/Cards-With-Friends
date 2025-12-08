package data.Game.goFish.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gofish/history")
public class GoFishHistoryController {

    @Autowired
    private GoFishMatchHistoryRepository matchHistoryRepository;

    /**
     * Get a full match by its ID (including events)
     */
    @GetMapping("/match/{matchId}")
    public GoFishMatchHistoryEntity getMatchById(@PathVariable Long matchId) {
        return matchHistoryRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));
    }

    /**
     * Get all matches for a given player
     */
    @GetMapping("/player/{username}")
    public List<GoFishMatchHistoryEntity> getMatchesByPlayer(@PathVariable String username) {
        return matchHistoryRepository.findAllByPlayer(username);
    }

    /**
     * Delete a match using its database ID
     */
    @DeleteMapping("/match/{id}")
    public String deleteMatchById(@PathVariable Long id) {
        if (!matchHistoryRepository.existsById(id)) {
            throw new RuntimeException("Match not found: " + id);
        }
        matchHistoryRepository.deleteById(id);
        return "Deleted GoFish match with ID: " + id;
    }

    /**
     * Delete a match using its matchId (UUID)
     */
    @DeleteMapping("/match/by-matchId/{matchId}")
    public String deleteMatchByMatchId(@PathVariable String matchId) {
        GoFishMatchHistoryEntity match = matchHistoryRepository.findByMatchId(matchId);
        if (match == null) {
            throw new RuntimeException("Match not found: " + matchId);
        }
        matchHistoryRepository.delete(match);
        return "Deleted GoFish match with matchId: " + matchId;
    }

    /**
     * Delete all matches involving a given player
     */
    @DeleteMapping("/player/{username}")
    public String deleteMatchesByPlayer(@PathVariable String username) {
        List<GoFishMatchHistoryEntity> matches = matchHistoryRepository.findAllByPlayer(username);
        if (matches.isEmpty()) {
            return "No matches found for: " + username;
        }
        matchHistoryRepository.deleteAll(matches);
        return "Deleted " + matches.size() + " matches for player: " + username;
    }
}