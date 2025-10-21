package data.Lobby;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class LobbyService {
    @Autowired
    private LobbyRepository lobbyRepository;

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

