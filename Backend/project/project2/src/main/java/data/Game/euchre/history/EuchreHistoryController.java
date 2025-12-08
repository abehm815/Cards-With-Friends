package data.Game.euchre.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/euchre/history")
public class EuchreHistoryController {

    @Autowired
    private EuchreMatchHistoryRepository matchHistoryRepository;

    /**
     * Get a match by its database ID (primary key)
     */
    @GetMapping("/match/db/{id}")
    public EuchreMatchHistoryEntity getMatchByDbId(@PathVariable Long id) {
        return matchHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id));
    }

    /**
     * Get a match by its unique matchId (UUID from your game)
     */
    @GetMapping("/match/{matchId}")
    public EuchreMatchHistoryEntity getMatchByMatchId(@PathVariable String matchId) {
        EuchreMatchHistoryEntity match = matchHistoryRepository.findByMatchId(matchId);
        if (match == null) {
            throw new RuntimeException("Match not found: " + matchId);
        }
        return match;
    }

    /**
     * Get all matches a player participated in
     */
    @GetMapping("/player/{username}")
    public List<EuchreMatchHistoryEntity> getMatchesByPlayer(@PathVariable String username) {
        return matchHistoryRepository.findAllByPlayer(username);
    }

    /**
     * Delete a match by DB id
     */
    @DeleteMapping("/match/db/{id}")
    public String deleteMatchByDbId(@PathVariable Long id) {
        if (!matchHistoryRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete: match not found: " + id);
        }
        matchHistoryRepository.deleteById(id);
        return "Deleted match with DB id: " + id;
    }

    /**
     * Delete a match by matchId (string)
     */
    @DeleteMapping("/match/{matchId}")
    public String deleteMatchByMatchId(@PathVariable String matchId) {
        EuchreMatchHistoryEntity match = matchHistoryRepository.findByMatchId(matchId);
        if (match == null) {
            throw new RuntimeException("Cannot delete: match not found: " + matchId);
        }
        matchHistoryRepository.delete(match);
        return "Deleted match with matchId: " + matchId;
    }

    /**
     * Delete all matches a player participated in
     */
    @DeleteMapping("/player/{username}")
    public String deleteAllPlayerMatches(@PathVariable String username) {
        List<EuchreMatchHistoryEntity> matches = matchHistoryRepository.findAllByPlayer(username);
        if (matches.isEmpty()) {
            return "No matches found for user: " + username;
        }
        matchHistoryRepository.deleteAll(matches);
        return "Deleted " + matches.size() + " matches for player: " + username;
    }

    /**
     * Delete ALL euchre history (admin/debug method)
     */
    @DeleteMapping("/all")
    public String deleteAllHistory() {
        long count = matchHistoryRepository.count();
        matchHistoryRepository.deleteAll();
        return "Deleted ALL Euchre match history (" + count + " records).";
    }
}