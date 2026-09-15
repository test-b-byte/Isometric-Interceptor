import aircom.model.AttackPath;
import aircom.model.GridC;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 *Verifies tha interceptor  mechanic
 * The intercept mechanic depends entirely on contains() working correctly,
 * which relies on GridC value equality (record).
 */
public class AttackPathTest {

    /**
     * A coordinate on the path should be found.
     */
    @Test
    public void testContainsKnownTile() {
        AttackPath path = new AttackPath(Arrays.asList(
                new GridC(0, 0), new GridC(1, 1), new GridC(2, 2)));
        assertTrue(path.contains(new GridC(1, 1)));
    }

    /**
     * A coordinate not on the path should not be found.
     *
     */
    @Test
    public void testDoesNotContainOffPathTile() {
        AttackPath path = new AttackPath(Arrays.asList(
                new GridC(0, 0), new GridC(1, 1), new GridC(2, 2)));
        assertFalse(path.contains(new GridC(5, 5)));
    }
}