package data.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LobbyController {


    @Autowired
    LobbyRepository LobbyRepository;
    @Autowired
    AppUserRepository AppUserRepository;
    
    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    /**
     * GET /Lobby
     * <p>
     * Retrieves all lobbies from the database.
     *
     * @return a list of all {@link Lobby} entities
     */
    @GetMapping(path = {"/Lobby"}) List<Lobby> getAllLobbies() {
         return this.LobbyRepository.findAll();
     }

    /**
     * GET /Lobby/{lobbyID}
     * <p>
     * Retrieves a specific lobby by its ID.
     *
     * @param lobbyID the ID of the lobby to retrieve
     * @return the {@link Lobby} entity if found, otherwise null
     */
    @GetMapping(path = {"/Lobby/{lobbyID}"})
    Lobby getLobbyById(@PathVariable long lobbyID) {
        return this.LobbyRepository.findById(lobbyID);
    }

    /**
     * POST /Lobby
     * <p>
     * Creates a new lobby and saves it to the database.
     *
     * @param Lobby the {@link Lobby} object to create
     * @return the created {@link Lobby} entity
     */
    @PostMapping(path = {"/Lobby"})
    public Lobby createLobby(@RequestBody Lobby Lobby) {
        return this.LobbyRepository.save(Lobby);
    }

    /**
     * PUT /Lobby/{lobbyID}
     * <p>
     * Updates an existing lobby by its ID. Only updates fields that are provided
     * in the request body (currently supports updating {@code gameType} and {@code users}).
     * I am gonna make a new method that can just update users inside a lobby for ease of use
     * if you want to change users you must relist users that are currently in lobby plus new additions in json
     *
     * @param lobbyID      the ID of the lobby to update
     * @param lobbyDetails the {@link Lobby} object containing fields to update
     * @return a JSON string indicating success or failure
     */
    @PutMapping(path= {"/Lobby/{lobbyID}"})
    public String updateLobby(@PathVariable long lobbyID, @RequestBody Lobby lobbyDetails){
        Lobby oldLobby= this.LobbyRepository.findById(lobbyID);
        if (oldLobby == null){
            return this.failure;
        }
        else {
            if (lobbyDetails.getGameType() != null) {
                oldLobby.setGameType(lobbyDetails.getGameType());
            }
            if (lobbyDetails.getUsers() != null) {
                oldLobby.setUsers(lobbyDetails.getUsers());
            }
            return this.success;
        }
    }

    /**
     * Updates the users in a given lobby by either adding or removing the specified user.
     * <p>
     * This endpoint is mapped to <code>/Lobby/{lobbyID}/{username}</code>.
     * It first verifies that both the lobby and the user exist. If either is not found,
     * the method returns {@code this.failure}.
     * <p>
     * If the user is already in the lobby, they are removed and their lobby reference is set to {@code null}.
     * If the user is not in the lobby, they are added and their lobby reference is updated.
     * Afterward, both the user and the lobby are saved back to their respective repositories.
     *
     * @param lobbyID  the unique identifier of the lobby to update
     * @param username the username of the user to add or remove from the lobby
     * @return {@code this.success} if the operation was successful,
     *         otherwise {@code this.failure} if the lobby or user could not be found
     */
    @PutMapping(path= {"/Lobby/{lobbyID}/{username}"})
    public String updateLobbyUsers(@PathVariable long lobbyID, @PathVariable String username){
        Lobby lobby = this.LobbyRepository.findById(lobbyID);
        if (lobby == null) {
            return this.failure; // Lobby not found
        }
        AppUser user = this.AppUserRepository.findByUsername(username);
        if (user == null) {
            return this.failure; // User not found
        }
        List<AppUser> usersInLobby = lobby.getUsers();
        if (usersInLobby.contains(user)) {
            // User already in lobby -> remove
            usersInLobby.remove(user);
            user.setLobby(null);
        } else {
            // User not in lobby -> add
            usersInLobby.add(user);
            user.setLobby(lobby);
        }
        //after proper list of users created set users in lobby to created list and save
        lobby.setUsers(usersInLobby);
        this.AppUserRepository.save(user);
        this.LobbyRepository.save(lobby);
        return this.success;
    }

    /**
     * DELETE /Lobby/{lobbyID}
     * <p>
     * Deletes a lobby by its ID. Also removes the relationship between the lobby
     * and its associated users before deletion.
     *
     * @param lobbyID the ID of the lobby to delete
     * @return a JSON string indicating success or failure
     */
    @DeleteMapping(path = {"/Lobby/{lobbyID}"})
    String deleteLobby(@PathVariable long lobbyID) {
         Lobby lobby = LobbyRepository.findById(lobbyID);
         if (lobby == null) {
             return this.failure; // Lobby not found
         }
         List<AppUser> usersInLobby = lobby.getUsers();
         for (AppUser user : usersInLobby) {
            user.setLobby(null); // break the relationship
         }
         this.AppUserRepository.saveAll(usersInLobby);
         this.LobbyRepository.deleteById(lobbyID);
         return this.success;
    }
}
