
package aircom.model;

import java.util.List;
/** ships in this are a bit more then standard battelship. Because weve added a interceptor and launch contraint
 * ships track 3 primary things
 * 1: Type
 * 2: Hit on body
 * 3: location of the ship segments (GCoords)
 * part exists, the launch point is valid. so it need to track damage and elimination*/



public class Ship {
    //type
    private final aircom.model.ShipType type;
    //grid pairing
    private final List<aircom.model.GridC> coordinates;
    //damage area tracking with array, so it can still launch or check launch
    private final boolean[] hitSegments;

    public Ship(aircom.model.ShipType type, List<aircom.model.GridC> coordinates) {
        this.type = type;
        this.coordinates = coordinates;
        this.hitSegments = new boolean[coordinates.size()];
    }
// SO because Im trying to create a somewhat realistic but also contrained system that forces player strategy,
    // the launch "point" is both specifically tide to a ship location so it needs a ghrid, but it also
    //n eed to update on which part of the ship if theres damage

    public aircom.model.GridC getLaunchPoint() {
        for (int i = 0; i < coordinates.size(); i++) {
            if (!hitSegments[i]) {
                return coordinates.get(i);
            }
        }
        return null;
    }

    //putting damage coutners on ships
    public void hit(aircom.model.GridC position) {
        for (int i = 0; i < coordinates.size(); i++) {
            if (coordinates.get(i).equals(position)) {
                hitSegments[i] = true;
                return;
            }
        }
    }

    //ship destroyed check
    public boolean isSunk () {
        for(boolean confirmedHit : hitSegments) {
            if(!confirmedHit) {
                return false;
            }
        }
        return true;
    }

    public aircom.model.ShipType getType() {
        return type;}

    public List<aircom.model.GridC> getCoordinates() {
        return coordinates;
    }
}
