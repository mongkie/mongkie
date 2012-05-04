package prefuse.action.animate;

import java.awt.geom.Rectangle2D;
import prefuse.Visualization;
import prefuse.action.ItemAction;
import prefuse.visual.VisualItem;

/**
 * Animator that linearly interpolates between two positions. This
 * is useful for performing animated transitions.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class LocationAnimator extends ItemAction {

    private final Rectangle2D damagedRect = new Rectangle2D.Double();

    /**
     * Create a new LocationAnimator that processes all data groups.
     */
    public LocationAnimator() {
        super();
    }

    /**
     * Create a new LocationAnimator that processes the specified group.
     * @param group the data group to process.
     */
    public LocationAnimator(String group) {
        super(group);
    }

    /**
     * @see prefuse.action.ItemAction#process(prefuse.visual.VisualItem, double)
     */
    public void process(VisualItem item, double frac) {
        double sx = item.getStartX();
        double sy = item.getStartY();
        double x = sx + frac * (item.getEndX() - sx);
        double y = sy + frac * (item.getEndY() - sy);
        item.setX(x);
        item.setY(y);
        damagedRect.setRect(sx, sy, x, y);
        Visualization v = item.getVisualization();
        for (int i = 0; i < v.getDisplayCount(); i++) {
            v.getDisplay(i).damageReport(damagedRect);
        }
    }
} // end of class LocationAnimator

