package aircom.model;

/**
 * While GameObserver tracks game state. turn updates, boarrd observer should help with renders and visuals
 */
public interface BoardObserver {
    void onBoardChanged();
}