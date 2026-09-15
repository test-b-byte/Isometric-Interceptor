package aircom.model;
import java.util.ArrayList;
import java.util.List;


// For this problem Cluade recommended a Bresenham's Line Algorithm.
//Using simple integers it helps to decide whihc squares best represent the "path lcoation"
//Since grids generally take up broader then real space.
/** this was also cool for me becuase i got to learn a enw math algo!
 * so we take our col/rows and figure out distance for each (0,0) _> 5,4. xDelta is 5, YD is4
 * Because XD > YD, x becomes the "driving axis"
 *1. Move one step along the driving axis
 * 2. Add the other axis distance to the bucket
 * 3. Is bucket >= driving axis distance?
 *    YES → step along the other axis
 *          subtract driving axis distance from bucket
 *    NO  → do nothing
 * 4. Record current position
 * 5. Repeat until you reach the target
 * */


public class AttackCalculator {

    public static AttackPath calculate(aircom.model.GridC launch, aircom.model.GridC target) {

        List<aircom.model.GridC> path = new ArrayList<>();

        int row = launch.row();
        int col = launch.col();

        //you need a x and y distance. End position - starting position
        int rowDistance = Math.abs(target.row() - launch.row());
        int colDistance = Math.abs(target.col() - launch.col());

        //need to orient if we're moving up or down, so a simple if-else check to assing a movement
        //positive or negative valence

        int rowStep = (target.row() > launch.row()) ? 1 : -1;
        int colStep = (target.col() > launch.col()) ? 1 : -1;

        //over flow for non-axis check on Bresenhams Algo
        int bucket = 0;

        // find driving axis and iterations
        boolean rowDrives = rowDistance >= colDistance;
        int driveCount = rowDrives ? rowDistance : colDistance;
        int bucketCount = rowDrives ? colDistance : rowDistance;

        //start point
        path.add(new GridC(row, col));
        //launch to target loop
        for (int step = 0; step < driveCount; step++) {

            if (rowDrives) {
                row += rowStep;
            } else {
                col += colStep;
            }

            bucket += bucketCount;

            if (bucket >= driveCount) {
                if (rowDrives) {
                    col += colStep;
                } else {
                    row += rowStep;
                }
                bucket -= driveCount;
            }

            path.add(new GridC(row, col));
        }
        return new AttackPath(path);
    }
}

