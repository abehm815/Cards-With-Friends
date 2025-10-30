package data.Lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface LobbyRepository extends JpaRepository<Lobby, Long>  {
    Lobby findById(long lobbyID);

    @Transactional
    void deleteById(long lobbyID);

    Lobby findByJoinCode(String joinCode);

    @Transactional
    void deleteByJoinCode(String joinCode);

    boolean existsByJoinCode(String joinCode);

    @Query("SELECT l FROM Lobby l LEFT JOIN FETCH l.users WHERE l.joinCode = :joinCode")
    Lobby findByJoinCodeWithUsers(@Param("joinCode") String joinCode);
}
