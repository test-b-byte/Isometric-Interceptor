package aircom.model;
import aircom.model.GridC;
import aircom.model.Ship;
/**the board is image, a vis interface. mapped over it are GrdC, or matched to it are the (data of relative positions).
 *  These coordinates and other factors, (object, etc) need to be unified in a container.. this is Cell
 *  Basically any key information that is tied to the GridC, such as statuses or relative position is held here
*/


public class Cell {
    public enum CellState {
        EMPTY, OCCUPIED, HIT, MISS}

    private final GridC position;

    private CellState state;
    public void setState(CellState state) {
        this.state = state;
    }

    private Ship occupant;

    //All grids get empty cells for starters.
    public Cell(GridC position) {
        this.position = position;
        this.state = CellState.EMPTY;
        this.occupant = null;
    }

    public GridC getPosition() {
        return position;
    }

    public CellState getState() {
        return state;
    }

    public void setOccupant(Ship ship) {
        this.occupant = ship;
        this.state = CellState.OCCUPIED;
    }

    public Ship getOccupant() {
        return occupant;
    }
}
