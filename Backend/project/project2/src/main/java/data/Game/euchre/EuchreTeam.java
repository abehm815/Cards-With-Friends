package data.Game.euchre;

import java.util.ArrayList;
import java.util.List;

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

    public void setTeamPickedUpCard(boolean toggle) {
        teamPickedUpCard = toggle;
    }

    public void setTeamMemberWhenAlone(boolean toggle) {
        teamMemberWhenAlone = toggle;
    }

    public boolean getTeamPickedUpCard() {
        return teamPickedUpCard;
    }

    public boolean getTeamMemberWentAlone() {
        return teamMemberWhenAlone;
    }

    public int getTricksTaken() {
        return tricksTaken;
    }

    public void incrementTricksTaken() {
        tricksTaken += 1;
    }

    public void clearTricksTaken() {
        tricksTaken = 0;
    }
}
