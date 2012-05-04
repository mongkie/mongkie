package prefuse.action.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.Display;
import prefuse.util.display.DisplayLib;
import prefuse.visual.VisualItem;
import static prefuse.Visualization.*;

/**
 *
 * @author gentie
 */
public class VerticalLinesLayout extends Layout {

    private final Display display;
    private final String field;
    private final String[] values;
    private final double[] x, h;
    private double y = 0, itemHeight = 0;
    private final Point2D center = new Point2D.Double();
    private final Rectangle2D itemsBound = new Rectangle2D.Double();
    private boolean panToCenter = false, fitViewToBounds = false;

    public VerticalLinesLayout(Display display, String group, String field, String[] values) {
        super(group);
        this.display = display;
        this.field = field;
        this.values = values;
        x = new double[values.length];
        h = new double[values.length];
        setVisualization(display.getVisualization());
        setMargin(20, 50, 20, 50);
    }

    @Deprecated
    public void setFitViewToBounds(boolean fitViewToBounds) {
        this.fitViewToBounds = fitViewToBounds;
    }

    public void setPanToCenter(boolean panToCenter) {
        this.panToCenter = panToCenter;
    }

    public int getNominalSize() {
        return values.length;
    }

    public void run(double frac) {

        if (values.length == 0) {
            return;
        }

        double xInc, startY = -(display.getHeight() / 2) + m_insets.top;
        int xPosAbs = display.getWidth() / 2;
        x[0] = -xPosAbs + m_insets.left;
        h[0] = startY;
        if (values.length > 1) {
            x[values.length - 1] = xPosAbs - m_insets.right;
            h[values.length - 1] = startY;
            xInc = (x[values.length - 1] - x[0]) / (values.length - 1);
            for (int i = 1; i < values.length - 1; i++) {
                x[i] = x[i - 1] + xInc;
                h[i] = startY;
            }
        }

        Iterator<VisualItem> items = m_vis.items(m_group);
        while (items.hasNext()) {
            VisualItem item = items.next();
            itemHeight = item.getBounds().getHeight();
            for (int i = 0; i < values.length; i++) {
                if (item.getString(field).equals(values[i])) {
                    y = h[i] + (itemHeight / 2);
                    setX(item, null, x[i]);
                    setY(item, null, y);
                    h[i] = y + (itemHeight * 2);
                }
            }
        }

        m_vis.getBounds(ALL_ITEMS, itemsBound);
        center.setLocation(itemsBound.getCenterX(), itemsBound.getCenterY());
        if (panToCenter) {
            display.panToAbs(center);
        }
        if (fitViewToBounds) {
            DisplayLib.fitViewToBounds(display, itemsBound, getDuration());
        }
    }
}
