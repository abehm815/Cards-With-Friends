package data.Lobby;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository interface for {@link Lobby} entities.
 * Provides methods for CRUD operations and custom queries based on join codes.
 */
public interface LobbyRepository extends JpaRepository<Lobby, Long>  {
    /**
     * Finds a lobby by its ID.
     *
     * @param lobbyID the ID of the lobby
     * @return the {@link Lobby} entity if found, otherwise null
     */
    Lobby findById(long lobbyID);

    /**
     * Deletes a lobby by its ID.
     *
     * @param lobbyID the ID of the lobby to delete
     */
    @Transactional
    void deleteById(long lobbyID);

    /**
     * Finds a lobby by its join code.
     *
     * @param joinCode the join code of the lobby
     * @return the {@link Lobby} entity if found, otherwise null
     */
    Lobby findByJoinCode(String joinCode);

    /**
     * Deletes a lobby by its join code.
     *
     * @param joinCode the join code of the lobby to delete
     */
    @Transactional
    void deleteByJoinCode(String joinCode);

    /**
     * Checks whether a lobby exists with the given join code.
     *
     * @param joinCode the join code to check
     * @return true if a lobby exists with the given join code, false otherwise
     */
    boolean existsByJoinCode(String joinCode);

    /**
     * Finds a lobby by its join code and fetches the associated users in the same query.
     *
     * @param joinCode the join code of the lobby
     * @return the {@link Lobby} entity with its users loaded, or null if not found
     */
    @Query("SELECT l FROM Lobby l LEFT JOIN FETCH l.users WHERE l.joinCode = :joinCode")
    Lobby findByJoinCodeWithUsers(@Param("joinCode") String joinCode);
}
