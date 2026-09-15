package aircom.render;

import aircom.model.GridC;
import aircom.model.GameBoard;
import aircom.model.PlayerInput;
import aircom.model.BoardObserver;
import aircom.model.Cell;
import aircom.model.AttackPath;
import aircom.model.Ship;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game canvas for AirCom.
 *
 * Refactored into focused collaborators:
 * - InputHandler  — all keyboard/mouse input, cursor, getSelection()
 * - ShipRenderer  — loads sprites, draws ship fleet
 * - WaterTexture  — animated water color computation
 * - ArcRenderer   — individual projectile arc drawing
 * - InterceptArc  — unified dual-dot intercept arc
 *
 * GameRender coordinates these — it is the Facade the state machine
 * talks to, delegating each concern to the appropriate class.
 * Implements PlayerInput (forwarding to InputHandler) and BoardObserver.
 */
public class GameRender extends JPanel implements aircom.model.PlayerInput, aircom.model.BoardObserver {

    // Board layout constants
    private static final int COLS     = GameBoard.COLS;
    private static final int ROWS     = GameBoard.ROWS;
    private static final int SPLIT    = GameBoard.ROWS / 2;
    private static final int TILE_W   = 32;
    private static final int TILE_H   = 16;
    private static final int ORIGIN_X = 700;
    private static final int ORIGIN_Y = 200;

    // Zone colors
    private static final Color PLAYER1_FILL   = new Color(42, 90, 160);
    private static final Color PLAYER1_STROKE = new Color(26, 61, 112);
    private static final Color PLAYER2_FILL   = new Color(42, 128, 96);
    private static final Color PLAYER2_STROKE = new Color(26, 80, 64);
    private static final Color DIVIDER        = new Color(255, 215, 0);
    private static final Color CURSOR_FILL    = new Color(255, 255, 100);
    private static final Color CURSOR_STROKE  = new Color(200, 200, 0);
    private static final Color SHIP_FILL      = new Color(180, 140, 80);
    private static final Color SHIP_STROKE    = new Color(120, 90, 40);
    private static final Color HIT_FILL       = new Color(180, 40, 40);
    private static final Color HIT_STROKE     = new Color(120, 20, 20);
    private static final Color MISS_FILL      = new Color(100, 100, 110);
    private static final Color MISS_STROKE    = new Color(70, 70, 70);
    private static final Color ATTACK_PATH_FILL    = new Color(255, 165, 0);
    private static final Color ATTACK_PATH_STROKE  = new Color(200, 120, 0);
    private static final Color INTERCEPT_PATH_FILL   = new Color(180, 80, 255);
    private static final Color INTERCEPT_PATH_STROKE = new Color(120, 40, 180);
    private static final Color[] EXPLOSION_COLORS = {
            new Color(255, 255, 255),
            new Color(255, 60, 60),
            new Color(255, 220, 0)
    };

    // Collaborators
    private final IsoRig isoRig;
    private final GameBoard board;
    private final WaterTexture waterTexture;
    private final InputHandler inputHandler;
    private final ShipRenderer shipRenderer;

    // Fleet references — set by Game after construction
    private List<Ship> p1Fleet = new ArrayList<>();
    private List<Ship> p2Fleet = new ArrayList<>();

    // Arc animation fields
    private ArcRenderer attackArc    = null;
    private ArcRenderer interceptArc = null;
    private double arcT              = 0.0;
    private boolean arcAnimating     = false;

    private InterceptArc interceptArc2  = null;
    private double attackArcT           = 0.0;
    private double interceptArcT        = 1.0;

    private double attackDelay    = 0.0;
    private double interceptDelay = 0.0;
    private double interceptTMax  = 1.0;

    // Tile-by-tile path highlights
    private volatile AttackPath lastAttackPath    = null;
    private volatile boolean attackPathVisible    = false;
    private volatile int visibleAttackCount       = 0;
    private volatile AttackPath lastInterceptPath = null;
    private volatile boolean interceptPathVisible = false;
    private volatile int visibleInterceptCount    = 0;

    // Tile explosion
    private volatile List<GridC> explosionTiles  = new ArrayList<>();
    private volatile boolean explosionVisible    = false;
    private volatile int explosionColorIndex     = 0;

    // Screen-space explosion
    private volatile boolean screenExplosionVisible    = false;
    private volatile int     screenExplosionX          = 0;
    private volatile int     screenExplosionY          = 0;
    private volatile int     screenExplosionRadius     = 0;
    private volatile int     screenExplosionColorIndex = 0;

    public GameRender(GameBoard board, int playerZone) {
        super();
        this.board = board;
        this.isoRig = new IsoRig(TILE_W, TILE_H);

        // InputHandler owns all keyboard/mouse concerns
        inputHandler = new InputHandler(isoRig, ORIGIN_X, ORIGIN_Y,
                ROWS, COLS, this::repaint);
        inputHandler.setActiveFlight(playerZone);

        // ShipRenderer owns sprite loading and drawing
        shipRenderer = new ShipRenderer(isoRig, ORIGIN_X, ORIGIN_Y);

        // WaterTexture owns animated ocean colors
        waterTexture = new WaterTexture(ROWS, COLS);

        // Water animation timer — independent of game logic
        Timer waterTimer = new Timer(27, e -> {
            waterTexture.tick(PLAYER1_FILL, PLAYER2_FILL);
            repaint();
        });
        waterTimer.setRepeats(true);
        waterTimer.start();

        setPreferredSize(new Dimension(1200, 900));
        setBackground(new Color(20, 20, 30));
        addKeyListener(inputHandler);
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);
        setFocusable(true);
    }

    /** Called by Game after construction so ShipRenderer can draw correct fleets. */
    public void setFleets(List<Ship> p1Fleet, List<Ship> p2Fleet) {
        this.p1Fleet = p1Fleet;
        this.p2Fleet = p2Fleet;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D canvas = (Graphics2D) g;
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw board tiles
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                drawTile(canvas, row, col);
            }
        }
        drawDivider(canvas);

        // Draw ships — only active player's fleet
        List<Ship> visibleFleet = inputHandler.getActiveFlight() == 1 ? p1Fleet : p2Fleet;
        //shipRenderer.drawFleet(canvas, visibleFleet);

        // Draw HUD
        drawHUD(canvas);

        // Intercepted case — unified arc
        if (interceptArc2 != null) {
            interceptArc2.draw(canvas, attackArcT, interceptArcT);
        }

        // Non-intercepted case — two separate arcs
        if (arcAnimating || attackArc != null) {
            if (attackArc != null && arcT >= attackDelay) {
                double t = attackDelay < 1.0
                        ? (arcT - attackDelay) / (1.0 - attackDelay) : 1.0;
                attackArc.draw(canvas, Math.min(t, interceptTMax));
            }
            if (interceptArc != null && arcT >= interceptDelay) {
                double t = interceptDelay < 1.0
                        ? (arcT - interceptDelay) / (1.0 - interceptDelay) : 1.0;
                interceptArc.draw(canvas, Math.min(t, interceptTMax));
            }
        }

        // Screen explosion — always renders regardless of arc state
        if (screenExplosionVisible) {
            canvas.setColor(EXPLOSION_COLORS[screenExplosionColorIndex % 3]);
            canvas.fillOval(screenExplosionX - screenExplosionRadius,
                    screenExplosionY - screenExplosionRadius,
                    screenExplosionRadius * 2, screenExplosionRadius * 2);
            canvas.setColor(Color.WHITE);
            canvas.setStroke(new java.awt.BasicStroke(2));
            canvas.drawOval(screenExplosionX - screenExplosionRadius,
                    screenExplosionY - screenExplosionRadius,
                    screenExplosionRadius * 2, screenExplosionRadius * 2);
        }
    }

    private void drawTile(Graphics2D canvas, int row, int col) {
        int tCenterX = ORIGIN_X + isoRig.pixelX(row, col);
        int tCenterY = ORIGIN_Y + isoRig.pixelY(row, col);

        Polygon diamond = new Polygon();
        diamond.addPoint(tCenterX,            tCenterY);
        diamond.addPoint(tCenterX + TILE_W/2, tCenterY + TILE_H/2);
        diamond.addPoint(tCenterX,            tCenterY + TILE_H);
        diamond.addPoint(tCenterX - TILE_W/2, tCenterY + TILE_H/2);

        Cell.CellState state = board.getCell(new GridC(row, col)).getState();
        int activeFlight = inputHandler.getActiveFlight();

        boolean isActiveZone   = (activeFlight == 1 && row < SPLIT)
                || (activeFlight == 2 && row >= SPLIT);
        boolean isOpponentZone = (activeFlight == 1 && row >= SPLIT)
                || (activeFlight == 2 && row < SPLIT);

        boolean isHit      = isOpponentZone && state == Cell.CellState.HIT;
        boolean isMiss     = (isOpponentZone || isActiveZone) && state == Cell.CellState.MISS;
        boolean isOccupied = isActiveZone && state == Cell.CellState.OCCUPIED;
        GridC cursor = inputHandler.getCursor();
        boolean isCursor = cursor != null
                && cursor.row() == row && cursor.col() == col;

        int attackIndex = (lastAttackPath != null)
                ? lastAttackPath.getPath().indexOf(new GridC(row, col)) : -1;
        boolean isAttackPath = attackPathVisible
                && attackIndex >= 0 && attackIndex < visibleAttackCount;

        int interceptIndex = (lastInterceptPath != null)
                ? lastInterceptPath.getPath().indexOf(new GridC(row, col)) : -1;
        boolean isInterceptPath = interceptPathVisible
                && interceptIndex >= 0 && interceptIndex < visibleInterceptCount;

        boolean isExplosion = explosionVisible
                && explosionTiles.contains(new GridC(row, col));

        Color fillColor;
        Color strokeColor;

        if (isExplosion) {
            fillColor   = EXPLOSION_COLORS[explosionColorIndex % 3];
            strokeColor = fillColor.darker();
        } else if (isCursor) {
            fillColor   = CURSOR_FILL;
            strokeColor = CURSOR_STROKE;
        } else if (isInterceptPath) {
            fillColor   = INTERCEPT_PATH_FILL;
            strokeColor = INTERCEPT_PATH_STROKE;
        } else if (isAttackPath) {
            fillColor   = ATTACK_PATH_FILL;
            strokeColor = ATTACK_PATH_STROKE;
        } else if (isHit) {
            fillColor   = HIT_FILL;
            strokeColor = HIT_STROKE;
        } else if (isMiss) {
            fillColor   = MISS_FILL;
            strokeColor = MISS_STROKE;
        } else if (isOccupied) {
            fillColor   = SHIP_FILL;
            strokeColor = SHIP_STROKE;
        } else {
            fillColor   = waterTexture.getColor(row, col);
            strokeColor = row < SPLIT ? PLAYER1_STROKE : PLAYER2_STROKE;
        }

        canvas.setColor(fillColor);
        canvas.fillPolygon(diamond);
        canvas.setColor(strokeColor);
        canvas.drawPolygon(diamond);
    }

    private void drawDivider(Graphics2D canvas) {
        int startX = ORIGIN_X + isoRig.pixelX(SPLIT, 0);
        int startY = ORIGIN_Y + isoRig.pixelY(SPLIT, 0);
        int endX   = ORIGIN_X + isoRig.pixelX(SPLIT, COLS);
        int endY   = ORIGIN_Y + isoRig.pixelY(SPLIT, COLS);
        canvas.setColor(DIVIDER);
        canvas.drawLine(startX, startY, endX, endY);
    }

    private void drawHUD(Graphics2D canvas) {
        String[] statusLines = inputHandler.getStatusLines();
        canvas.setFont(new Font("Monospaced", Font.BOLD, 16));
        int y = 35;
        for (String line : statusLines) {
            if (line.contains("INTERCEPTED")) canvas.setColor(new Color(180, 80, 255));
            else if (line.contains("HIT"))    canvas.setColor(new Color(255, 80, 80));
            else if (line.contains("MISS"))   canvas.setColor(new Color(200, 200, 200));
            else if (line.contains("SUNK"))   canvas.setColor(new Color(255, 60, 60));
            else if (line.contains("GAME OVER")) canvas.setColor(new Color(255, 215, 0));
            else                              canvas.setColor(Color.WHITE);
            canvas.drawString(line, 20, y);
            y += 22;
        }
        y = Math.max(y + 10, 120);
        canvas.setFont(new Font("Monospaced", Font.BOLD, 14));
        canvas.setColor(new Color(100, 160, 255));
        canvas.drawString("▲ PLAYER 1", 20, y);
        canvas.setColor(new Color(100, 220, 140));
        canvas.drawString("▼ PLAYER 2", 20, y + 20);

        GridC cursor = inputHandler.getCursor();
        if (cursor != null) {
            canvas.setFont(new Font("Monospaced", Font.PLAIN, 12));
            canvas.setColor(new Color(150, 150, 150));
            canvas.drawString("cursor: (" + cursor.row() + "," + cursor.col() + ")",
                    20, y + 45);
        }
    }

    public void launchArcs(GridC attackLaunch, GridC attackTarget,
                           GridC interceptLaunch, GridC interceptTarget,
                           boolean intercepted) {

        if (intercepted) {
            interceptArc2 = new InterceptArc(isoRig, ORIGIN_X, ORIGIN_Y,
                    attackLaunch, interceptLaunch, interceptTarget);

            double dA = interceptArc2.getAttackSegmentDistance();
            double dI = interceptArc2.getInterceptSegmentDistance();
            final double aDelay = dA < dI ? (dI - dA) / dI : 0.0;
            final double iDelay = dI < dA ? (dA - dI) / dA : 0.0;

            attackArcT    = 0.0;
            interceptArcT = 1.0;
            arcT          = 0.0;
            arcAnimating  = true;

            Timer arcTimer = new Timer(16, null);
            arcTimer.addActionListener(evt -> {
                arcT += 0.037;
                double capped = Math.min(arcT, 1.0);
                if (capped >= aDelay) {
                    double progress = (capped - aDelay) / (1.0 - aDelay);
                    attackArcT = interceptArc2.getMeetingT() * Math.min(progress, 1.0);
                }
                if (capped >= iDelay) {
                    double progress = (capped - iDelay) / (1.0 - iDelay);
                    interceptArcT = 1.0 - (1.0 - interceptArc2.getMeetingT())
                            * Math.min(progress, 1.0);
                }
                repaint();
                if (arcT >= 1.0) {
                    arcTimer.stop();
                    arcAnimating = false;
                    int[] meet = interceptArc2.getMeetingScreenPosition();
                    SwingUtilities.invokeLater(() -> triggerScreenExplosion(meet[0], meet[1]));
                    Timer clearTimer = new Timer(1500, e -> {
                        interceptArc2 = null;
                        repaint();
                    });
                    clearTimer.setRepeats(false);
                    clearTimer.start();
                }
            });
            arcTimer.setRepeats(true);
            arcTimer.start();

        } else {
            interceptTMax = 1.0;
            attackArc = new ArcRenderer(isoRig, ORIGIN_X, ORIGIN_Y,
                    attackLaunch, attackTarget, new Color(255, 165, 0), 2.5f);
            interceptArc = new ArcRenderer(isoRig, ORIGIN_X, ORIGIN_Y,
                    interceptLaunch, interceptTarget, new Color(220, 50, 50), 2.5f);

            double d_a = attackArc.getDistance();
            double d_i = interceptArc.getDistance();
            if (d_a >= d_i) {
                attackDelay    = 0.0;
                interceptDelay = (d_a - d_i) / d_a;
            } else {
                interceptDelay = 0.0;
                attackDelay    = (d_i - d_a) / d_i;
            }

            arcT = 0.0;
            arcAnimating = true;

            Timer arcTimer = new Timer(16, null);
            arcTimer.addActionListener(evt -> {
                arcT += 0.037;
                if (arcT >= 1.0) {
                    arcT = 1.0;
                    arcAnimating = false;
                    arcTimer.stop();
                    Timer clearTimer = new Timer(1500, e -> {
                        attackArc    = null;
                        interceptArc = null;
                        repaint();
                    });
                    clearTimer.setRepeats(false);
                    clearTimer.start();
                }
                repaint();
            });
            arcTimer.setRepeats(true);
            arcTimer.start();
        }
    }

    @Override
    public void showAttackPath(AttackPath path, boolean intercepted, GridC interceptPoint) {
        lastAttackPath = path;
        visibleAttackCount = 0;
        attackPathVisible = true;
        repaint();
        final int totalTiles = intercepted
                ? findInterceptTile(path, interceptPoint) : path.getPath().size();
        Timer animTimer = new Timer(150, null);
        animTimer.addActionListener(evt -> {
            visibleAttackCount++;
            repaint();
            if (visibleAttackCount >= totalTiles) {
                animTimer.stop();
                if (intercepted) triggerExplosion(path.getPath().get(totalTiles - 1));
                Timer clearTimer = new Timer(2500, e -> {
                    attackPathVisible = false;
                    lastAttackPath = null;
                    visibleAttackCount = 0;
                    repaint();
                });
                clearTimer.setRepeats(false);
                clearTimer.start();
            }
        });
        animTimer.setRepeats(true);
        animTimer.start();
    }

    private int findInterceptTile(AttackPath path, GridC interceptPoint) {
        int index = path.getPath().indexOf(interceptPoint);
        return index >= 0 ? index + 1 : path.getPath().size();
    }

    @Override
    public void showInterceptPath(AttackPath path, boolean successful) {
        lastInterceptPath = path;
        visibleInterceptCount = 0;
        interceptPathVisible = true;
        repaint();
        final int totalTiles = path.getPath().size();
        Timer animTimer = new Timer(150, null);
        animTimer.addActionListener(evt -> {
            visibleInterceptCount++;
            repaint();
            if (visibleInterceptCount >= totalTiles) {
                animTimer.stop();
                if (successful) triggerExplosion(path.getPath().get(totalTiles - 1));
                Timer clearTimer = new Timer(2500, e -> {
                    interceptPathVisible = false;
                    lastInterceptPath = null;
                    visibleInterceptCount = 0;
                    repaint();
                });
                clearTimer.setRepeats(false);
                clearTimer.start();
            }
        });
        animTimer.setRepeats(true);
        animTimer.start();
    }

    private void triggerExplosion(GridC center) {
        explosionTiles = new ArrayList<>();
        explosionTiles.add(center);
        if (center.row() > 0)      explosionTiles.add(new GridC(center.row()-1, center.col()));
        if (center.row() < ROWS-1) explosionTiles.add(new GridC(center.row()+1, center.col()));
        if (center.col() > 0)      explosionTiles.add(new GridC(center.row(), center.col()-1));
        if (center.col() < COLS-1) explosionTiles.add(new GridC(center.row(), center.col()+1));
        explosionColorIndex = 0;
        explosionVisible = true;
        repaint();
        Timer flickerTimer = new Timer(200, null);
        final int[] ticks = {0};
        flickerTimer.addActionListener(evt -> {
            ticks[0]++;
            explosionColorIndex = ticks[0] % 3;
            repaint();
            if (ticks[0] >= 8) {
                flickerTimer.stop();
                explosionVisible = false;
                explosionTiles.clear();
                repaint();
            }
        });
        flickerTimer.setRepeats(true);
        flickerTimer.start();
    }

    private void triggerScreenExplosion(int px, int py) {
        screenExplosionX = px;
        screenExplosionY = py;
        screenExplosionRadius = 5;
        screenExplosionColorIndex = 0;
        screenExplosionVisible = true;
        repaint();
        Timer flickerTimer = new Timer(120, null);
        final int[] ticks = {0};
        flickerTimer.addActionListener(evt -> {
            ticks[0]++;
            screenExplosionColorIndex = ticks[0] % 3;
            screenExplosionRadius = ticks[0] < 5
                    ? 5 + ticks[0] * 8
                    : Math.max(0, 45 - (ticks[0] - 5) * 10);
            repaint();
            if (ticks[0] >= 10) {
                flickerTimer.stop();
                screenExplosionVisible = false;
                repaint();
            }
        });
        flickerTimer.setRepeats(true);
        flickerTimer.start();
    }

    @Override
    public void showExplosion(GridC center) {
        SwingUtilities.invokeLater(() -> triggerExplosion(center));
    }

    @Override
    public void setActiveFlight(int zone) {
        inputHandler.setActiveFlight(zone);
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void setStatusText(String text) {
        inputHandler.setStatusText(text);
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void onBoardChanged() {
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public GridC getSelection() {
        return inputHandler.getSelection();
    }
}