package coms309;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Deal test application
 * 
 * @author Jake Breyfogle
 */

@SpringBootApplication
public class Application {
	
    public static void main(String[] args) throws Exception {
        MyDeck testDeck = new MyDeck();
        // testDeck.shuffle();
        Player player1 = new Player("Jake");
        for(int i = 0; i < 5; i++) {
            player1.addCard(testDeck.dealCard());
        }
        player1.showHand();
        System.out.println();
        System.out.println(player1.hasCard(2));
    }

}
