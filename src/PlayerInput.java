package aircom.model;

/**
 * This is functioning as a sort of adapter pattern: translating
 * input from the logic into retreivables for the GameRender
 */
public interface PlayerInput {
    GridC getSelection();
    void showInterceptPath(AttackPath path, boolean successful);
    void setActiveFlight(int zone);
    void setStatusText(String text);
    void showExplosion(GridC center);
    void showAttackPath(AttackPath path, boolean intercepted, GridC interceptPoint);
}