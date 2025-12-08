package data.Game.goFish.history;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gofish_match_event")
public class GoFishMatchEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "match_history_id")
    private GoFishMatchHistoryEntity matchHistory;

    // Timestamp of the event
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Player performing the action
    private String player;

    // Action performed
    private String action;

    // Target player (if any)
    private String target;

    // Card value involved in the action
    private Integer cardValue;

    // Card drawn (if any)
    private String cardDrawn;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GoFishMatchHistoryEntity getMatchHistory() { return matchHistory; }
    public void setMatchHistory(GoFishMatchHistoryEntity matchHistory) { this.matchHistory = matchHistory; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPlayer() { return player; }
    public void setPlayer(String player) { this.player = player; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public Integer getCardValue() { return cardValue; }
    public void setCardValue(Integer cardValue) { this.cardValue = cardValue; }

    public String getCardDrawn() { return cardDrawn; }
    public void setCardDrawn(String cardDrawn) { this.cardDrawn = cardDrawn; }
}
