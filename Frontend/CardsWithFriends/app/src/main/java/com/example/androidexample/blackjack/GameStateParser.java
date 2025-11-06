package com.example.androidexample.blackjack;

import com.example.androidexample.blackjack.BlackjackModels.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Converts raw WebSocket JSON messages into structured {@link GameState} objects.
 *
 * <p>This class is responsible for taking the server-sent JSON game snapshot
 * and converting it into the hierarchy of model classes used by the client:
 * {@link GameState}, {@link DealerState}, {@link PlayerState}, {@link HandState}, and {@link CardState}.</p>
 *
 */
public class GameStateParser {

    /**
     * Parses a JSONObject into a fully populated {@link GameState} instance.
     *
     * @param json A JSON object representing the full game state sent from the WebSocket.
     * @return A structured {@link GameState} containing all dealer, player, and card data.
     * @throws JSONException If a malformed JSON structure is encountered.
     */
    public static GameState fromJson(JSONObject json) throws JSONException {
        // Create a new empty GameState object
        GameState state = new GameState();

        // Basic game-level fields (lobby code, round state, current turn)
        state.lobbyCode = json.optString("lobbyCode");
        state.roundInProgress = json.optBoolean("roundInProgress");
        state.currentTurn = json.optString("currentTurn");

        // ==========================================================
        // Parse Dealer Information
        // ==========================================================
        JSONObject dealerObj = json.optJSONObject("dealer");
        if (dealerObj != null) {
            DealerState dealer = new DealerState();
            dealer.handValue = dealerObj.optInt("handValue");

            // Parse dealer's cards (if present)
            JSONArray cards = dealerObj.optJSONArray("hand");
            if (cards != null) {
                for (int i = 0; i < cards.length(); i++) {
                    JSONObject c = cards.getJSONObject(i);

                    // Construct each card from JSON
                    CardState card = new CardState();
                    card.value = c.optInt("value");
                    card.suit = c.optString("suit");
                    card.isShowing = c.optBoolean("isShowing");

                    // Add card to dealer hand list
                    dealer.hand.add(card);
                }
            }
            // Assign parsed dealer to overall state
            state.dealer = dealer;
        }

        // ==========================================================
        // Parse Player Information
        // ==========================================================
        JSONArray playersArray = json.optJSONArray("players");
        if (playersArray != null) {
            // Loop through all player entries in the JSON
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject p = playersArray.getJSONObject(i);

                PlayerState ps = new PlayerState();
                ps.username = p.optString("username");
                ps.chips = p.optInt("chips");
                ps.hasBet = p.optBoolean("hasBet");

                // Parse each player's hand list
                JSONArray hands = p.optJSONArray("hands");
                if (hands != null) {
                    for (int j = 0; j < hands.length(); j++) {
                        JSONObject h = hands.getJSONObject(j);

                        HandState hs = new HandState();
                        hs.handIndex = h.optInt("handIndex");
                        hs.handValue = h.optInt("handValue");
                        hs.bet = h.optInt("bet");
                        hs.hasStood = h.optBoolean("hasStood");
                        hs.canSplit = h.optBoolean("canSplit");

                        // Parse all cards within this hand
                        JSONArray cards = h.optJSONArray("hand");
                        if (cards != null) {
                            for (int k = 0; k < cards.length(); k++) {
                                JSONObject c = cards.getJSONObject(k);

                                CardState card = new CardState();
                                card.value = c.optInt("value");
                                card.suit = c.optString("suit");
                                card.isShowing = c.optBoolean("isShowing");

                                // Add card to the current hand
                                hs.hand.add(card);
                            }
                        }

                        // Add the completed hand to the player's hand list
                        ps.hands.add(hs);
                    }
                }

                // Add the player (with all parsed hands) to the game state
                state.players.add(ps);
            }
        }

        // Return the fully built game state
        return state;
    }
}
