package aircom;

import aircom.model.*;
import aircom.render.GameRender;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.Arrays;

/**
 * Central orchestrator for AirCom.
 * Creates two separate windows — one per player.
 * Each window only shows that player's zone and accepts
 * input only during that player's turn.
 */
public class Game {

    private final GameBoard board;
    private final Player player1;
    private final Player player2;
    private final GameRender renderer1; // Player 1's window
    private final GameRender renderer2; // Player 2's window

    public Game() {
        board = new GameBoard();

        // Each renderer is locked to its player's zone
        renderer1 = new GameRender(board, 1);
        renderer2 = new GameRender(board, 2);

        board.addObserver(renderer1);
        board.addObserver(renderer2);

        // --- Player 1 ships --- rows 0-14
        Ship p1Carrier = new Ship(ShipType.CARRIER,
                Arrays.asList(new GridC(1,2), new GridC(2,2),
                        new GridC(3,2), new GridC(4,2), new GridC(5,2)));
        Ship p1Battleship = new Ship(ShipType.BATTLESHIP,
                Arrays.asList(new GridC(2,10), new GridC(3,10),
                        new GridC(4,10), new GridC(5,10)));
        Ship p1Cruiser = new Ship(ShipType.CRUISER,
                Arrays.asList(new GridC(8,15), new GridC(9,15), new GridC(10,15)));
        Ship p1Destroyer = new Ship(ShipType.DESTROYER,
                Arrays.asList(new GridC(12,6), new GridC(13,6)));

        // --- Player 2 ships --- rows 15-29
        Ship p2Carrier = new Ship(ShipType.CARRIER,
                Arrays.asList(new GridC(16,17), new GridC(17,17),
                        new GridC(18,17), new GridC(19,17), new GridC(20,17)));
        Ship p2Battleship = new Ship(ShipType.BATTLESHIP,
                Arrays.asList(new GridC(18,8), new GridC(19,8),
                        new GridC(20,8), new GridC(21,8)));
        Ship p2Cruiser = new Ship(ShipType.CRUISER,
                Arrays.asList(new GridC(22,3), new GridC(23,3), new GridC(24,3)));
        Ship p2Destroyer = new Ship(ShipType.DESTROYER,
                Arrays.asList(new GridC(26,12), new GridC(27,12)));

        board.placeShip(p1Carrier);
        board.placeShip(p1Battleship);
        board.placeShip(p1Cruiser);
        board.placeShip(p1Destroyer);
        board.placeShip(p2Carrier);
        board.placeShip(p2Battleship);
        board.placeShip(p2Cruiser);
        board.placeShip(p2Destroyer);

        player1 = new Player("Player 1", 1,
                Arrays.asList(p1Carrier, p1Battleship, p1Cruiser, p1Destroyer));
        player2 = new Player("Player 2", 2,
                Arrays.asList(p2Carrier, p2Battleship, p2Cruiser, p2Destroyer));

        renderer1.setFleets(player1.getFleet(), player2.getFleet());
        renderer2.setFleets(player1.getFleet(), player2.getFleet());
    }

    public void start() {
        // Open Player 1's window
        JFrame window1 = new JFrame("AirCom — Player 1");
        window1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window1.add(renderer1);
        window1.pack();
        window1.setLocation(0, 0);
        window1.setVisible(true);

        // Open Player 2's window — offset to the right
        JFrame window2 = new JFrame("AirCom — Player 2");
        window2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window2.add(renderer2);
        window2.pack();
        window2.setLocation(1220, 0);
        window2.setVisible(true);

        // Game loop on background thread
        // Pass BOTH renderers — states pick the right one per turn
        new Thread(() -> {
            State current = new AttackState(player1, player2, board,
                    renderer1, renderer2);
            while (!(current instanceof GameOverState)) {
                current = current.execute();
            }
            System.out.println("Game ended.");
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game().start());
    }
}