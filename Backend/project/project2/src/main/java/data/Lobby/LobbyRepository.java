package data.Lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface LobbyRepository extends JpaRepository<Lobby, Long>  {
    Lobby findById(long lobbyID);

    @Transactional
    void deleteById(long lobbyID);

    Lobby findByJoinCode(String joinCode);

    @Transactional
    void deleteByJoinCode(String joinCode);

}
