package data.Lobby;

import data.User.AppUser;
import data.User.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * REST controller for managing game lobbies.
 * Provides endpoints to create, update, retrieve, and delete lobbies.
 */
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
     * Retrieves all {@link Lobby} objects that match the specified {@link GameType}.
     * This endpoint filters the list of all lobbies to only include those whose
     * {@code gameType} field matches the value provided in the URL path.
     *Example request:
     * GET /Lobby/BLACKJACK
     * GET /Lobby/GO_FISH
     * GET /Lobby/EUCHRE
     *
     * @param gameType the type of game to filter lobbies by (parsed automatically from the URL).
     *                 Must be a valid constant of the {@link GameType} enum.
     * @return a list of {@link Lobby} entities whose {@code gameType} matches the given value.
     */
    @GetMapping(path = {"/Lobby/GameType/{gameType}"}) List<Lobby> getAllLobbiesByGameType(@PathVariable GameType gameType) {
        List<Lobby> lobbies = this.LobbyRepository.findAll();
        List<Lobby> lobbiesGameType= new ArrayList<>();
        for (Lobby lobby : lobbies) {
            if (lobby.getGameType() == gameType) {
                lobbiesGameType.add(lobby);
            }
        }
        return lobbiesGameType;
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
     * GET /Lobby/{lobbyID}
     * <p>
     * Retrieves a specific lobby by its ID.
     *
     * @param joinCode the joinCode of the lobby to retrieve
     * @return the {@link Lobby} entity if found, otherwise null
     */
    @GetMapping(path = {"/Lobby/joinCode/{joinCode}"})
    Lobby getLobbyByJoinCode(@PathVariable String joinCode) {
        return this.LobbyRepository.findByJoinCode(joinCode);
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
        Lobby savedLobby = this.LobbyRepository.save(Lobby);
        LobbyListWebSocket.broadcastLobbyList(); // Notify WebSocket clients
        return savedLobby;
    }

    /**
     * POST /Lobby/joinCode/autogen/{username}
     * <p>
     * Creates a new lobby with a randomly generated unique join code and adds the specified user as host.
     *
     * @param lobby    the {@link Lobby} object to create
     * @param username the username of the host
     * @return the generated join code
     */
    @PostMapping(path = "/Lobby/joinCode/autogen/{username}")
    public String createLobbyWithautogen(@RequestBody Lobby lobby, @PathVariable String username) {
        AppUser host = this.AppUserRepository.findByUsername(username);
        if (host == null) {
            // Handle if user not found (optional)
            return null;
        }
        //set join code
        String joinCode;
        // Loop until a unique join code is found
        do {
            joinCode = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
        } while (this.LobbyRepository.existsByJoinCode(joinCode));

        lobby.setJoinCode(joinCode);

        // Create a list of users with the host
        List<AppUser> users = new ArrayList<>();
        users.add(host);

        // Assign users to the lobby
        lobby.setUsers(users);

        // Assign lobby to user
        host.setLobby(lobby);

        // Save the lobby and return it
        Lobby savedLobby = this.LobbyRepository.save(lobby);
        this.AppUserRepository.save(host);
        LobbyListWebSocket.broadcastLobbyList();
        return  joinCode;
    }


    /**
     * POST /Lobby/joinCode/{joinCode}/{username}
     * <p>
     * Creates a lobby with a specified join code and adds the host user.
     *
     * @param lobby    the {@link Lobby} object to create
     * @param username the username of the host
     * @param joinCode the join code for the lobby
     * @return the created {@link Lobby} entity
     */
    @PostMapping(path = "/Lobby/joinCode/{joinCode}/{username}")
    public Lobby createLobbyWithJoinCode(@RequestBody Lobby lobby, @PathVariable String username, @PathVariable String joinCode) {
        // Find the user creating the lobby (host)
        AppUser host = this.AppUserRepository.findByUsername(username);
        if (host == null) {
            // Handle if user not found (optional)
            return null;
        }
        //set join code
        lobby.setJoinCode(joinCode);

        // Create a list of users with the host
        List<AppUser> users = new ArrayList<>();
        users.add(host);

        // Assign users to the lobby
        lobby.setUsers(users);

        // Assign lobby to user
        host.setLobby(lobby);

        // Save the lobby and return it
        Lobby savedLobby = this.LobbyRepository.save(lobby);
        this.AppUserRepository.save(host);
        LobbyListWebSocket.broadcastLobbyList();
        return  savedLobby;
    }

    /**
     * DELETE /Lobby/empty
     * <p>
     * Deletes all empty lobbies from the database.
     *
     * @return JSON message indicating how many lobbies were deleted
     */
    @DeleteMapping(path = {"/Lobby/empty"})
    public String deleteEmptyLobbies() {
        List<Lobby> allLobbies = this.LobbyRepository.findAll();
        List<Lobby> emptyLobbies = new ArrayList<>();

        for (Lobby lobby : allLobbies) {
            if (lobby.getUsers() == null || lobby.getUsers().isEmpty()) {
                emptyLobbies.add(lobby);
            }
        }

        if (emptyLobbies.isEmpty()) {
            return "{\"message\":\"no empty lobbies found\"}";
        }

        // For each empty lobby, delete it and make sure relationships are cleared
        for (Lobby lobby : emptyLobbies) {
            this.LobbyRepository.delete(lobby);
        }

        LobbyListWebSocket.broadcastLobbyList();
        return "{\"message\":\"deleted " + emptyLobbies.size() + " empty lobbies\"}";
    }

    /**
     * PUT /Lobby/{lobbyID}
     * <p>
     * Updates an existing lobby by its ID. Only updates fields that are provided
     * in the request body (currently supports updating {@code gameType} and {@code users}).
     * I am going to make a new method that can just update users inside a lobby for ease of use
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
            if(lobbyDetails.getJoinCode() != null) {
                oldLobby.setJoinCode(lobbyDetails.getJoinCode());
            }
            this.LobbyRepository.save(oldLobby);
            LobbyListWebSocket.broadcastLobbyList();
            return this.success;
        }
    }

    /**
     * PUT /Lobby/joinCode/{joinCode}
     * <p>
     * Updates an existing lobby by its join code. Only updates fields provided in the request body.
     *
     * @param joinCode     the join code of the lobby to update
     * @param lobbyDetails the {@link Lobby} object containing fields to update
     * @return JSON string indicating success or failure
     */
    @PutMapping(path= {"/Lobby/joinCode/{joinCode}"})
    public String updateLobbyByCode(@PathVariable String joinCode, @RequestBody Lobby lobbyDetails){
        Lobby oldLobby= this.LobbyRepository.findByJoinCode(joinCode);
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
            if(lobbyDetails.getJoinCode() != null){
                oldLobby.setJoinCode(lobbyDetails.getJoinCode());
            }
            this.LobbyRepository.save(oldLobby);
            LobbyListWebSocket.broadcastLobbyList();
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
        LobbyListWebSocket.broadcastLobbyList();
        return this.success;
    }

    /**
     * PUT /Lobby/joinCode/{joinCode}/{username}
     * <p>
     * Adds or removes a user from a lobby by join code.
     * If the user is already in the lobby, they are removed; otherwise, they are added.
     *
     * @param joinCode the join code of the lobby
     * @param username the username of the user to add or remove
     * @return JSON string indicating success or failure
     */
    @PutMapping(path= {"/Lobby/joinCode/{joinCode}/{username}"})
    public String updateLobbyUsersByCode(@PathVariable String joinCode, @PathVariable String username){
        Lobby lobby = this.LobbyRepository.findByJoinCode(joinCode);
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
        LobbyListWebSocket.broadcastLobbyList();
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
         LobbyListWebSocket.broadcastLobbyList();
         return this.success;
    }


    /**
     * DELETE /Lobby/joinCode/{joinCode}
     * <p>
     * Deletes a lobby by its join code and clears relationships with users.
     *
     * @param joinCode the join code of the lobby to delete
     * @return JSON string indicating success or failure
     */
    @DeleteMapping(path = {"/Lobby/joinCode/{joinCode}"})
    String deleteLobbyByCode(@PathVariable String joinCode) {
        Lobby lobby = LobbyRepository.findByJoinCode(joinCode);
        if (lobby == null) {
            return this.failure; // Lobby not found
        }
        List<AppUser> usersInLobby = lobby.getUsers();
        for (AppUser user : usersInLobby) {
            user.setLobby(null); // break the relationship
        }
        this.AppUserRepository.saveAll(usersInLobby);
        this.LobbyRepository.deleteByJoinCode(joinCode);
        LobbyListWebSocket.broadcastLobbyList();
        return this.success;
    }
}

