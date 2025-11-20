package data.Game.BlackJack;

import data.Game.MyCard;

public class BlackJackCard extends MyCard {
    public boolean isShowing;
    /**
     * Constructor for a card
     *
     * @param value value of card
     * @param suit suit of card
     */
    public BlackJackCard(int value, char suit) {
        super(value, suit);
        isShowing = false;
    }

    /**
     *
     * sets boolean is showing
     * @param isShowing if the cards value is showing
     */
    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    public boolean getIsShowing(){
        return isShowing;
    }


}
