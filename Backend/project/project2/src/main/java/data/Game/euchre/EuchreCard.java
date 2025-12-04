package data.Game.euchre;

import com.fasterxml.jackson.annotation.JsonIgnore;
import data.Game.MyCard;

/**
 * Represents a Euchre-specific card with an assigned owner and
 * Euchre-based ranking rules, including trump and bower logic.
 */
public class EuchreCard extends MyCard {
    @JsonIgnore
    private EuchrePlayer owner;

    /**
     * Constructor for a card
     *
     * @param value value of card
     * @param suit suit of card
     * @param owner owner of card
     */
    public EuchreCard(int value, char suit, EuchrePlayer owner) {
        super(value, suit);
        this.owner = owner;
    }

    /**
     * Gets the owner of the card
     * @return owner
     */
    public EuchrePlayer getOwner() {
        if (owner == null) {
            System.out.println("The " + this.toString() + " has no owner!");
            return null;
        }
        return owner;
    }

    /**
     * Changes the owner of the card
     * @param player new owner
     */
    public void setOwner(EuchrePlayer player) {
        owner = player;
    }

    /**
     * Computes the Euchre trump ranking value for this card
     * based on the currently selected trump suit.
     *
     * <p>Special rules:</p>
     * <ul>
     *   <li>Right bower (Jack of trump suit) gets the highest rank</li>
     *   <li>Left bower (Jack of same-color suit) gets the second-highest rank</li>
     *   <li>Other trump cards receive boosted ranking</li>
     * </ul>
     *
     * @param trump the suit selected as trump
     * @return modified value reflecting Euchre trump priority
     */
    public int getTrumpValue(char trump) {
        char effSuit = getEffectiveSuit(trump);

        if (value == 11 && suit == trump) return 16; // Right bower
        if (value == 11 && effSuit == trump) return 15; // Left bower

        // Standard trump ranking: A > K > Q > 10 > 9
        if (effSuit == trump) return value + 5;

        return value;
    }

    /**
     * Determines the effective suit of this card in Euchre.
     * The left bower (Jack of the same-color suit) counts as trump.
     *
     * @param trumpSuit the suit chosen as trump
     * @return the effective suit for this card, considering bower rules
     */
    public char getEffectiveSuit(char trumpSuit) {
        // Right bower: Jack of trump
        if (value == 11 && suit == trumpSuit) {
            return trumpSuit;
        }

        // Left bower: Jack of same-color suit
        if (value == 11) {
            if (trumpSuit == 'h' && suit == 'd') return 'h';
            if (trumpSuit == 'd' && suit == 'h') return 'd';
            if (trumpSuit == 'c' && suit == 's') return 'c';
            if (trumpSuit == 's' && suit == 'c') return 's';
        }

        return suit;
    }
}
