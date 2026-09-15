package aircom.render;

import aircom.model.GridC;
import aircom.model.PlayerInput;
import aircom.model.AttackPath;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Handles all player input for AirCom.
 * Owns keyboard, mouse, and hover input.
 * Implements PlayerInput — game logic calls getSelection()
 * to block and wait for player input without knowing
 * anything about how input is gathered.
 *
 * Extracted from GameRender — Single Responsibility:
 * InputHandler handles input, nothing else.
 * GameRender delegates all input calls here.
 */
public class InputHandler implements KeyListener, MouseListener,
        MouseMotionListener {

    private final IsoRig isoRig;
    private final int originX;
    private final int originY;
    private final int rows;
    private final int cols;

    // Current cursor position on the grid
    private volatile GridC cursor;

    // Confirmed selection — set when player clicks or presses Enter
    private volatile GridC selection;

    // Callback so InputHandler can tell BoardRenderer to repaint
    // when cursor moves — without importing BoardRenderer directly
    private final Runnable repaintCallback;

    // Callback for status text updates — forwarded to HUD
    private volatile String[] statusLines = {"Welcome to AirCom"};
    private volatile int activeFlight = 1;

    /**
     * @param isoRig          grid-to-pixel converter for inverse transform
     * @param originX         board origin x offset
     * @param originY         board origin y offset
     * @param rows            board row count
     * @param cols            board column count
     * @param repaintCallback called whenever cursor moves or selection changes
     */
    public InputHandler(IsoRig isoRig, int originX, int originY,
                        int rows, int cols, Runnable repaintCallback) {
        this.isoRig          = isoRig;
        this.originX         = originX;
        this.originY         = originY;
        this.rows            = rows;
        this.cols            = cols;
        this.repaintCallback = repaintCallback;
        this.cursor          = new GridC(0, 0);
        this.selection       = null;
    }

    // --- KeyListener ---

    @Override
    public void keyPressed(KeyEvent e) {
        int row = cursor.row();
        int col = cursor.col();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP    -> row = Math.max(0, row - 1);
            case KeyEvent.VK_DOWN  -> row = Math.min(rows - 1, row + 1);
            case KeyEvent.VK_LEFT  -> col = Math.max(0, col - 1);
            case KeyEvent.VK_RIGHT -> col = Math.min(cols - 1, col + 1);
            case KeyEvent.VK_ENTER -> {
                synchronized (this) {
                    selection = cursor;
                    notify();
                }
            }
        }
        cursor = new GridC(row, col);
        repaintCallback.run();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // --- MouseListener ---

    @Override
    public void mouseClicked(MouseEvent e) {
        synchronized (this) {
            selection = cursor;
            notify();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- MouseMotionListener ---

    @Override
    public void mouseMoved(MouseEvent e) {
        int screenX = e.getX() - originX;
        int screenY = e.getY() - originY;
        int row = isoRig.gridRow(screenX, screenY);
        int col = isoRig.gridCol(screenX, screenY);
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        cursor = new GridC(row, col);
        repaintCallback.run();
    }

    @Override public void mouseDragged(MouseEvent e) {}

    // --- PlayerInput ---


    public synchronized GridC getSelection() {
        selection = null;
        try {
            while (selection == null) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return selection;
    }

    public void setStatusText(String text) {
        this.statusLines = text.split("\n");
        repaintCallback.run();
    }

    public void setActiveFlight(int zone) {
        this.activeFlight = zone;
        repaintCallback.run();
    }

    // Getters for BoardRenderer to read
    public GridC getCursor()       { return cursor; }
    public String[] getStatusLines() { return statusLines; }
    public int getActiveFlight()   { return activeFlight; }
}