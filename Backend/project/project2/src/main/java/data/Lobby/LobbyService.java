package data.Lobby;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling {@link Lobby}-related operations.
 * Provides methods to fetch and transform lobbies into DTOs.
 */
@Service
public class LobbyService {
    @Autowired
    private LobbyRepository lobbyRepository;

    /**
     * Retrieves all lobbies from the repository and converts them into {@link LobbyDTO} objects.
     * <p>
     * This method ensures that the list of users for each lobby is loaded
     * by accessing {@code lobby.getUsers().size()} within a transactional context,
     * preventing lazy-loading exceptions when the DTO is used outside the transaction.
     *
     * @return a list of {@link LobbyDTO} objects representing all lobbies
     */
    @Transactional
    public List<LobbyDTO> getAllLobbyDTOs() {
        List<Lobby> lobbies = lobbyRepository.findAll();
        List<LobbyDTO> dtoList = new ArrayList<>();

        for (Lobby lobby : lobbies) {
            // Force load users inside transactional context
            lobby.getUsers().size();  // triggers Hibernate to load it
            dtoList.add(new LobbyDTO(lobby));
        }

        return dtoList;
    }
}

