package data.Game.goFish.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoFishMatchEventRepository extends JpaRepository<GoFishMatchEventEntity, Long> {
    List<GoFishMatchEventEntity> findByMatchHistory_MatchId(String matchId);
}