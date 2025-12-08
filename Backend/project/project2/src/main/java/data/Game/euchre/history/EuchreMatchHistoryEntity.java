package data.Game.euchre.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "euchre_match_history")
public class EuchreMatchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique ID of the match
    private String matchId;

    // Match start time
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    // Match end time
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    // Winners of the match
    @ElementCollection
    @CollectionTable(name = "euchre_winning_players", joinColumns = @JoinColumn(name = "match_history_id"))
    @Column(name = "player")
    private List<String> winningPlayers = new ArrayList<>();

    // List of events in the match
    @OneToMany(mappedBy = "matchHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<EuchreMatchEventEntity> events = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<String> getWinningPlayers() { return winningPlayers; }
    public void setWinningPlayers(List<String> winningPlayers) { this.winningPlayers = winningPlayers; }

    public List<EuchreMatchEventEntity> getEvents() { return events; }
    public void setEvents(List<EuchreMatchEventEntity> events) { this.events = events; }
}

