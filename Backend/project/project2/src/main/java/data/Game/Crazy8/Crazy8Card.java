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

    @Override
    public String toString() {
        String colorName;
        switch (this.getSuit()) {
            case 'R': colorName = "Red"; break;
            case 'G': colorName = "Green"; break;
            case 'B': colorName = "Blue"; break;
            case 'Y': colorName = "Yellow"; break;
            default: colorName = "Unknown";
        }

        String valueName;
        switch (this.getValue()) {
            case 8: valueName = "Wild"; break;
            case 11: valueName = "Reverse"; break;
            case 12: valueName = "Skip"; break;
            case 13: valueName = "Draw 2"; break;
            case 14: valueName = "Wild Draw 4"; break;
            default: valueName = String.valueOf(this.getValue());
        }

        return colorName + " " + valueName;
    }
}

