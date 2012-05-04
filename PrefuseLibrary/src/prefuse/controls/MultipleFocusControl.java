package prefuse.controls;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import prefuse.Visualization;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 *
 * @author lunardo
 */
public class MultipleFocusControl extends FocusControl {

    private final Visualization v;
    private final TupleSet focusedTupleSet;
    private final boolean focusFixed;
    public static final TupleSetListener DOFIX = new TupleSetListener() {

        @Override
        public void tupleSetChanged(TupleSet ts, Tuple[] added, Tuple[] removed) {
            for (Tuple t : removed) {
                ((VisualItem) t).setFixed(false);
            }
            for (Tuple t : added) {
                ((VisualItem) t).setFixed(false);
                ((VisualItem) t).setFixed(true);
            }
        }
    };

    public MultipleFocusControl(Visualization v) {
        this(v, Visualization.DRAW, null);
    }

    public MultipleFocusControl(Visualization v, String filterGroup) {
        this(v, Visualization.DRAW, filterGroup, false);
    }

    public MultipleFocusControl(Visualization v, String filterGroup, boolean focusFixed) {
        this(v, Visualization.DRAW, filterGroup, focusFixed);
    }

    public MultipleFocusControl(Visualization v, String activity, String filterGroup) {
        this(v, activity, filterGroup, false);
    }

    public MultipleFocusControl(Visualization v, String activity, String filterGroup, boolean focusFixed) {
        super(1, activity);
        this.v = v;
        this.focusedTupleSet = v.getFocusGroup(group);
        this.focusFixed = focusFixed;
        if (focusFixed) {
            focusedTupleSet.addTupleSetListener(DOFIX);
        }
        setFilter(new InGroupPredicate(filterGroup));
    }

    public MultipleFocusControl setClicks(int clicks) {
        ccount = clicks;
        return this;
    }

    public boolean isFocusFixed() {
        return focusFixed;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (!UILib.isButtonPressed(e, button)) {
            return;
        }

        curFocus = null;
        focusedTupleSet.clear();

        runActivity(v);
    }

    @Override
    public void itemClicked(VisualItem item, MouseEvent e) {

        if (!filterCheck(item)
                || v != item.getVisualization()
                || !UILib.isButtonPressed(e, button)
                || e.getClickCount() != ccount) {
            return;
        }

        if (e.isControlDown()) {
            if (focusedTupleSet.containsTuple(item)) {
                focusedTupleSet.removeTuple(item);
                if (item == curFocus) {
                    curFocus = null;
                    Iterator<VisualItem> focusedTuples = focusedTupleSet.tuples();
                    while (focusedTuples.hasNext()) {
                        curFocus = focusedTuples.next();
                    }
                }
            } else {
                focusedTupleSet.addTuple(item);
                curFocus = item;
            }
        } else {
            focusedTupleSet.setTuple(item);
            curFocus = item;
        }

        runActivity(v);
    }
}
