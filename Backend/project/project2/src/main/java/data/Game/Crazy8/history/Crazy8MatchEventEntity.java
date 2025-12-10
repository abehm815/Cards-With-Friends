package data.Game.Crazy8.history;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "crazy8_match_event")
public class Crazy8MatchEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "match_history_id")
    private Crazy8MatchHistoryEntity matchHistory;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Player who performed the action
    private String player;

    // Action taken (PLAY_CARD, DRAW_CARD, WILD, REVERSE, SKIP, UNO_CALLED, etc.)
    private String action;

    // Card played (e.g., "RED_5", "BLUE_SKIP", "WILD", "WILD_DRAW_FOUR")
    private String cardPlayed;

    // Chosen color for wild card actions
    private String chosenColor;

    // Number of cards drawn (0 if none)
    private String cardsDrawn;

    // How many cards the acting player has after the move
    private Integer playerHandCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Crazy8MatchHistoryEntity getMatchHistory() {
        return matchHistory;
    }

    public void setMatchHistory(Crazy8MatchHistoryEntity matchHistory) {
        this.matchHistory = matchHistory;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCardPlayed() {
        return cardPlayed;
    }

    public void setCardPlayed(String cardPlayed) {
        this.cardPlayed = cardPlayed;
    }

    public String getChosenColor() {
        return chosenColor;
    }

    public void setChosenColor(String chosenColor) {
        this.chosenColor = chosenColor;
    }

    public String getCardsDrawn() {
        return cardsDrawn;
    }

    public void setCardsDrawn(String cardsDrawn) {
        this.cardsDrawn = cardsDrawn;
    }


    public Integer getPlayerHandCount() {
        return playerHandCount;
    }

    public void setPlayerHandCount(Integer playerHandCount) {
        this.playerHandCount = playerHandCount;
    }
}
