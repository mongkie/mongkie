package prefuse.util.display;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.Display;
import prefuse.util.GraphicsLib;
import prefuse.visual.VisualItem;

/**
 * Library routines pertaining to a prefuse Display.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class DisplayLib {

    private DisplayLib() {
        // don't instantiate
    }

    /**
     * Get a bounding rectangle of the VisualItems in the input iterator.
     * @param iter an iterator of VisualItems
     * @param margin a margin to add on to the bounding rectangle
     * @param b the Rectangle instance in which to store the result
     * @return the bounding rectangle. This is the same object as the
     * parameter <code>b</code>.
     */
    public static Rectangle2D getBounds(
            Iterator iter, double margin, Rectangle2D b) {
        b.setFrame(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        // TODO: synchronization?
        if (iter.hasNext()) {
            VisualItem item = (VisualItem) iter.next();
            Rectangle2D nb = item.getBounds();
            b.setFrame(nb);
        }
        while (iter.hasNext()) {
            VisualItem item = (VisualItem) iter.next();
            Rectangle2D nb = item.getBounds();
            double x1 = (nb.getMinX() < b.getMinX() ? nb.getMinX() : b.getMinX());
            double x2 = (nb.getMaxX() > b.getMaxX() ? nb.getMaxX() : b.getMaxX());
            double y1 = (nb.getMinY() < b.getMinY() ? nb.getMinY() : b.getMinY());
            double y2 = (nb.getMaxY() > b.getMaxY() ? nb.getMaxY() : b.getMaxY());
            b.setFrame(x1, y1, x2 - x1, y2 - y1);
        }
        b.setFrame(b.getMinX() - margin,
                b.getMinY() - margin,
                b.getWidth() + 2 * margin,
                b.getHeight() + 2 * margin);
        return b;
    }

    /**
     * Get a bounding rectangle of the VisualItems in the input iterator.
     * @param iter an iterator of VisualItems
     * @param margin a margin to add on to the bounding rectangle
     * @return the bounding rectangle. A new Rectangle2D instance is
     * allocated and returned.
     */
    public static Rectangle2D getBounds(Iterator iter, double margin) {
        Rectangle2D b = new Rectangle2D.Double();
        return getBounds(iter, margin, b);
    }

    public static Rectangle2D getUnionBounds(Iterator<? extends VisualItem> iter, double margin, Rectangle2D r) {
        if (iter.hasNext()) {
            VisualItem item = iter.next();
            r.setRect(item.getBounds());
        }
        while (iter.hasNext()) {
            VisualItem item = iter.next();
            Rectangle2D.union(item.getBounds(), r, r);
        }
        GraphicsLib.expand(r, margin);
        return r;
    }

    public static Rectangle2D getUnionBoundsWithoutValidating(Iterator<? extends VisualItem> iter, double margin, Rectangle2D r) {
        if (iter.hasNext()) {
            VisualItem item = iter.next();
            r.setRect((Rectangle2D) item.get(VisualItem.BOUNDS));
        }
        while (iter.hasNext()) {
            VisualItem item = iter.next();
            Rectangle2D.union((Rectangle2D) item.get(VisualItem.BOUNDS), r, r);
        }
        GraphicsLib.expand(r, margin);
        return r;
    }

    /**
     * Return the centroid (averaged location) of a group of items.
     * @param iter an iterator of VisualItems
     * @param p a Point2D instance in which to store the result
     * @return the centroid point. This is the same object as the
     * parameter <code>p</code>.
     */
    public static Point2D getCentroid(Iterator iter, Point2D p) {
        double cx = 0, cy = 0;
        int count = 0;

        while (iter.hasNext()) {
            VisualItem item = (VisualItem) iter.next();
            double x = item.getX(), y = item.getY();
            if (!(Double.isInfinite(x) || Double.isNaN(x))
                    && !(Double.isInfinite(y) || Double.isNaN(y))) {
                cx += x;
                cy += y;
                count++;
            }
        }
        if (count > 0) {
            cx /= count;
            cy /= count;
        }
        p.setLocation(cx, cy);
        return p;
    }

    /**
     * Return the centroid (averaged location) of a group of items.
     * @param iter an iterator of VisualItems
     * @return the centroid point. A new Point2D instance is allocated
     * and returned.
     */
    public static Point2D getCentroid(Iterator iter) {
        return getCentroid(iter, new Point2D.Double());
    }

    public static float[] getCentroid(float[] poly) {
        return getCentroid(poly, poly.length);
    }

    public static float[] getCentroid(float[] poly, int len) {
        float[] c = new float[]{0, 0};
        float x, y;
        int count = 0;
        for (int i = 0; i < len; i += 2) {
            x = poly[i];
            y = poly[i + 1];
            if (!(Double.isInfinite(x) || Double.isNaN(x))
                    && !(Double.isInfinite(y) || Double.isNaN(y))) {
                c[0] += x;
                c[1] += y;
                count++;
            }

        }
        if (count > 0) {
            c[0] /= count;
            c[1] /= count;
        }
        return c;
    }

    public static double getMaxDistanceFrom(float[] p, float[] poly) {
        double max = 0.0D;
        float x, y;
        for (int i = 0; i < poly.length; i += 2) {
            x = poly[i];
            y = poly[i + 1];
            if (!(Double.isInfinite(x) || Double.isNaN(x))
                    && !(Double.isInfinite(y) || Double.isNaN(y))) {
                double d = Point2D.distance(p[0], p[1], x, y);
                if (Double.compare(max, d) < 0) {
                    max = d;
                }
            }
        }
        return max;
    }

    public static double getMaxDistanceFrom(Point2D p, float[] poly) {
        double max = 0.0D;
        float x, y;
        for (int i = 0; i < poly.length; i += 2) {
            x = poly[i];
            y = poly[i + 1];
            if (!(Double.isInfinite(x) || Double.isNaN(x))
                    && !(Double.isInfinite(y) || Double.isNaN(y))) {
                double d = Point2D.distance(p.getX(), p.getY(), x, y);
                if (Double.compare(max, d) < 0) {
                    max = d;
                }
            }
        }
        return max;
    }

    /**
     * Set the display view such that the given bounds are within view.
     * @param display the Display instance
     * @param bounds the bounds that should be visible in the Display view
     * @param duration the duration of an animated transition. A value of
     * zero will result in an instantaneous change.
     */
    public static void fitViewToBounds(Display display, Rectangle2D bounds,
            long duration) {
        fitViewToBounds(display, bounds, null, duration);
    }

    /**
     * Set the display view such that the given bounds are within view, subject
     * to a given center point being maintained.
     * @param display the Display instance
     * @param bounds the bounds that should be visible in the Display view
     * @param center the point that should be the center of the Display
     * @param duration the duration of an animated transition. A value of
     * zero will result in an instantaneous change.
     */
    public static void fitViewToBounds(Display display, Rectangle2D bounds,
            Point2D center, long duration) {
        // init variables
        double w = display.getWidth(), h = display.getHeight();
        double cx = (center == null ? bounds.getCenterX() : center.getX());
        double cy = (center == null ? bounds.getCenterY() : center.getY());

        // compute half-widths of final bounding box around
        // the desired center point
        double wb = Math.max(cx - bounds.getMinX(),
                bounds.getMaxX() - cx);
        double hb = Math.max(cy - bounds.getMinY(),
                bounds.getMaxY() - cy);

        // compute scale factor
        //  - figure out if z or y dimension takes priority
        //  - then balance against the current scale factor
        double scale = Math.min(w / (2 * wb), h / (2 * hb)) / display.getScale();

        // animate to new display settings
        if (center == null) {
            center = new Point2D.Double(cx, cy);
        }
        if (duration > 0) {
            display.animatePanAndZoomToAbs(center, scale, duration);
        } else {
            display.panToAbs(center);
            display.zoomAbs(center, scale);
        }
    }

    public static Point2D getDisplayCenter(Display d) {
        c.setLocation(0, 0);
        c.setLocation(d.getWidth() / 2.0, d.getHeight() / 2.0);
        d.getInverseTransform().transform(c, c);
        return c;
    }
    private static Point2D c = new Point2D.Double();

} // end of class DisplayLib

