package aircom.render;

import aircom.model.GridC;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Renders a parabolic arc for a projectile.
 * Used for attack missiles (orange) and interceptors (red).
 * Same class, different parameters — Strategy pattern.
 *
 * Parametric equations:
 *   x(t) = startX + (endX - startX) * t
 *   y(t) = startY + (endY - startY) * t - arcHeight * 4t(1-t)
 *
 * 4t(1-t) peaks at t=0.5, zero at endpoints — produces the parabolic lift.
 * Height scales with distance so short shots arc low, long shots arc high.
 */
public class ArcRenderer {

    private final int startX, startY, endX, endY;
    private final double arcHeight;
    private final Color color;
    private final float strokeWidth;
    private final double distance;

    private static final double ARC_HEIGHT_FACTOR = 0.3;
    private static final int DOT_RADIUS = 5;

    /**
     * Standard constructor — launch and destination are grid positions.
     */
    public ArcRenderer(IsoRig isoRig, int originX, int originY,
                       GridC launch, GridC target,
                       Color color, float strokeWidth) {
        this.color       = color;
        this.strokeWidth = strokeWidth;
        this.startX = originX + isoRig.pixelX(launch.row(), launch.col());
        this.startY = originY + isoRig.pixelY(launch.row(), launch.col());
        this.endX   = originX + isoRig.pixelX(target.row(), target.col());
        this.endY   = originY + isoRig.pixelY(target.row(), target.col());
        double dx = endX - startX;
        double dy = endY - startY;
        this.distance  = Math.sqrt(dx * dx + dy * dy);
        this.arcHeight = distance * ARC_HEIGHT_FACTOR;
    }

    /**
     * Pixel-destination constructor — destination is raw screen coordinates.
     * Used for successful intercept arc so it meets the attack arc in the air.
     */
    public ArcRenderer(IsoRig isoRig, int originX, int originY,
                       GridC launch, int destX, int destY,
                       Color color, float strokeWidth) {
        this.color       = color;
        this.strokeWidth = strokeWidth;
        this.startX = originX + isoRig.pixelX(launch.row(), launch.col());
        this.startY = originY + isoRig.pixelY(launch.row(), launch.col());
        this.endX   = destX;
        this.endY   = destY;
        double dx = endX - startX;
        double dy = endY - startY;
        this.distance  = Math.sqrt(dx * dx + dy * dy);
        this.arcHeight = distance * ARC_HEIGHT_FACTOR;
    }

    /**
     * Draws trail and dot at progress t.
     */
    public void draw(Graphics2D canvas, double t) {
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setColor(color);
        canvas.setStroke(new BasicStroke(strokeWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        final int TRAIL_STEPS = 30;
        int prevX = startX;
        int prevY = startY;
        for (int step = 1; step <= TRAIL_STEPS; step++) {
            double tStep = (step / (double) TRAIL_STEPS) * t;
            int currX = screenX(tStep);
            int currY = screenY(tStep);
            canvas.drawLine(prevX, prevY, currX, currY);
            prevX = currX;
            prevY = currY;
        }
        int dotX = screenX(t);
        int dotY = screenY(t);
        canvas.fillOval(dotX - DOT_RADIUS, dotY - DOT_RADIUS,
                DOT_RADIUS * 2, DOT_RADIUS * 2);
    }

    /**
     * Finds t value on arc closest to screen position px,py.
     * Used to find where intercept point falls on the attack arc.
     */
    public double findTForPosition(int px, int py) {
        double bestT = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i <= 100; i++) {
            double t = i / 100.0;
            double dx = screenX(t) - px;
            double dy = screenY(t) - py;
            double dist = dx * dx + dy * dy;
            if (dist < bestDist) {
                bestDist = dist;
                bestT = t;
            }
        }
        return bestT;
    }

    public int screenX(double t) {
        return (int)(startX + (endX - startX) * t);
    }

    public int screenY(double t) {
        double linear = startY + (endY - startY) * t;
        double lift   = arcHeight * 4 * t * (1 - t);
        return (int)(linear - lift);
    }

    public double getDistance() { return distance; }

    public int[] getEndScreenPosition() {
        return new int[]{endX, endY};
    }

    public int[] getPeakScreenPosition() {
        return new int[]{ screenX(0.5), screenY(0.5) };
    }
}