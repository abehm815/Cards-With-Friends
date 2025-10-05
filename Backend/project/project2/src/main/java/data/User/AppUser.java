package data.User;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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
    private int age;

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

    // default constructor, needed for JPA
    public AppUser() {
        this.userStats = new UserStats();
    }

    //constructor for a AppUser
    public AppUser(String username, String password, String email, String firstName, String lastName, int age) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.userStats = new UserStats();
    }


    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return this.firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return this.age;
    }
    public void setAge(int age) {
        this.age = age;
    }

    public long getUserID() {
        return this.id;
    }
    public void setUserID(long userID) {
        this.id = userID;
    }

    public List<MyCard> getHand() {
        return this.hand;
    }

    public Lobby getLobby() { return lobby; }
    public void setLobby(Lobby lobby) { this.lobby = lobby; }

    public UserStats getUserStats() { return userStats; }
    public void setUserStats(UserStats stats) { this.userStats = stats; }

    @Override
    public String toString() {
        return username;
    }
}

