package prefuse.render;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.*;
import java.util.Iterator;
import prefuse.Constants;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.StrokeLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * <p>Renderer that draws edges as lines connecting nodes. Both
 * straight and curved lines are supported. Curved lines are drawn using
 * cubic Bezier curves. Subclasses can override the
 * {@link #getCurveControlPoints(EdgeItem, Point2D[], double, double, double, double)}
 * method to provide custom control point assignment for such curves.</p>
 *
 * <p>This class also supports arrows for directed edges. See the
 * {@link #setArrowType(int)} method for more.</p>
 *
 * @version 1.0
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class EdgeRenderer extends AbstractShapeRenderer {

    public static final String EDGE_TYPE = "edgeType";
    protected static final double HALF_PI = Math.PI / 2;
    protected Line2D m_line = new Line2D.Float();
    protected QuadCurve2D m_quad = new QuadCurve2D.Float();
    protected int m_edgeType = Constants.EDGE_TYPE_LINE;
    protected int m_xAlign1 = Constants.CENTER;
    protected int m_yAlign1 = Constants.CENTER;
    protected int m_xAlign2 = Constants.CENTER;
    protected int m_yAlign2 = Constants.CENTER;
    protected double m_width = 2;
    protected float m_curWidth = 2;
    protected Point2D m_tmpPoints[] = new Point2D[2];
    protected Point2D m_ctrlPoints[] = new Point2D[1];
    protected Point2D m_isctPoints[] = new Point2D[2];
    // arrow head handling
    protected int m_edgeArrow = Constants.EDGE_ARROW_FORWARD;
    private int m_arrowWidth = 8;
    private int m_arrowHeight = 12;
    protected Polygon m_arrowHead = updateArrowHead(
            m_arrowWidth, m_arrowHeight);
    protected AffineTransform m_arrowTrans = new AffineTransform();
    protected Shape m_curArrow;
    protected boolean edgeBounded = false;
    protected boolean multipleEdge = false;
    protected boolean lineForSingleEdge = true;

    /**
     * Create a new EdgeRenderer.
     */
    public EdgeRenderer() {
        m_tmpPoints[0] = new Point2D.Float();
        m_tmpPoints[1] = new Point2D.Float();
        m_ctrlPoints[0] = new Point2D.Float();
//        m_ctrlPoints[1] = new Point2D.Float();
        m_isctPoints[0] = new Point2D.Float();
        m_isctPoints[1] = new Point2D.Float();
    }

    public EdgeRenderer(boolean edgeBounded, boolean multipleEdge) {
        this();
        this.edgeBounded = edgeBounded;
        setMultipleEdge(multipleEdge);
    }

    public boolean isEdgeBounded() {
        return edgeBounded;
    }

    public void setEdgeBounded(boolean edgeBounded) {
        this.edgeBounded = edgeBounded;
    }

    public boolean isMultipleEdge() {
        return multipleEdge;
    }

    private void setMultipleEdge(boolean multipleEdge) {
        this.multipleEdge = multipleEdge;
        if (multipleEdge) {
            setEdgeType(Constants.EDGE_TYPE_CURVE);
        }
    }

    public boolean isLineForSingleEdge() {
        return lineForSingleEdge;
    }

    public void setLineForSingleEdge(boolean lineForSingleEdge) {
        this.lineForSingleEdge = lineForSingleEdge;
    }

    /**
     * Create a new EdgeRenderer with the given edge type.
     *
     * @param edgeType the edge type, one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     */
    public EdgeRenderer(int edgeType) {
        this(edgeType, Constants.EDGE_ARROW_FORWARD);
    }

    /**
     * Create a new EdgeRenderer with the given edge and arrow types.
     *
     * @param edgeType  the edge type, one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     * @param arrowType the arrow type, one of
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or
     * {@link prefuse.Constants#EDGE_ARROW_NONE}.
     * @see #setArrowType(int)
     */
    public EdgeRenderer(int edgeType, int arrowType) {
        this();
        setEdgeType(edgeType);
        setArrowType(arrowType);
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRenderType(prefuse.visual.VisualItem)
     */
    @Override
    public int getRenderType(VisualItem item) {
        return RENDER_TYPE_DRAW;
    }

    protected VisualItem getSourceItem(EdgeItem e, boolean forward) {
        VisualItem source = forward ? e.getSourceItem() : e.getTargetItem();
        return source;
    }

    protected VisualItem getTargetItem(EdgeItem e, boolean forward) {
        VisualItem target = forward ? e.getTargetItem() : e.getSourceItem();
        return target;
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getRawShape(prefuse.visual.VisualItem)
     */
    @Override
    protected Shape getRawShape(VisualItem item) {
        EdgeItem e = (EdgeItem) item;
        VisualItem source = e.getSourceItem();
        VisualItem target = e.getTargetItem();

        int type = m_edgeType;
        int edgeIndex = -1;

        getAlignedPoint(m_tmpPoints[0], source.getBounds(),
                m_xAlign1, m_yAlign1);
        getAlignedPoint(m_tmpPoints[1], target.getBounds(),
                m_xAlign2, m_yAlign2);
        m_curWidth = (float) (m_width * getLineWidth(item));

        boolean curveCtrlPtsReady = false;
        Polygon arrowHead;
        // create the arrow head, if needed
        if (isDirected(e) && m_edgeArrow != Constants.EDGE_ARROW_NONE && (arrowHead = getArrowHead(e)) != null) {
            // get starting and ending edge endpoints
            boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
            Point2D start = null, end = null;
            start = m_tmpPoints[forward ? 0 : 1];
            end = m_tmpPoints[forward ? 1 : 0];
            // compute the intersection with the source/target bounding box
            VisualItem src = getSourceItem(e, forward);
            VisualItem dest = getTargetItem(e, forward);

            if (edgeBounded && !e.isAggregating()) {
                adjustEndingPoint(start, end, src, dest);
            } else if (GraphicsLib.intersectLineRectangle(
                    start, end, dest.getBounds(), m_isctPoints) > 0) {
                end.setLocation(m_isctPoints[0]);
            }

            // create the arrow head shape
            AffineTransform arrowTrans;
            if (multipleEdge) {
                edgeIndex = adjustEndingPointByMultipleEdge(e, start, end);
                if (lineForSingleEdge && edgeIndex < 0) {
                    type = Constants.EDGE_TYPE_LINE;
                    arrowTrans = getArrowTrans(start, end, m_curWidth);
                } else {
                    getCurveControlPoints(edgeIndex, m_ctrlPoints, start.getX(), start.getY(), end.getX(), end.getY());
                    curveCtrlPtsReady = true;
                    arrowTrans = getArrowTrans(m_ctrlPoints[0], end, m_curWidth);
                }
            } else {
                arrowTrans = getArrowTrans(start, end, m_curWidth);
            }
            m_curArrow = arrowTrans.createTransformedShape(arrowHead);

            // update the endpoints for the edge shape
            // need to bias this by arrow head size
            adjustLineEndByArrowHead(e, end);
            arrowTrans.transform(end, end);
        } else {
            m_curArrow = null;
            if (edgeBounded && !e.isAggregating()) {
                adjustEndingPoint(m_tmpPoints[0], m_tmpPoints[1], source, target);
            }
            if (multipleEdge) {
                edgeIndex = adjustEndingPointByMultipleEdge(e, m_tmpPoints[0], m_tmpPoints[1]);
                if (lineForSingleEdge && edgeIndex < 0) {
                    type = Constants.EDGE_TYPE_LINE;
                } else {
                    getCurveControlPoints(edgeIndex, m_ctrlPoints, m_tmpPoints[0].getX(), m_tmpPoints[0].getY(), m_tmpPoints[1].getX(), m_tmpPoints[1].getY());
                    curveCtrlPtsReady = true;
                }
            }
        }

        // create the edge shape
        Shape shape = null;
        double sx = m_tmpPoints[0].getX();
        double sy = m_tmpPoints[0].getY();
        double ex = m_tmpPoints[1].getX();
        double ey = m_tmpPoints[1].getY();
        switch (type) {
            case Constants.EDGE_TYPE_LINE:
                m_line.setLine(sx, sy, ex, ey);
                shape = m_line;
                break;
            case Constants.EDGE_TYPE_CURVE:
                if (!curveCtrlPtsReady) {
                    getCurveControlPoints(e, m_ctrlPoints, sx, sy, ex, ey);
                }
                m_quad.setCurve(sx, sy,
                        m_ctrlPoints[0].getX(), m_ctrlPoints[0].getY(),
                        ex, ey);
                shape = m_quad;
                break;
            default:
                throw new IllegalStateException("Unknown edge type");
        }

        // return the edge shape
        return shape;
    }

    protected boolean isIgnored(EdgeItem e) {
        return e.isAggregating();
    }

    //TODO: cache required
    protected int getEdgeIndex(EdgeItem e) {
        if (isIgnored(e)) {
            return -1;
        }

        NodeItem sourceItem = e.getSourceItem();
        NodeItem targetItem = e.getTargetItem();
        Iterator<EdgeItem> edges = sourceItem.edges();

        // number of equal edges = same target and source
        int equalEdges = 0;
        // number of nearequal edges = same nodes, but any order target and source
        int sameEdges = 0;
        int edgeIndex = 0;
        int row = e.getRow();
        while (edges.hasNext()) {
            EdgeItem edge = edges.next();
            int edgeRow = edge.getRow();
            if (isIgnored(edge)) {
                continue;
            }
            if (edge.getSourceItem() == sourceItem && edge.getTargetItem() == targetItem) {
                if (row == edgeRow) {
                    edgeIndex = equalEdges;
                }
                equalEdges++;
                sameEdges++;
            } else if (edge.getSourceItem() == targetItem && edge.getTargetItem() == sourceItem) {
                sameEdges++;
            }
        }

        // draw the line straight if we found one edge
        if (lineForSingleEdge && edgeIndex == 0 && sameEdges == 1) {
            return -1;
        } else if (sameEdges > 1) {
            edgeIndex++;
        }

        return edgeIndex;
    }
    protected AffineTransform m_edgeTrans = new AffineTransform();

    protected int adjustEndingPointByMultipleEdge(EdgeItem e, Point2D start, Point2D end) {
        int edgeIndex = getEdgeIndex(e);
        if (edgeIndex < 0) {
            return edgeIndex;
        }
        m_edgeTrans.setToTranslation(end.getX(), end.getY());
        m_edgeTrans.rotate(-HALF_PI
                + Math.atan2(end.getY() - start.getY(), end.getX() - start.getX()));
        if (edgeIndex % 2 == 1) {
            end.setLocation(4 + edgeIndex, 0);
        } else {
            end.setLocation(-4 - (edgeIndex - 1), 0);
        }
        m_edgeTrans.transform(end, end);
        return edgeIndex;
    }

    protected void adjustLineEndByArrowHead(EdgeItem e, Point2D end) {
        end.setLocation(0, -getArrowHeadHeight());
    }

    protected Polygon getArrowHead(EdgeItem e) {
        return m_arrowHead;
    }

    protected boolean isDirected(EdgeItem e) {
        return e.isDirected();
    }

    private void adjustEndingPoint(Point2D start, Point2D end, VisualItem source, VisualItem target) {
        Rectangle2D sourceBounds = source.getBounds();
        Rectangle2D targetBounds = target.getBounds();
        if (source.getShape() == Constants.SHAPE_ELLIPSE) {
            m_edgeTrans.setToTranslation(start.getX(), start.getY());
            m_edgeTrans.rotate(-HALF_PI
                    + Math.atan2(start.getY() - end.getY(), start.getX() - end.getX()));
            start.setLocation(0, -sourceBounds.getWidth() / 2.0D);
            m_edgeTrans.transform(start, start);
        } else if (GraphicsLib.intersectLineRectangle(start, end, sourceBounds, m_isctPoints) > 0) {
            start.setLocation(m_isctPoints[0]);
        }
        if (target.getShape() == Constants.SHAPE_ELLIPSE) {
            m_edgeTrans.setToTranslation(end.getX(), end.getY());
            m_edgeTrans.rotate(-HALF_PI
                    + Math.atan2(end.getY() - start.getY(), end.getX() - start.getX()));
            end.setLocation(0, -targetBounds.getWidth() / 2.0D);
            m_edgeTrans.transform(end, end);
        } else if (GraphicsLib.intersectLineRectangle(start, end, targetBounds, m_isctPoints) > 0) {
            end.setLocation(m_isctPoints[0]);
        }
    }

    /**
     * @see prefuse.render.Renderer#render(java.awt.Graphics2D, prefuse.visual.VisualItem)
     */
    @Override
    public void render(Graphics2D g, VisualItem item) {
        // render the edge line
        super.render(g, item);
        // render the edge arrow head, if appropriate
        if (m_curArrow != null) {
            g.setPaint(ColorLib.getColor(item.getFillColor()));
            g.fill(m_curArrow);
        }
    }

    /**
     * Returns an affine transformation that maps the arrowhead shape
     * to the position and orientation specified by the provided
     * line segment end points.
     */
    protected AffineTransform getArrowTrans(Point2D start, Point2D end, double width) {
//        m_arrowTrans.setToTranslation(end.getX(), end.getY());
//        m_arrowTrans.rotate(-HALF_PI + Math.atan2(end.getY() - start.getY(), end.getX() - start.getX()));
//        if (width > m_width) {
//            double scalar = width / 3.0d;
//            m_arrowTrans.scale(scalar, scalar);
//        }
//        return m_arrowTrans;
        return getArrowTrans(start.getX(), start.getY(), end.getX(), end.getY(), width, 0);
    }

    protected AffineTransform getArrowTrans(double sx, double sy, double ex, double ey, double width) {
        return getArrowTrans(sx, sy, ex, ey, width, 0);
    }

    protected AffineTransform getArrowTrans(double sx, double sy, double ex, double ey, double width, double theta) {
        m_arrowTrans.setToTranslation(ex, ey);
        m_arrowTrans.rotate(-HALF_PI + theta + Math.atan2(ey - sy, ex - sx));
        if (width > m_width) {
            double scalar = width / 3;
            m_arrowTrans.scale(scalar, scalar);
        }
        return m_arrowTrans;
    }

    /**
     * Update the dimensions of the arrow head, creating a new
     * arrow head if necessary. The return value is also set
     * as the member variable
     * <code>m_arrowHead</code>
     *
     * @param w the width of the untransformed arrow head base, in pixels
     * @param h the height of the untransformed arrow head, in pixels
     * @return the untransformed arrow head shape
     */
    private Polygon updateArrowHead(int w, int h) {
        if (m_arrowHead == null) {
            m_arrowHead = new Polygon();
        } else {
            m_arrowHead.reset();
        }
        m_arrowHead.addPoint(0, 0);
        m_arrowHead.addPoint(-w / 2, -h);
        m_arrowHead.addPoint(w / 2, -h);
        m_arrowHead.addPoint(0, 0);
        return m_arrowHead;
    }

    /**
     * @see prefuse.render.AbstractShapeRenderer#getTransform(prefuse.visual.VisualItem)
     */
    @Override
    protected AffineTransform getTransform(VisualItem item) {
        return null;
    }

    /**
     * @see prefuse.render.Renderer#locatePoint(java.awt.geom.Point2D, prefuse.visual.VisualItem)
     */
    @Override
    public boolean locatePoint(Point2D p, VisualItem item) {
        Shape s = getShape(item);
        if (s == null) {
            return false;
        } else {
            double width = Math.max(2, getLineWidth(item));
            double halfWidth = width / 2.0;
            return s.intersects(p.getX() - halfWidth,
                    p.getY() - halfWidth,
                    width, width);
        }
    }

    /**
     * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
     */
    @Override
    public void setBounds(VisualItem item) {
        if (!m_manageBounds) {
            return;
        }
        Shape shape = getShape(item);
        if (shape == null) {
            item.setBounds(item.getX(), item.getY(), 0, 0);
            return;
        }
//        GraphicsLib.setBounds(item, shape, getStroke(item));
//        if (m_curArrow != null) {
//            Rectangle2D bbox = (Rectangle2D) item.get(VisualItem.BOUNDS);
//            Rectangle2D.union(bbox, m_curArrow.getBounds2D(), bbox);
//        }
        GraphicsLib.getBounds(m_tmpBounds, shape, getStroke(item));
        if (m_curArrow != null) {
            Rectangle2D.union(m_tmpBounds, m_curArrow.getBounds2D(), m_tmpBounds);
        }
        item.setBounds(m_tmpBounds.getX(), m_tmpBounds.getY(), m_tmpBounds.getWidth(), m_tmpBounds.getHeight());
    }
    private final Rectangle2D m_tmpBounds = new Rectangle2D.Double();

    /**
     * Returns the line width to be used for this VisualItem. By default,
     * returns the base width value set using the {@link #setDefaultLineWidth(double)}
     * method, scaled by the item size returned by
     * {@link VisualItem#getSize()}. Subclasses can override this method to
     * perform custom line width determination, however, the preferred
     * method is to change the item size value itself.
     *
     * @param item the VisualItem for which to determine the line width
     * @return the desired line width, in pixels
     */
    protected double getLineWidth(VisualItem item) {
        return item.getSize();
    }

    /**
     * Returns the stroke value returned by {@link VisualItem#getStroke()},
     * scaled by the current line width
     * determined by the {@link #getLineWidth(VisualItem)} method. Subclasses
     * may override this method to perform custom stroke assignment, but should
     * respect the line width paremeter stored in the {@link #m_curWidth}
     * member variable, which caches the result of
     * <code>getLineWidth</code>.
     *
     * @see prefuse.render.AbstractShapeRenderer#getStroke(prefuse.visual.VisualItem)
     */
    @Override
    protected BasicStroke getStroke(VisualItem item) {
        return StrokeLib.getDerivedStroke(item.getStroke(), m_curWidth);
    }

    /**
     * Determines the control points to use for cubic (Bezier) curve edges.
     * Override this method to provide custom curve specifications.
     * To reduce object initialization, the entries of the Point2D array are
     * already initialized, so use the <tt>Point2D.setLocation()</tt> method rather than
     * <tt>new Point2D.Double()</tt> to more efficiently set custom control points.
     *
     * @param eitem the EdgeItem we are determining the control points for
     * @param cp    array of Point2D's (length >= 2) in which to return the control points
     * @param x1    the x co-ordinate of the first node this edge connects to
     * @param y1    the y co-ordinate of the first node this edge connects to
     * @param x2    the x co-ordinate of the second node this edge connects to
     * @param y2    the y co-ordinate of the second node this edge connects to
     */
    protected void getCurveControlPoints(EdgeItem eitem, Point2D[] cp,
            double x1, double y1, double x2, double y2) {
//        double dx = x2 - x1, dy = y2 - y1;
//        cp[0].setLocation(x1 + 2 * dx / 3, y1);
//        cp[1].setLocation(x2 - dx / 8, y2 - dy / 8);

        double d = Point2D.distance(x1, y1, x2, y2);
        cp[0].setLocation(d / 10, -d / 2);
//        cp[0].setLocation(-d / 10, -d / 2);
        m_edgeTrans.setToTranslation(x2, y2);
        m_edgeTrans.rotate(-HALF_PI
                + Math.atan2(y2 - y1, x2 - x1));
        m_edgeTrans.transform(cp[0], cp[0]);
//        cp[1].setLocation(x2, y2);
    }

    protected void getCurveControlPoints(int edgeIndex, Point2D[] cp,
            double x1, double y1, double x2, double y2) {
        if (edgeIndex < 0) {
            // draw the line straight if we found one edge
//            cp[0].setLocation(x2, y2);
//            cp[1].setLocation(x1, y1);
            return;
        }

        double d = Point2D.distance(x1, y1, x2, y2);
        if (edgeIndex % 2 == 1) {
            cp[0].setLocation(d / 10 * edgeIndex, -d / 2);
        } else {
            cp[0].setLocation(-d / 10 * (edgeIndex - 1), -d / 2);
        }
        m_edgeTrans.setToTranslation(x2, y2);
        m_edgeTrans.rotate(-HALF_PI
                + Math.atan2(y2 - y1, x2 - x1));
        m_edgeTrans.transform(cp[0], cp[0]);
//        cp[1].setLocation(x2, y2);
    }

    /**
     * Helper method, which calculates the top-left co-ordinate of a rectangle
     * given the rectangle's alignment.
     */
    protected static void getAlignedPoint(Point2D p, Rectangle2D r, int xAlign, int yAlign) {
        double x = r.getX(), y = r.getY(), w = r.getWidth(), h = r.getHeight();
        if (xAlign == Constants.CENTER) {
            x = x + (w / 2);
        } else if (xAlign == Constants.RIGHT) {
            x = x + w;
        }
        if (yAlign == Constants.CENTER) {
            y = y + (h / 2);
        } else if (yAlign == Constants.BOTTOM) {
            y = y + h;
        }
        p.setLocation(x, y);
    }

    /**
     * Returns the type of the drawn edge. This is one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     *
     * @return the edge type
     */
    public int getEdgeType() {
        return m_edgeType;
    }

    /**
     * Sets the type of the drawn edge. This must be one of
     * {@link prefuse.Constants#EDGE_TYPE_LINE} or
     * {@link prefuse.Constants#EDGE_TYPE_CURVE}.
     *
     * @param type the new edge type
     */
    public void setEdgeType(int type) {
        if (type < 0 || type >= Constants.EDGE_TYPE_COUNT) {
            throw new IllegalArgumentException(
                    "Unrecognized edge curve type: " + type);
        }
        m_edgeType = type;
    }

    /**
     * Returns the type of the drawn edge. This is one of
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD},
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE}, or
     * {@link prefuse.Constants#EDGE_ARROW_NONE}.
     *
     * @return the edge type
     */
    public int getArrowType() {
        return m_edgeArrow;
    }

    /**
     * Sets the type of the drawn edge. This is either
     * {@link prefuse.Constants#EDGE_ARROW_NONE} for no edge arrows,
     * {@link prefuse.Constants#EDGE_ARROW_FORWARD} for arrows from source to
     * target on directed edges, or
     * {@link prefuse.Constants#EDGE_ARROW_REVERSE} for arrows from target to
     * source on directed edges.
     *
     * @param type the new arrow type
     */
    public void setArrowType(int type) {
        if (type < 0 || type >= Constants.EDGE_ARROW_COUNT) {
            throw new IllegalArgumentException(
                    "Unrecognized edge arrow type: " + type);
        }
        m_edgeArrow = type;
    }

    /**
     * Sets the dimensions of an arrow head for a directed edge. This specifies
     * the pixel dimensions when both the zoom level and the size factor
     * (a combination of item size value and default stroke width) are 1.0.
     *
     * @param width  the untransformed arrow head width, in pixels. This
     * specifies the span of the base of the arrow head.
     * @param height the untransformed arrow head height, in pixels. This
     * specifies the distance from the point of the arrow to its base.
     */
    protected void setArrowHeadSize(int width, int height) {
        m_arrowWidth = width;
        m_arrowHeight = height;
        m_arrowHead = updateArrowHead(width, height);
    }

    /**
     * Get the height of the untransformed arrow head. This is the distance,
     * in pixels, from the tip of the arrow to its base.
     *
     * @return the default arrow head height
     */
    protected int getArrowHeadHeight() {
        return m_arrowHeight;
    }

    /**
     * Get the width of the untransformed arrow head. This is the length,
     * in pixels, of the base of the arrow head.
     *
     * @return the default arrow head width
     */
    protected int getArrowHeadWidth() {
        return m_arrowWidth;
    }

    /**
     * Get the horizontal aligment of the edge mount point with the first node.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT},
     * {@link prefuse.Constants#RIGHT}, or {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment1() {
        return m_xAlign1;
    }

    /**
     * Get the vertical aligment of the edge mount point with the first node.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP},
     * {@link prefuse.Constants#BOTTOM}, or {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment1() {
        return m_yAlign1;
    }

    /**
     * Get the horizontal aligment of the edge mount point with the second
     * node.
     *
     * @return the horizontal alignment, one of {@link prefuse.Constants#LEFT},
     * {@link prefuse.Constants#RIGHT}, or {@link prefuse.Constants#CENTER}.
     */
    public int getHorizontalAlignment2() {
        return m_xAlign2;
    }

    /**
     * Get the vertical aligment of the edge mount point with the second node.
     *
     * @return the vertical alignment, one of {@link prefuse.Constants#TOP},
     * {@link prefuse.Constants#BOTTOM}, or {@link prefuse.Constants#CENTER}.
     */
    public int getVerticalAlignment2() {
        return m_yAlign2;
    }

    /**
     * Set the horizontal aligment of the edge mount point with the first node.
     *
     * @param align the horizontal alignment, one of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment1(int align) {
        m_xAlign1 = align;
    }

    /**
     * Set the vertical aligment of the edge mount point with the first node.
     *
     * @param align the vertical alignment, one of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment1(int align) {
        m_yAlign1 = align;
    }

    /**
     * Set the horizontal aligment of the edge mount point with the second
     * node.
     *
     * @param align the horizontal alignment, one of
     * {@link prefuse.Constants#LEFT}, {@link prefuse.Constants#RIGHT}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public void setHorizontalAlignment2(int align) {
        m_xAlign2 = align;
    }

    /**
     * Set the vertical aligment of the edge mount point with the second node.
     *
     * @param align the vertical alignment, one of
     * {@link prefuse.Constants#TOP}, {@link prefuse.Constants#BOTTOM}, or
     * {@link prefuse.Constants#CENTER}.
     */
    public void setVerticalAlignment2(int align) {
        m_yAlign2 = align;
    }

    /**
     * Sets the default width of lines. This width value will
     * be scaled by the value of an item's size data field. The default
     * base width is 1.
     *
     * @param w the desired default line width, in pixels
     */
    public void setDefaultLineWidth(double w) {
        m_width = w;
    }

    /**
     * Gets the default width of lines. This width value that will
     * be scaled by the value of an item's size data field. The default
     * base width is 1.
     *
     * @return the default line width, in pixels
     */
    public double getDefaultLineWidth() {
        return m_width;
    }
} // end of class EdgeRenderer

