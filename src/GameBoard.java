package aircom.model;
import aircom.model.Ship;
import aircom.model.Cell;
import aircom.model.GridC;

//The purpose of Board class is to inform the GameRender what to paint
//The central digital twin holdong the basic parameters for our Jpanel

public class GameBoard {

    public static final int ROWS = 30;
    public static final int COLS = 20;
    //array of an array, ros, and colums
    private final Cell[][] cells;

    private final java.util.List<BoardObserver> observers = new java.util.ArrayList<>();

    public void addObserver(BoardObserver observer) {
        observers.add(observer);
    }

    public void notifyBoardChanged() {
        for (BoardObserver observer : observers) {
            observer.onBoardChanged();
        }
    }

    public GameBoard() {
        cells = new Cell[ROWS][COLS];

        for (int row = 0; row < ROWS; row++) {
            for (int col =0; col < COLS; col++) {
                cells[row][col] = new Cell(new GridC(row, col));
            }
        }
    }
    public Cell getCell(GridC position) {
        return cells[position.row()][position.col()];
    }

    //Places Ship in grid, update tiles to hold ship
    public void placeShip(Ship ship) {
        for (GridC position : ship.getCoordinates()) {
            getCell(position).setOccupant(ship);
        }
    }
}
