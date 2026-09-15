package aircom.model;

/**
 * Terminal state — game is over, no further actions accepted.
 * execute() returns itself so the game loop knows to stop.
 */
public class GameOverState implements State {

    @Override
    public State execute() {
        System.exit(0);
        return this;
    }
}