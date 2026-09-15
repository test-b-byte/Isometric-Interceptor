package aircom.render;

import aircom.model.GridC;
import aircom.model.Ship;
import aircom.model.ShipType;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * Draws ship sprites on the isometric board.
 * Owns all sprite images — loads them at construction.
 * One sprite drawn per ship, centered between first and last segment.
 * Uses the same placeholder sprite for all ship types until
 * individual sprites are drawn and swapped in.
 *
 * Extracted from GameRender to keep rendering concerns separated —
 * Single Responsibility: ShipRenderer draws ships, nothing else.
 */
public class ShipRenderer {

    private final IsoRig isoRig;
    private final int originX;
    private final int originY;

    // Placeholder sprite used for all ship types for now.
    // Replace with per-type map once individual sprites are drawn.
    private BufferedImage shipSprite;

    /**
     * @param isoRig  grid-to-pixel converter
     * @param originX board origin x offset in screen pixels
     * @param originY board origin y offset in screen pixels
     */
    public ShipRenderer(IsoRig isoRig, int originX, int originY) {
        this.isoRig  = isoRig;
        this.originX = originX;
        this.originY = originY;
        loadSprites();
    }

    /**
     * Loads sprite images from disk.
     * Currently loads one placeholder sprite for all ship types.
     * When individual sprites are ready, load them here by ShipType.
     */
    private void loadSprites() {
        try {
            shipSprite = ImageIO.read(
                    new File("src/Resources/destroyerpixel.png"));
        } catch (Exception e) {
            System.out.println("ShipRenderer: could not load sprite — " + e.getMessage());
        }
    }

    /**
     * Draws one sprite per ship in the fleet.
     * Sprite is centered on the midpoint between the ship's
     * first and last segment in screen space.
     *
     * @param canvas Graphics2D paintbrush
     * @param fleet  list of ships to draw
     */
    public void drawFleet(Graphics2D canvas, List<Ship> fleet) {
        if (shipSprite == null) return;
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (Ship ship : fleet) {
            drawShip(canvas, ship);
        }
    }

    private void drawShip(Graphics2D canvas, Ship ship) {
        List<GridC> coords = ship.getCoordinates();
        if (coords.isEmpty()) return;

        // Midpoint between first and last segment in screen space
        GridC first = coords.get(0);
        GridC last  = coords.get(coords.size() - 1);

        int x1 = originX + isoRig.pixelX(first.row(), first.col());
        int y1 = originY + isoRig.pixelY(first.row(), first.col());
        int x2 = originX + isoRig.pixelX(last.row(), last.col());
        int y2 = originY + isoRig.pixelY(last.row(), last.col());

        int centerX = (x1 + x2) / 2;
        int centerY = (y1 + y2) / 2;

        int imgW = shipSprite.getWidth();
        int imgH = shipSprite.getHeight();

        canvas.drawImage(shipSprite,
                centerX - imgW / 2,
                centerY - imgH / 2,
                null);
    }
}