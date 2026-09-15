package aircom.model;

import java.util.List;


/** This class is relatively small, jsut used to distinguish permissions
/ and ownership . status of ships on the board
/ Organizes board  logic and player status
/ One reason why I decided to keep the print statements even thoough they dont talk to the HUD
 / is they helped alot with debugging since the give context updates in the terminal
*/


public class Player implements GameObserver {
    private final String name;
    private final int zone;
    private final List<Ship> fleet;

    public Player(String name, int zone, List<Ship> fleet) {
        this.name = name;
        this.zone = zone;
        this.fleet = fleet;
    }

    public List<Ship> getFleet() {return fleet;}

    @Override
    public void onTurnImpact(GridC target, boolean intercepted, boolean miss,
                             Ship shipSunk, boolean gameOver, String winnerName) {
        System.out.println("After Action report: ");
        System.out.println("  target =" + target);
        if (intercepted) System.out.println("Attack intercepted!");
        if (miss)        System.out.println("Attack missed");
        if (shipSunk != null) System.out.println("ship sunk: " + shipSunk.getType());
        if (gameOver)    System.out.println("Game over. Winner: " + winnerName);
    }

    @Override
    public void onGameOver(String winnerName) {
        System.out.println("Game over. Winner: " + winnerName);
    }

    public String getName() { return name; }
    public int getZone() { return zone; }

}

