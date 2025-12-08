package data.Game.BlackJack.history;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blackjack_match_event")
public class BlackJackMatchEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "match_history_id")
    private BlackJackMatchHistoryEntity matchHistory;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Player performing the action ("dealer" or username)
    private String player;

    // e.g. HIT, STAND, DEAL_INITIAL, DOUBLE, SPLIT, DEALER_HIT, ROUND_END
    private String action;

    // Card involved in the action (if any)
    private String card;

    // For split hands (0 = primary, 1 = split)
    private Integer handIndex;

    // Hand values before/after action
    private Integer handValueBefore;
    private Integer handValueAfter;

    // Betting details (optional depending on action)
    private Integer betAmount;
    private Integer payoutAmount;

    // Dealer-specific info
    private String dealerUpCard;
    private Integer dealerFinalValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BlackJackMatchHistoryEntity getMatchHistory() {
        return matchHistory;
    }

    public void setMatchHistory(BlackJackMatchHistoryEntity matchHistory) {
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

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public Integer getHandIndex() {
        return handIndex;
    }

    public void setHandIndex(Integer handIndex) {
        this.handIndex = handIndex;
    }

    public Integer getHandValueBefore() {
        return handValueBefore;
    }

    public void setHandValueBefore(Integer handValueBefore) {
        this.handValueBefore = handValueBefore;
    }

    public Integer getHandValueAfter() {
        return handValueAfter;
    }

    public void setHandValueAfter(Integer handValueAfter) {
        this.handValueAfter = handValueAfter;
    }

    public Integer getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(Integer betAmount) {
        this.betAmount = betAmount;
    }

    public Integer getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(Integer payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public String getDealerUpCard() {
        return dealerUpCard;
    }

    public void setDealerUpCard(String dealerUpCard) {
        this.dealerUpCard = dealerUpCard;
    }

    public Integer getDealerFinalValue() {
        return dealerFinalValue;
    }

    public void setDealerFinalValue(Integer dealerFinalValue) {
        this.dealerFinalValue = dealerFinalValue;
    }
}
