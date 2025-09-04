package Experiments.Eli_Herring;

public class Deck {
    
    
    Card[] deck;


    //Creates a deck for a specific game
    //"52" is a standard deck of cards
    //"Euchre" is a deck of cards with fewer cards
    //Maybe change to a switch statement 
    public Deck(String type){
        if (type == "Standard"){

            deck = new Card[52];

            for(int i = 1; i < 14; i++){
                Card temp = new Card(i,"Spades");
                deck[i - 1] = temp;
            }

            for(int i = 1; i < 14; i++){
                Card temp = new Card(i,"Hearts");
                deck[i - 1 + 13] = temp;
            }

            for(int i = 1; i < 14; i++){
                Card temp = new Card(i,"Clubs");
                deck[i - 1 + 26] = temp;
            }

            for(int i = 1; i < 14; i++){
                Card temp = new Card(i,"Diamonds");
                deck[i - 1 + 39] = temp;
            }
        } 

        if(type == "Euchre"){

            //This implementation causes the Ace card to have the value 14
            deck = new Card[24];

            for(int i = 0; i < 6; i++){
                Card temp = new Card(i + 9, "Spades");
                deck[i] = temp;
            }

            for(int i = 6; i < 12; i++){
                Card temp = new Card(i + 9 - 6, "Hearts");
                deck[i] = temp;
            }

            for(int i = 12; i < 18; i++){
                Card temp = new Card(i + 9 - 12, "Clubs");
                deck[i] = temp;
            }

            for(int i = 18; i < 24; i++) {
                Card temp = new Card(i + 9 -18, "Diamonds");
                deck[i] = temp;
            }
        }
        
    }
    
}
