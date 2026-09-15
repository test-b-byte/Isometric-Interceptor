package aircom.model;

/** We will use and Observor behavior design pattern. after some brainstormign I think this is the most
 * effective way to build the functionality. Becaue two players have distinct limits in access to the game board,
 * including where they can place things, Observor gets to be master of the whole game, seing and adjudicating
 * the entire situation and passing information to the players.
 *
 */

public interface GameObserver {
    void onTurnImpact(GridC target, boolean intercepted, boolean miss, Ship shipSunk, boolean gameOver, String winnerName);

    void onGameOver(String winnerName);
    }
