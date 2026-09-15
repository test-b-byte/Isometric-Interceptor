package aircom.render;
import java.util.Random;

import java.awt.Color;

/**
 * Animated water texture for the isometric board.
 * Uses a sine wave phase offset advancing each tick to simulate
 * gentle ocean swells rolling across the surface.
 *
 * Pre-computes a Color[][] cache each tick so drawTile does O(1)
 * cache lookup rather than computing sine per tile per frame.
 * tick() runs once per frame, concentrating all sine math there.
 */
public class WaterTexture {

    private final int rows;
    private final int cols;

    // Pre-computed color cache — updated once per tick, read per tile
    private final Color[][] cache;
    // Fixed per-tile random offset, generated once — this is what gives
    // each tile a slightly different shade even at rest, before any wave math runs
    private final double[][] speckle;

    // Max color shift from the per-tile speckle, applied on top of the wave
    private static final int SPECKLE_AMPLITUDE = 12;

    // Wave parameters
    private double phase = 0.0;

    // How tightly packed the waves are — lower = broader swells
    private static final double FREQ = 0.4;

    // Max brightness shift from sine — keep small for subtle effect
    private static final int AMPLITUDE = 18;

    // How fast the phase advances each tick — controls wave speed
    private static final double PHASE_STEP = 0.6;

    public WaterTexture(int rows, int cols) {
        this.rows  = rows;
        this.cols  = cols;
        this.cache = new Color[rows][cols];

        // Build the fixed per-tile speckle grid ONCE — this never changes again,
        // which is what makes it a static texture instead of more animation.
        this.speckle = new double[rows][cols];
        Random rng = new Random();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // nextInt(21) gives 0-20; subtract 10 to center on 0 -> range -10..10
                // dividing by 10.0 turns that into -1.0..1.0 in steps of 0.1
                int steppedValue = rng.nextInt(21) - 10;
                speckle[row][col] = steppedValue / 10.0;
            }
        }

        // Initialize cache so first frame has valid colors
        updateCache(null, null);
    }

    /**
     * Advances the wave phase and rebuilds the color cache.
     * Called once per animation frame by GameRender's water timer.
     * All sine computation happens here — drawTile just reads cache.
     *
     * @param p1Base base ocean color for Player 1 zone
     * @param p2Base base ocean color for Player 2 zone
     */
    public void tick(Color p1Base, Color p2Base) {
        phase += PHASE_STEP;
        if (phase > Math.PI * 2) phase -= Math.PI * 2; // keep phase bounded
        updateCache(p1Base, p2Base);
    }

    private void updateCache(Color p1Base, Color p2Base) {
        // Default colors if called before game sets them
        Color base1 = p1Base != null ? p1Base : new Color(42, 90, 160);
        Color base2 = p2Base != null ? p2Base : new Color(42, 128, 96);
        int split = rows / 2;

        for (int row = 0; row < rows; row++) {
            Color base = row < split ? base1 : base2;
            for (int col = 0; col < cols; col++) {
                // Sine wave offset — diagonal roll across the board (changes every tick)
                double wave = Math.sin(row * FREQ + col * FREQ + phase);

                // Fixed speckle offset for this specific tile (never changes after construction)
                double speckleValue = speckle[row][col];

                // Combine both: the wave animates, the speckle stays constant underneath it
                int offset = (int) (wave * AMPLITUDE + speckleValue * SPECKLE_AMPLITUDE);

                cache[row][col] = applyOffset(base, offset);
            }
        }
    }

    /**
     * Returns the pre-computed color for this tile.
     * O(1) — just a cache read.
     *
     * @param row grid row
     * @param col grid col
     * @return animated water color for this tile
     */
    public Color getColor(int row, int col) {
        return cache[row][col];
    }

    /**
     * Shifts each RGB channel by offset, clamped to 0-255.
     */
    private Color applyOffset(Color base, int offset) {
        return new Color(
                clamp(base.getRed()   + offset),
                clamp(base.getGreen() + offset),
                clamp(base.getBlue()  + offset)
        );
    }

    private int clamp(int v) {
        return Math.min(255, Math.max(0, v));
    }
}