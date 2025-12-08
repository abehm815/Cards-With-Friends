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
}