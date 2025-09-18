package Backend.User;

/**
 * This class describes a card object. Each card has a value and a suit
 */
public class MyCard {
    /**
     * The value of the card, 2 = 2, 3 = 3 etc,
     * 11 = Jack, 12 = Queen, 13 = King, 14 = Ace
     */
    public int value;

    /**
     * This is the suit of the card
     * h = heart, d = diamond, s = spade, c = club
     */
    public char suit;

    /**
     * Constructor for a card
     * @param value
     * @param suit
     */
    public MyCard(int value, char suit) {
        this.value = value;
        this.suit = suit;
    }

    /**
     * Returns the value of the card
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value of the card
     * @param value
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Gets the suit of the card
     * @return
     */
    public char getSuit() {
        return suit;
    }

    /**
     * Sets the suit of the card
     * @param suit
     */
    public void setSuit(char suit) {
        this.suit = suit;
    }

    @Override
    public String toString() {
        String cardValue = faceCardConverter(getValue());
        String cardSuit = suitConverter(getSuit());
        return faceCardConverter(getValue()) + " of " + suitConverter(getSuit());
    }

    public String suitConverter(char cardSuit) {
        if (cardSuit == 'h') {
            return "hearts";
        } else if (cardSuit == 'd') {
            return "diamonds";
        } else if (cardSuit == 's') {
            return "spades";
        } else if (cardSuit == 'c') {
            return "clubs";
        } else {
            return "UNRECOGNIZED SUIT";
        }
    }

    /**
     * Converts the int values into Strings for better readability
     * @param value
     * @return
     */
    public String faceCardConverter(int value) {
        String returnValue;
        if(value == 11) {
            returnValue = "Jack";
            return returnValue;
        } else if (value == 12) {
            returnValue = "Queen";
            return returnValue;
        } else if (value == 13) {
            returnValue = "King";
            return returnValue;
        } else if (value == 14) {
            returnValue = "Ace";
            return returnValue;
        } else {
            returnValue = Integer.toString(getValue());
            return returnValue;
        }
    }
}
