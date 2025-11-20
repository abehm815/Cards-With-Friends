package data.User;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import data.Game.MyCard;
import data.Lobby.Lobby;
import data.User.Stats.UserStats;
import jakarta.persistence.*;

/**
 * Represents an application user within the system. This entity stores authentication
 * details, personal information, administrative roles, lobby participation state,
 * and associated gameplay statistics.
 *
 * <p>This class is persisted via JPA and is mapped to a relational database table.
 * Each AppUser holds a {@link UserStats} object that tracks the user's performance
 * across supported games.</p>
 */
@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    /**
    / Does not allow users to have the same username if users try and have same username exception thrown
     **/
    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String age;

    @Column(nullable = false)
    private boolean isModerator = false;

    @Column(nullable = false)
    private boolean isAdmin = false;

    @Transient
    private boolean inLobby = false;

    @Transient
    private List<MyCard> hand;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_stats_id", referencedColumnName = "id")
    @JsonIgnore
    private UserStats userStats;

    @ManyToOne
    @JoinColumn(name = "lobby_id")
    @JsonBackReference
    private Lobby lobby;

    /**
     * Default constructor required by JPA. Initializes a new {@link UserStats}
     * object and links it to this user.
     */
    public AppUser() {
        this.userStats = new UserStats();
        this.userStats.setAppUser(this);
    }

    /**
     * Constructs a new AppUser with the provided identifying and personal information.
     *
     * @param username  the unique username for the user
     * @param password  the user's password
     * @param email     the user's email address
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param age       the user's age
     */
    public AppUser(String username, String password, String email, String firstName, String lastName, String age) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.userStats = new UserStats();
    }

    /** @return the user's username */
    public String getUsername() {
        return this.username;
    }
    /** @param username sets the user's username */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the user's password */
    public String getPassword() {
        return this.password;
    }
    /** @param password sets the user's password */
    public void setPassword(String password) {
        this.password = password;
    }

    /** @return the user's email address */
    public String getEmail() {
        return this.email;
    }
    /** @param email sets the user's email address */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the user's first name */
    public String getFirstName() {
        return this.firstName;
    }
    /** @param firstName sets the user's first name */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** @return the user's last name */
    public String getLastName() {
        return this.lastName;
    }
    /** @param lastName sets the user's last name */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** @return the user's age */
    public String getAge() {
        return this.age;
    }
    /** @param age sets the user's age */
    public void setAge(String age) {
        this.age = age;
    }

    /** @return the user's unique internal ID */
    public long getUserID() {
        return this.id;
    }
    /** @param userID sets the user's internal ID */
    public void setUserID(long userID) {
        this.id = userID;
    }

    /** @return the user's current hand of cards (not persistent) */
    public List<MyCard> getHand() {
        return this.hand;
    }

    /** @return the lobby the user belongs to */
    public Lobby getLobby() { return lobby; }
    /** @param lobby sets the lobby association */
    public void setLobby(Lobby lobby) { this.lobby = lobby; }

    /** @return the user's statistics */
    public UserStats getUserStats() { return userStats; }
    /** @param stats sets the user's statistics object */
    public void setUserStats(UserStats stats) { this.userStats = stats; }

    /** @return whether the user is a moderator */
    public boolean isModerator() { return isModerator; }
    /** @param moderator sets moderator privileges */
    public void setModerator(boolean moderator) { this.isModerator = moderator; }

    /** @return whether the user is an administrator */
    public boolean isAdmin() { return isAdmin; }
    /** @param admin sets administrator privileges */
    public void setAdmin(boolean admin) { this.isAdmin = admin; }

    /** @return whether the user is currently in a lobby */
    public boolean isInLobby() { return inLobby; }
    /** @param lobby sets whether the user is in a lobby */
    public void setInLobby(boolean lobby) { this.inLobby = lobby; }

    /**
     * Returns the username as the string representation of the user.
     *
     * @return the user's username
     */
    @Override
    public String toString() {
        return username;
    }
}

