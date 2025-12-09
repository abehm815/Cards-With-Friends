package data.Game.Crazy8.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crazy8_match_history")
public class Crazy8MatchHistoryEntity {
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

    private String winner;


    // List of events in the match
    @OneToMany(mappedBy = "matchHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Crazy8MatchEventEntity> events = new ArrayList<>();

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public List<Crazy8MatchEventEntity> getEvents() { return events; }
    public void setEvents(List<Crazy8MatchEventEntity> events) { this.events = events; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
}
