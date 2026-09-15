package aircom.model;

import aircom.render.GameRender;

/**
 * Attack phase. Uses the attacker's renderer for input —
 * only the attacking player's window accepts clicks this turn.
 * Defender's window shows status but is locked.
 */
public class AttackState implements State {

    private final Player attacker;
    private final Player defender;
    private final GameBoard board;
    private final GameRender attackerRenderer;
    private final GameRender defenderRenderer;

    public AttackState(Player attacker, Player defender,
                       GameBoard board,
                       GameRender attackerRenderer,
                       GameRender defenderRenderer) {
        this.attacker = attacker;
        this.defender = defender;
        this.board = board;
        this.attackerRenderer = attackerRenderer;
        this.defenderRenderer = defenderRenderer;
    }

    @Override
    public State execute() {
        attackerRenderer.setStatusText(attacker.getName() + " — click a ship tile to launch from");
        defenderRenderer.setStatusText("Waiting for " + attacker.getName() + " to attack...");

        GridC launchPoint = null;
        while (launchPoint == null) {
            GridC selected = attackerRenderer.getSelection();
            Ship occupant = board.getCell(selected).getOccupant();
            if (occupant != null && attacker.getFleet().contains(occupant)) {
                launchPoint = selected;
            } else {
                attackerRenderer.setStatusText(attacker.getName() + " — invalid! Click one of your ship tiles");
            }
        }

        attackerRenderer.setStatusText(attacker.getName() + " — click a target on opponent's half");
        GridC target = attackerRenderer.getSelection();

        AttackPath path = AttackCalculator.calculate(launchPoint, target);
        Missile missile = new Missile(launchPoint, target, path);

        System.out.println(attacker.getName() + " launched from " + launchPoint + " targeting " + target);

        return new DefenseState(attacker, defender, board,
                missile, attackerRenderer, defenderRenderer);
    }
}