package data.Game.BlackJack.history;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/blackjack/history")
public class BlackJackHistoryController {

    @Autowired
    private BlackJackMatchHistoryRepository matchHistoryRepository;

    /**
     * Get a full match by its ID (including events)
     */
    @GetMapping("/match/{matchId}")
    public BlackJackMatchHistoryEntity getMatchById(@PathVariable Long matchId) {
        return matchHistoryRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));
    }

    /**
     * Get all matches for a given player
     */
    @GetMapping("/player/{username}")
    public List<BlackJackMatchHistoryEntity> getMatchesByPlayer(@PathVariable String username) {
        return matchHistoryRepository.findAllByPlayer(username);
    }
}