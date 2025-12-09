package data.Game.Crazy8.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Crazy8MatchEventRepository extends JpaRepository<Crazy8MatchEventEntity, Long> {
    List<Crazy8MatchEventEntity> findByMatchHistory_MatchId(String matchId);
}
