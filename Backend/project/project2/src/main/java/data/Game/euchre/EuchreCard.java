package data.Game.euchre;

import data.Game.MyCard;

public class EuchreCard extends MyCard {
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
     * Gets the trump value if it is a jack
     */
    public int getTrumpValue(char trump) {
        char effSuit = getEffectiveSuit(trump);

        if (value == 11 && suit == trump) return 16; // Right bower
        if (value == 11 && effSuit == trump) return 15; // Left bower

        // Standard trump ranking: A > K > Q > 10 > 9
        if (effSuit == trump) return value + 5;

        return value;
    }

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
