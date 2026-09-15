import aircom.model.AttackCalculator;
import aircom.model.AttackPath;
import aircom.model.GridC;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Currently writing tests for the calculator because thats the one with the most functioning action,
 * its recieves inputs and outputs and the whole fidelity of the project (other then rendering kind of come down to this..
 * Im not genuinely sure what else to test yet since I have made the game fucnitonal
 */

public class AttackCalculatorTest {

    /**
     * Straight horizontal path /same row, different cols.
     * From (0,0) to (0,4) should produce 5 tiles all on row 0.
     */
    @Test
    public void testHorizontalPath() {
        GridC launch = new GridC(0, 0);
        GridC target = new GridC(0, 4);
        AttackPath path = AttackCalculator.calculate(launch, target);

        // Path should contain 5 tiles — (0,0),(0,1),(0,2),(0,3),(0,4)
        assertEquals(5, path.getPath().size());

        // Start and end points must be on the path
        assertTrue(path.contains(new GridC(0, 0)));
        assertTrue(path.contains(new GridC(0, 4)));
    }
// Testing vertical driven
    @Test
    public void testVerticalPath() {
        GridC launch = new GridC(0, 0);
        GridC target = new GridC(4, 0);
        AttackPath path = AttackCalculator.calculate(launch, target);

        assertEquals(5, path.getPath().size());
        assertTrue(path.contains(new GridC(0, 0)));
        assertTrue(path.contains(new GridC(4, 0)));
    }

    //testign more complicated trajectories to make sure we didnt jsut get lucky before.
    @Test
    public void testDiagonalPath() {
        GridC launch = new GridC(0, 0);
        GridC target = new GridC(4, 3);
        AttackPath path = AttackCalculator.calculate(launch, target);

        // Should contain 5 tiles
        assertEquals(5, path.getPath().size());

        assertTrue(path.contains(new GridC(0, 0)));
        assertTrue(path.contains(new GridC(2, 1)));
        assertTrue(path.contains(new GridC(4, 3)));
    }

    //Interceptor Check. Once flight path is calcuated, can the game recognize a defender blocking it:

    @Test
    public void testContains() {
        GridC launch = new GridC(0, 0);
        GridC target = new GridC(0, 4);
        AttackPath path = AttackCalculator.calculate(launch, target);


        assertTrue(path.contains(new GridC(0, 2)));
        assertFalse(path.contains(new GridC(5, 5)));
    }
}