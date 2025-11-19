package data.Game.Crazy8;

import data.Game.MyCard;

public class Crazy8Card extends MyCard {
    public boolean isPlayable;
    /**
     * Constructor for a card
     *
     * @param value
     * @param color
     */
    public Crazy8Card(int value, char color) {//R,G,B,Y
        super(value, color);
        isPlayable = false;
    }

    public void setIsPlayable(boolean isPlayable) {
        this.isPlayable = isPlayable;
    }

    public boolean getIsSPlayable(){
        return  isPlayable;
    }
}

