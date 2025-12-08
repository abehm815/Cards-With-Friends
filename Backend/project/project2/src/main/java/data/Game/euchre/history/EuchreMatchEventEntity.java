package data.Game.euchre.history;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "euchre_match_event")
public class EuchreMatchEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "match_history_id")
    private EuchreMatchHistoryEntity matchHistory;

    // Timestamp of the event
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Type of event:
    // "DEAL", "TRUMP_DECISION", "TRICK_PLAY", "TRICK_WIN", "SCORE_UPDATE", "MATCH_END"
    private String eventType;

    // Player taking the action (dealer, caller, trick leader, etc.)
    private String player;

    // Player team identifier (e.g., "TeamA" vs "TeamB")
    private String team;

    // For TRUMP_DECISION
    private String trumpSuit;   // e.g. "HEARTS"
    private Boolean alone;      // Did they call alone?

    // For TRICK_PLAY
    private String cardPlayed;
    private Integer trickNumber;

    // For TRICK_WIN
    private String trickWinner;

    // For SCORE_UPDATE
    private Integer teamAScore;
    private Integer teamBScore;

    // For DEAL
    private String dealer;
    private String upCard;      // card turned up during deal

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EuchreMatchHistoryEntity getMatchHistory() { return matchHistory; }
    public void setMatchHistory(EuchreMatchHistoryEntity matchHistory) { this.matchHistory = matchHistory; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getTrumpSuit() { return trumpSuit; }
    public void setTrumpSuit(String trumpSuit) { this.trumpSuit = trumpSuit; }

    public Boolean getAlone() { return alone; }
    public void setAlone(Boolean alone) { this.alone = alone; }

    public String getCardPlayed() { return cardPlayed; }
    public void setCardPlayed(String cardPlayed) { this.cardPlayed = cardPlayed; }

    public Integer getTrickNumber() { return trickNumber; }
    public void setTrickNumber(Integer trickNumber) { this.trickNumber = trickNumber; }

    public String getTrickWinner() { return trickWinner; }
    public void setTrickWinner(String trickWinner) { this.trickWinner = trickWinner; }

    public Integer getTeamAScore() { return teamAScore; }
    public void setTeamAScore(Integer teamAScore) { this.teamAScore = teamAScore; }

    public Integer getTeamBScore() { return teamBScore; }
    public void setTeamBScore(Integer teamBScore) { this.teamBScore = teamBScore; }

    public String getDealer() { return dealer; }
    public void setDealer(String dealer) { this.dealer = dealer; }

    public String getUpCard() { return upCard; }
    public void setUpCard(String upCard) { this.upCard = upCard; }
}

