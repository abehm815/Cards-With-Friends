package data.Game.Crazy8;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.Lobby.Lobby;
import data.Lobby.LobbyRepository;
import data.User.AppUser;
import data.User.AppUserRepository;
import data.User.Stats.Crazy8Stats;
import data.User.Stats.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Crazy8Game {
    @Autowired
    LobbyRepository LobbyRepository;
    @Autowired
    AppUserRepository AppUserRepository;
    @Autowired
    UserStatsRepository userStatsRepository;

    public void setLobbyRepository(LobbyRepository repo) {
        this.LobbyRepository = repo;
    }

    public void setAppUserRepository(AppUserRepository repo) {
        this.AppUserRepository = repo;
    }

    public void setUserStatsRepository(UserStatsRepository repo) {
        this.userStatsRepository = repo;
    }

    private Consumer<String> broadcastFunction;
    private static final ObjectMapper mapper = new ObjectMapper();

    public void setBroadcastFunction(Consumer<String> broadcastFunction) {
        this.broadcastFunction = broadcastFunction;
    }

    private Crazy8Deck drawDeck;
    private List<Crazy8Card> playedCards;
    private Crazy8Card upCard;
    private List<Crazy8Player> players;
    private String lobbyCode;
    private int currentPlayerIndex = 0; // Track whose turn it is
    private boolean roundInProgress = false;

    public Crazy8Game(String lobbyCode) {
        this.players = new ArrayList<>();
        drawDeck = new Crazy8Deck(1);
        this.playedCards = new ArrayList<>();
    }

    /**
     * Initializes a blackjack game by loading all users from the specified lobby
     * and creating player objects for them.
     *
     * @param joinCode the joinCode of the lobby to load players from
     */
    public void initializeGameFromLobby(String joinCode) {
        this.lobbyCode = joinCode;

        Lobby lobby = LobbyRepository.findByJoinCodeWithUsers(joinCode);

        if (lobby == null) {
            System.out.println("Lobby not found with join code: " + joinCode);
            return;
        }

        List<AppUser> users = lobby.getUsers();
        List<String> userNames = new ArrayList<>();
        for (AppUser user : users) {
            userNames.add(user.getUsername());
        }

        // Clear any existing players before reloading
        players.clear();

        //use jakes query to add users to blackjack game
        for (String name : userNames) {
            AppUser user = AppUserRepository.findByUsernameWithStats(name);
            players.add(new Crazy8Player(user));
        }

        System.out.println("Initialized game with " + players.size() + " players.");
    }

    public void startRound() {
        drawDeck.shuffle();
        currentPlayerIndex = 0;
        roundInProgress = true;

        // Clear all player hands
        for (Crazy8Player player : players) {
            player.getHand().clear();
        }
        System.out.println("New round started dealing cards");
        for (Crazy8Player player : players) {
            for(int i = 0; i < 6; i++) {
                player.addCard(drawDeck.dealCard(false));
            }
        }
        playedCards.add(drawDeck.dealCard(false));

    }

    public void handlePlayerDecision(String username, String decision,char cardcolor, int value) {
        if (!roundInProgress) return;

        Crazy8Player currentPlayer = players.get(currentPlayerIndex);
        Crazy8Stats currentPlayerStats = currentPlayer.getCrazy8Stats();

        if (currentPlayerStats == null) {
            System.out.println("Current player stats is null");
            return;
        }
        switch (decision.toUpperCase()) {

            case "PLAYCARD":

                //Playcard(cardcolor,value);
                break;

            case "LEAVE":
               // playerLeave(username);
                break;

            default:
                System.out.println("Invalid decision: " + decision);
        }
    }
}
