package data.Game.euchre.history;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EuchreMatchHistoryRepository extends JpaRepository<EuchreMatchHistoryEntity, Long> {

    EuchreMatchHistoryEntity findByMatchId(String matchId);

    /**
     * Returns all matches where the player participated
     */
    @Query("""
        SELECT m 
        FROM EuchreMatchHistoryEntity m 
        JOIN m.events e 
        WHERE e.player = :username
    """)
    List<EuchreMatchHistoryEntity> findAllByPlayer(@Param("username") String username);
}
