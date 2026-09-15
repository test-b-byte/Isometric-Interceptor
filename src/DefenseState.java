package aircom.model;

import aircom.render.GameRender;

/**
 * Defense phase. Uses the defender's renderer for input —
 * only the defending player's window accepts clicks this turn.
 */
public class DefenseState implements State {

    private final Player attacker;
    private final Player defender;
    private final GameBoard board;
    private final Missile missile;
    private final GameRender attackerRenderer;
    private final GameRender defenderRenderer;

    public DefenseState(Player attacker, Player defender,
                        GameBoard board, Missile missile,
                        GameRender attackerRenderer,
                        GameRender defenderRenderer) {
        this.attacker = attacker;
        this.defender = defender;
        this.board = board;
        this.missile = missile;
        this.attackerRenderer = attackerRenderer;
        this.defenderRenderer = defenderRenderer;
    }

    @Override
    public State execute() {
        defenderRenderer.setStatusText(defender.getName() + " — click a ship tile to launch interceptor from");
        attackerRenderer.setStatusText("Waiting for " + defender.getName() + " to intercept...");

        GridC interceptLaunch = null;
        while (interceptLaunch == null) {
            GridC selected = defenderRenderer.getSelection();
            Ship occupant = board.getCell(selected).getOccupant();
            if (occupant != null && defender.getFleet().contains(occupant)) {
                interceptLaunch = selected;
            } else {
                defenderRenderer.setStatusText(defender.getName() + " — invalid! Click one of your ship tiles");
            }
        }

        defenderRenderer.setStatusText(defender.getName() + " — click where you think the missile will pass");
        GridC interceptPoint = defenderRenderer.getSelection();

        System.out.println(defender.getName() + " intercept attempt at " + interceptPoint);

        return new ResultState(attacker, defender, board,
                missile, interceptLaunch, interceptPoint,
                attackerRenderer, defenderRenderer);
    }
}