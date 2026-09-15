package aircom.model;

import aircom.render.GameRender;

/**
 * Resolution phase. Resolves the turn — checks intercept, applies damage,
 * notifies observers, checks win condition, swaps roles for next turn.
 *
 * Ships are hidden during resolution — players only see flight paths.
 * Both renderers receive identical animations so both players watch
 * the same resolution play out on their respective windows.
 *
 * Arc animations replace tile-by-tile path highlights — parabolic arcs
 * render in screen pixel space above the board surface, giving visual
 * depth to the missile trajectories.
 */
public class ResultState implements State {

    private final Player attacker;
    private final Player defender;
    private final GameBoard board;
    private final Missile missile;
    private final GridC interceptLaunch;
    private final GridC interceptPoint;
    private final GameRender attackerRenderer;
    private final GameRender defenderRenderer;

    public ResultState(Player attacker, Player defender,
                       GameBoard board, Missile missile,
                       GridC interceptLaunch, GridC interceptPoint,
                       GameRender attackerRenderer,
                       GameRender defenderRenderer) {
        this.attacker = attacker;
        this.defender = defender;
        this.board = board;
        this.missile = missile;
        this.interceptLaunch = interceptLaunch;
        this.interceptPoint = interceptPoint;
        this.attackerRenderer = attackerRenderer;
        this.defenderRenderer = defenderRenderer;
    }

    @Override
    public State execute() {
        attackerRenderer.setStatusText("Resolving turn...");
        defenderRenderer.setStatusText("Resolving turn...");

        boolean intercepted = false;
        boolean miss = false;
        Ship shipSunk = null;

        AttackPath interceptPath = AttackCalculator.calculate(interceptLaunch, interceptPoint);

        if (missile.getPath().contains(interceptPoint)) {
            intercepted = true;

            // Launch both arcs — attack stops at intercept point
            attackerRenderer.launchArcs(missile.getLaunchPoint(), missile.getTarget(),
                    interceptLaunch, interceptPoint, true);
            defenderRenderer.launchArcs(missile.getLaunchPoint(), missile.getTarget(),
                    interceptLaunch, interceptPoint, true);

            System.out.println("Intercepted!");

        } else {
            // Launch both arcs — attack travels full path to target
            attackerRenderer.launchArcs(missile.getLaunchPoint(), missile.getTarget(),
                    interceptLaunch, interceptPoint, false);
            defenderRenderer.launchArcs(missile.getLaunchPoint(), missile.getTarget(),
                    interceptLaunch, interceptPoint, false);

            Ship targetOccupant = board.getCell(missile.getTarget()).getOccupant();
            if (targetOccupant != null && defender.getFleet().contains(targetOccupant)) {
                targetOccupant.hit(missile.getTarget());
                board.getCell(missile.getTarget()).setState(Cell.CellState.HIT);
                board.notifyBoardChanged();
                attackerRenderer.showExplosion(missile.getTarget());
                defenderRenderer.showExplosion(missile.getTarget());
                if (targetOccupant.isSunk()) {
                    shipSunk = targetOccupant;
                    System.out.println(targetOccupant.getType() + " sunk!");
                }
            } else {
                miss = true;
                board.getCell(missile.getTarget()).setState(Cell.CellState.MISS);
                board.notifyBoardChanged();
                System.out.println("Miss.");
            }
        }

        String outcome = intercepted ? "INTERCEPTED" : (miss ? "MISS" : "HIT");
        String sunkLine = shipSunk != null ? "\nSunk: " + shipSunk.getType() : "";
        String report = "TURN RESULT\n"
                + "Target: (" + missile.getTarget().row() + "," + missile.getTarget().col() + ")\n"
                + "Intercept: (" + interceptPoint.row() + "," + interceptPoint.col() + ")\n"
                + "Outcome: " + outcome + sunkLine + "\nClick to continue.";

        attackerRenderer.setStatusText(report);
        defenderRenderer.setStatusText(report);

        boolean defenderLost = defender.getFleet().stream().allMatch(Ship::isSunk);
        boolean attackerLost = attacker.getFleet().stream().allMatch(Ship::isSunk);
        boolean gameOver = defenderLost || attackerLost;
        String winnerName = defenderLost ? attacker.getName()
                : attackerLost ? defender.getName() : null;

        attacker.onTurnImpact(missile.getTarget(), intercepted, miss, shipSunk, gameOver, winnerName);
        defender.onTurnImpact(missile.getTarget(), intercepted, miss, shipSunk, gameOver, winnerName);

        if (gameOver) {
            attackerRenderer.setStatusText("GAME OVER — " + winnerName + " wins!\nClick to exit.");
            defenderRenderer.setStatusText("GAME OVER — " + winnerName + " wins!\nClick to exit.");
            attacker.onGameOver(winnerName);
            defender.onGameOver(winnerName);
            attackerRenderer.getSelection();
            return new GameOverState();
        }

        attackerRenderer.getSelection();

        return new AttackState(defender, attacker, board,
                defenderRenderer, attackerRenderer);
    }
}