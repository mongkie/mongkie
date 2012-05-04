package prefuse.action.layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.Visualization;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

/**
 *
 * @author gentie
 */
public class AggregateLayout extends Layout {

    private int margin = 5; // convex hull pixel margin
    private double[] points;   // buffer for computing convex hulls

    public AggregateLayout() {
        this(Visualization.AGGR_ITEMS);
    }

    public AggregateLayout(int margin) {
        this(Visualization.AGGR_ITEMS);
        this.margin = margin;
    }

    public AggregateLayout(String aggrGroup) {
        super(aggrGroup);
    }

    public AggregateLayout(String aggrGroup, int margin) {
        super(aggrGroup);
        this.margin = margin;
    }

    @Override
    public void run(double frac) {

        AggregateTable aggregates = (AggregateTable) m_vis.getVisualGroup(m_group);
        // do we have any to process?
        if (aggregates.getTupleCount() == 0) {
            return;
        }

        // update buffers
        int maxSize = 0;
        for (Iterator<AggregateItem> aggrs = aggregates.tuples(); aggrs.hasNext();) {
            maxSize = Math.max(maxSize, 4 * 2 * aggrs.next().getAggregateSize());
        }
        if (points == null || maxSize > points.length) {
            points = new double[maxSize];
        }

        // compute and assign convex hull for each aggregate
        Iterator<AggregateItem> aggrs = m_vis.visibleItems(m_group);
        while (aggrs.hasNext()) {

            AggregateItem aggrItem = aggrs.next();

            int idx = 0;
            if (aggrItem.getAggregateSize() == 0) {
                continue;
            }

            Iterator<VisualItem> items = aggrItem.items();
            VisualItem item = null;
            while (items.hasNext()) {
                item = items.next();
                if (item.isVisible()) {
                    addPoint(points, idx, item, margin);
                    idx += 2 * 4;
                }
            }
            // if no aggregates are visible, do nothing
            if (idx == 0) {
                continue;
            }

            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(points, idx);

            // prepare viz attribute array
            float[] fhull = (float[]) aggrItem.get(VisualItem.POLYGON);
            if (fhull == null || fhull.length < nhull.length) {
                fhull = new float[nhull.length];
            } else if (fhull.length > nhull.length) {
                fhull[nhull.length] = Float.NaN;
            }

            // copy hull values
            for (int j = 0; j < nhull.length; j++) {
                fhull[j] = (float) nhull[j];
            }
            aggrItem.set(VisualItem.POLYGON, fhull);

            // force invalidation
            aggrItem.setValidated(false);
        }
    }

    private static void addPoint(double[] points, int idx, VisualItem item, int growth) {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX()) - growth, minY = (b.getMinY()) - growth;
        double maxX = (b.getMaxX()) + growth, maxY = (b.getMaxY()) + growth;
        points[idx] = minX;
        points[idx + 1] = minY;
        points[idx + 2] = minX;
        points[idx + 3] = maxY;
        points[idx + 4] = maxX;
        points[idx + 5] = minY;
        points[idx + 6] = maxX;
        points[idx + 7] = maxY;
    }
}
