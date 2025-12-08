package data.Game.BlackJack.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlackJackMatchEventRepository extends JpaRepository<BlackJackMatchEventEntity, Long>{
    List<BlackJackMatchEventEntity> findByMatchHistory_MatchId(String matchId);
}
