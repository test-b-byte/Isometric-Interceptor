package aircom.model;
/**
 * Encapsulation
 * Missile is just a packet that takes the various information in attakc and interceottion and encapsulates it so
 * it can be passed around.
 * As I get to the renderer Ill probably also connect it there so it can render an animation???
 *
 */


public class Missile {

    //grid coord for missile on a attack
    private final GridC launchPoint;
    private final GridC target;
    private final AttackPath path;
// all in all prtty self explanitory. You have the launch point, the target gird, and calculator builds the path
    /**
     * @param launchPoint where the missile launched from
     * @param target      where the missile is aimed
     * @param path        the computed flight path between them
     */
    public Missile(GridC launchPoint, GridC target, AttackPath path) {
        this.launchPoint = launchPoint;
        this.target = target;
        this.path = path;
    }

    public GridC getLaunchPoint() { return launchPoint; }
    public GridC getTarget() { return target; }
    public AttackPath getPath() { return path; }
}