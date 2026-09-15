package aircom.render;

import aircom.model.GridC;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A single parabolic arc rendered from two endpoints simultaneously,
 * meeting at a computed point along the arc. Used for intercept resolution.
 *
 * One arc defined from attackLaunch to interceptLaunch.
 * Attack dot travels forward from t=0.
 * Intercept dot travels backward from t=1.0.
 * Both arrive at meetingT simultaneously via delay-based sync.
 * Explosion fires at meetingT screen position.
 *
 * meetingT is computed from the proportional distance of interceptPoint
 * along the arc — wherever it naturally falls, not forced to peak.
 */
public class InterceptArc {

    private final int startX, startY; // attack launch screen position
    private final int endX, endY;     // intercept launch screen position
    private final double arcHeight;
    private final double meetingT;    // t where both dots converge
    private final double distance;

    private static final double ARC_HEIGHT_FACTOR = 0.3;
    private static final int DOT_RADIUS = 5;

    private static final Color ATTACK_COLOR    = new Color(255, 165, 0);  // orange
    private static final Color INTERCEPT_COLOR = new Color(220, 50, 50);  // red

    /**
     * @param isoRig         grid-to-pixel converter
     * @param originX        board origin x
     * @param originY        board origin y
     * @param attackLaunch   where the attack missile launched from
     * @param interceptLaunch where the interceptor launched from
     * @param interceptPoint  the grid coordinate where interception occurs
     */
    public InterceptArc(IsoRig isoRig, int originX, int originY,
                        GridC attackLaunch, GridC interceptLaunch,
                        GridC interceptPoint) {

        this.startX = originX + isoRig.pixelX(attackLaunch.row(), attackLaunch.col());
        this.startY = originY + isoRig.pixelY(attackLaunch.row(), attackLaunch.col());
        this.endX   = originX + isoRig.pixelX(interceptLaunch.row(), interceptLaunch.col());
        this.endY   = originY + isoRig.pixelY(interceptLaunch.row(), interceptLaunch.col());

        double dx = endX - startX;
        double dy = endY - startY;
        this.distance  = Math.sqrt(dx * dx + dy * dy);
        this.arcHeight = distance * ARC_HEIGHT_FACTOR;

        // meetingT = proportional distance of interceptPoint from attackLaunch
        // along the straight line between the two launch points
        int ipX = originX + isoRig.pixelX(interceptPoint.row(), interceptPoint.col());
        int ipY = originY + isoRig.pixelY(interceptPoint.row(), interceptPoint.col());
        double dxIP = ipX - startX;
        double dyIP = ipY - startY;
        double ipDist = Math.sqrt(dxIP * dxIP + dyIP * dyIP);
        this.meetingT = ipDist / distance;
    }

    /**
     * Draws both dots traveling toward meetingT.
     * attackT  — how far the attack dot has traveled from t=0
     * interceptT — how far the intercept dot has traveled from t=1.0
     *
     * @param canvas    Graphics2D paintbrush
     * @param attackT   progress of attack dot from 0 toward meetingT
     * @param interceptT progress of intercept dot from 1.0 toward meetingT
     */
    public void draw(Graphics2D canvas, double attackT, double interceptT) {
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw attack trail — orange, from start to attackT
        canvas.setColor(ATTACK_COLOR);
        canvas.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        drawTrail(canvas, 0.0, attackT);

        // Draw intercept trail — red, from end back to interceptT
        canvas.setColor(INTERCEPT_COLOR);
        canvas.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        drawTrail(canvas, 1.0, interceptT);

        // Draw attack dot at current position
        canvas.setColor(ATTACK_COLOR);
        drawDot(canvas, attackT);

        // Draw intercept dot at current position
        canvas.setColor(INTERCEPT_COLOR);
        drawDot(canvas, interceptT);
    }

    private void drawTrail(Graphics2D canvas, double fromT, double toT) {
        final int STEPS = 30;
        double tStart = Math.min(fromT, toT);
        double tEnd   = Math.max(fromT, toT);
        int prevX = screenX(tStart);
        int prevY = screenY(tStart);
        for (int i = 1; i <= STEPS; i++) {
            double t = tStart + (tEnd - tStart) * (i / (double) STEPS);
            int cx = screenX(t);
            int cy = screenY(t);
            canvas.drawLine(prevX, prevY, cx, cy);
            prevX = cx;
            prevY = cy;
        }
    }

    private void drawDot(Graphics2D canvas, double t) {
        int x = screenX(t);
        int y = screenY(t);
        canvas.fillOval(x - DOT_RADIUS, y - DOT_RADIUS,
                DOT_RADIUS * 2, DOT_RADIUS * 2);
    }

    public int screenX(double t) {
        return (int)(startX + (endX - startX) * t);
    }

    public int screenY(double t) {
        double linear = startY + (endY - startY) * t;
        double lift   = arcHeight * 4 * t * (1 - t);
        return (int)(linear - lift);
    }

    /** @return t value where both dots meet */
    public double getMeetingT() { return meetingT; }

    /** @return screen position at meetingT — where explosion fires */
    public int[] getMeetingScreenPosition() {
        return new int[]{ screenX(meetingT), screenY(meetingT) };
    }

    /** @return total arc distance for delay sync math */
    public double getDistance() { return distance; }

    /** @return distance from attack launch to meeting point */
    public double getAttackSegmentDistance() {
        return distance * meetingT;
    }

    /** @return distance from intercept launch to meeting point */
    public double getInterceptSegmentDistance() {
        return distance * (1.0 - meetingT);
    }
}