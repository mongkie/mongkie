package prefuse.controls;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 *
 * @author lunardo
 */
public class MultipleDragControl extends DragControl {

    private int modifierMask;

    public MultipleDragControl() {
        this(KeyEvent.SHIFT_DOWN_MASK, Visualization.DRAW);
    }

    public MultipleDragControl(String activity) {
        this(KeyEvent.SHIFT_DOWN_MASK, activity);
    }

    public MultipleDragControl(int modifierMask) {
        this(modifierMask, null);
    }

    public MultipleDragControl(int modifierMask, String activity) {
        super(activity);
        this.modifierMask = modifierMask;
    }

    public int getModifier() {
        return modifierMask;
    }

    public void setModifier(int modifier) {
        this.modifierMask = modifier;
    }

    @Override
    public void itemDragged(VisualItem item, MouseEvent e) {

        if (!SwingUtilities.isLeftMouseButton(e) || (e.getModifiersEx() & modifierMask) != modifierMask) {
            super.itemDragged(item, e);
            return;
        }

        Visualization v = item.getVisualization();

        TupleSet focusSet = v.getFocusGroup(Visualization.FOCUS_ITEMS);

        Display d = (Display) e.getComponent();

        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX() - down.getX();
        double dy = temp.getY() - down.getY();

        Iterator<VisualItem> tuples = focusSet.tuples();

        while (tuples.hasNext()) {

            VisualItem jtem = tuples.next();
            double x = jtem.getX();
            double y = jtem.getY();

            jtem.setStartX(x);
            jtem.setStartY(y);
            jtem.setX(x + dx);
            jtem.setY(y + dy);
            jtem.setEndX(x + dx);
            jtem.setEndY(y + dy);
        }

        if (repaint) {
            v.repaint();
        }
        if (action != null) {
            v.cancel(action);
            v.run(action);
        }

        down.setLocation(temp);
        dragged = true;
    }
}
