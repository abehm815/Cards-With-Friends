package data.Game.euchre.history;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EuchreMatchEventRepository extends JpaRepository<EuchreMatchEventEntity, Long> {
    List<EuchreMatchEventEntity> findByMatchHistory_MatchId(String matchId);
}