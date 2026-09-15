import aircom.model.GridC;
import aircom.model.Ship;
import aircom.model.ShipType;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Tests for thw way ships control game logic and update tiles*/
public class ShipTest {

    /**
     * A fresh ship should not be sunk and should have a valid launch point.
     * hitting all segments should sink the ship.
     */
    @Test
    public void testShipSinksWhenAllSegmentsHit() {
        Ship ship = new Ship(ShipType.DESTROYER,
                Arrays.asList(new GridC(2, 5), new GridC(3, 5)));
        ship.hit(new GridC(2, 5));
        ship.hit(new GridC(3, 5));
        assertTrue(ship.isSunk());
    }


     // Ships are actually designed to lose launch point options as they take damage, so
    //this should verify the baord updates.

    @Test
    public void testLaunchPointAdvancesOnHit() {
        Ship ship = new Ship(ShipType.DESTROYER,
                Arrays.asList(new GridC(2, 5), new GridC(3, 5)));
        assertEquals(new GridC(2, 5), ship.getLaunchPoint());
        ship.hit(new GridC(2, 5));
        assertEquals(new GridC(3, 5), ship.getLaunchPoint());
    }


     //A fully destroyed ship should return null for launch point.
    @Test
    public void testLaunchPointNullWhenSunk() {
        Ship ship = new Ship(ShipType.DESTROYER,
                Arrays.asList(new GridC(2, 5), new GridC(3, 5)));
        ship.hit(new GridC(2, 5));
        ship.hit(new GridC(3, 5));
        assertNull(ship.getLaunchPoint());
    }
}