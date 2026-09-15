package aircom.model;
import java.util.List;

//util.List has a builtin feature or functions beyond standard array,. including not having a fixed size and
// having methods that can be used quickly like add or contains check.
//This biggest thing here is since every attack has totally different potential size, i think this is
// just simpler and avoids some busy work.

public class AttackPath {
    private final List<GridC> path;

    public AttackPath(List<GridC> path) {
        this.path = path;
    }

    public List<GridC> getPath() {
        return path;
    }

    public boolean contains(GridC position) {
        return path.contains(position);
    }
}
