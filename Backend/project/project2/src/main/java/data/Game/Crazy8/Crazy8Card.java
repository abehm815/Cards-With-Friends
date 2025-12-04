package data.Game.Crazy8;

import data.Game.MyCard;
/**
 * Represents a single Crazy 8s card.
 * <p>
 * Extends {@link MyCard} and adds an {@code isPlayable} flag used by the
 * Crazy 8s game engine to mark whether a card is currently playable based
 * on game rules (matching value, matching color, or wild 8).
 */
public class Crazy8Card extends MyCard {
    /**
     * Whether this card is currently playable according to game rules.
     */
    public boolean isPlayable;

    /**
     * Creates a new Crazy 8s card with the specified value and color.
     *
     * @param value the card's value (e.g., 8 for wild, 2–14 depending on implementation)
     * @param color the color suit (e.g., 'R', 'G', 'B', 'Y')
     */
    public Crazy8Card(int value, char color) {//R,G,B,Y
        super(value, color);
        isPlayable = false;
    }

    /**
     * Sets whether this card is currently playable.
     *
     * @param isPlayable {@code true} if the card is playable; otherwise {@code false}
     */
    public void setIsPlayable(boolean isPlayable) {
        this.isPlayable = isPlayable;
    }

    /**
     * Returns whether the card is playable.
     *
     * @return {@code true} if the card is playable; otherwise {@code false}
     */
    public boolean getIsSPlayable(){
        return  isPlayable;
    }

}

