package data.Game.Crazy8.history;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Crazy8/history")
public class Crazy8HistoryController {

    @Autowired
    private Crazy8MatchHistoryRepository matchHistoryRepository;

    /**
     * Get a full match by its ID (including events)
     */
    @GetMapping("/match/{matchId}")
    public Crazy8MatchHistoryEntity getMatchById(@PathVariable Long matchId) {
        return matchHistoryRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));
    }

    /**
     * Get all matches for a given player
     */
    @GetMapping("/player/{username}")
    public List<Crazy8MatchHistoryEntity> getMatchesByPlayer(@PathVariable String username) {
        return matchHistoryRepository.findAllByPlayer(username);
    }
}
