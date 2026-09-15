package aircom.render;

/**convers x,y grid into and isometirc screen.
 * should be used for pixel coordinates. this is all math, not game logic.
 * in plain english, the math is used to proportionally strech the grid squares into  pixel group that can actually be seen
 * https://www.youtube.com/watch?v=04oQ2jOUjkU, structure helped translated into script with discussion with Claude
 */
public class IsoRig {
    //Note finals can only be assigned once. Java internal cape. Compiler should enforce it so it never get reassigned.
    private final int tileWidth;
    private final int tileHeight;

    /**
    *@param final tileWidth width 1 tile in pixels
    *@param final tileHeight height 1 tiel in pixels
     *@param screenX pixel x coordinate relative to board origin
     * @param screenY pixel y coordinate relative to board origin
     * @return the grid row at that screen position
     */

    public IsoRig(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }
//coordinates for where teh pixels of the coordinates should be on screen
    public int pixelX(int row, int col) {
        return (col - row) * (tileWidth /2);
    }
    public int pixelY(int row, int col) {
        return (col +row) * (tileHeight / 2);
    }

    public int gridRow(int screenX, int screenY) {
        // Inverse iso formula solve for row from pixel coords
        double tileHHalf = tileHeight / 2.0;
        double tileWHalf = tileWidth / 2.0;
        return (int) Math.round(screenY / tileHHalf - screenX / tileWHalf) / 2;
    }

    public int gridCol(int screenX, int screenY) {
        double tileHHalf = tileHeight / 2;
        double tileWHalf = tileWidth / 2;
        return (int) Math.round(screenY / tileHHalf + screenX / tileWHalf) / 2;
    }
}
