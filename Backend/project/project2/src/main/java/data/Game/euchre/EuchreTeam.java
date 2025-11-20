package data.Game.euchre;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a team of two players in a Euchre game. Tracks the team's
 * members, score, number of tricks taken in the current round, and other
 * team-specific statuses such as whether the team picked up the card or
 * if a member went alone.
 */
public class EuchreTeam {
    private List<EuchrePlayer> teamMembers;
    private int score;
    private int tricksTaken;
    private boolean teamPickedUpCard;
    private boolean teamMemberWhenAlone;

    /**
     * Creates a basic team
     * @param member1 (player1)
     * @param member2 (player2)
     */
    public EuchreTeam(EuchrePlayer member1, EuchrePlayer member2) {
        teamMembers = new ArrayList<>();
        teamMembers.add(member1);
        teamMembers.add(member2);
        teamPickedUpCard = false;
        teamMemberWhenAlone = false;
        score = 0;
    }

    /**
     * Gets the score
     * @return score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the members of the team
     * @return teamMembers
     */
    public List<EuchrePlayer> getTeamMembers() {
        return teamMembers;
    }

    /**
     * Increases the score by a given amount
     * @param amount (how much to increase score by)
     */
    public void incrementScore(int amount) {
        score += amount;
    }

    /**
     * Sets whether this team picked up the card during bidding.
     *
     * @param toggle true if the team picked up the card, false otherwise
     */
    public void setTeamPickedUpCard(boolean toggle) {
        teamPickedUpCard = toggle;
    }

    /**
     * Sets whether a member of this team went alone.
     *
     * @param toggle true if a member went alone, false otherwise
     */
    public void setTeamMemberWhenAlone(boolean toggle) {
        teamMemberWhenAlone = toggle;
    }

    /**
     * Returns whether this team picked up the card during bidding.
     *
     * @return true if the team picked up the card, false otherwise
     */
    public boolean getTeamPickedUpCard() {
        return teamPickedUpCard;
    }

    /**
     * Returns whether a member of this team went alone.
     *
     * @return true if a member went alone, false otherwise
     */
    public boolean getTeamMemberWentAlone() {
        return teamMemberWhenAlone;
    }

    /**
     * Returns the number of tricks this team has taken in the current round.
     *
     * @return number of tricks taken
     */
    public int getTricksTaken() {
        return tricksTaken;
    }

    /**
     * Increments the count of tricks taken by the team by 1.
     */

    public void incrementTricksTaken() {
        tricksTaken += 1;
    }

    /**
     * Increments the count of tricks taken by the team by 1.
     */

    public void clearTricksTaken() {
        tricksTaken = 0;
    }
}
