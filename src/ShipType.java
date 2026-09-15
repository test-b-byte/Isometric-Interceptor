package aircom.model;
/** ships in this are a bit more then standard battelship. Because weve added a interceptor and launch contraint
 * ships anchor more than targets
 * 1 Occupy a gird space (obvious)
 * 2 Launch locations. Attacks must begin from a location
 * ... that might b it for now.
 * 3. I think I'll have automatic launch point of a ship be indexed and so long as any
 * part exists, the launch point is valid. so it need to track damage and elimination*/


// two paramters, number of tiles and an extra parameter int for something later
public enum ShipType {

    CARRIER(5, 2),
    BATTLESHIP(4, 2),
    CRUISER(3, 1),
    DESTROYER(2, 1);

    private final int length;

    ShipType(int length, int launchSpeed) {
        this.length = length;
    }

    public int getLength() {
        return length;}
    }