package data.Game.BlackJack;

import data.Game.MyCard;

public class BlackJackCard extends MyCard {
    public boolean isShowing;
    /**
     * Constructor for a card
     *
     * @param value
     * @param suit
     */
    public BlackJackCard(int value, char suit) {
        super(value, suit);
        isShowing = false;
    }

    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }
}
