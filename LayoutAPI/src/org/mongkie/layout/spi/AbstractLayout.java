/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.layout.spi;

import java.awt.Insets;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import org.mongkie.layout.LayoutController;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.openide.util.Lookup;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class AbstractLayout implements Layout {

    private final LayoutBuilder<? extends Layout> builder;
    protected MongkieDisplay display;
    protected LayoutProperty[] _properties;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected AbstractLayout(LayoutBuilder<? extends Layout> builder) {
        this.builder = builder;
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected final void firePropertyChange(String name, Object o, Object n) {
        pcs.firePropertyChange(name, o, n);
    }

    @Override
    public void setDisplay(MongkieDisplay d) {
        this.display = d;
    }

    @Override
    public final LayoutProperty[] getProperties() {
        if (_properties == null) {
            _properties = createProperties();
        }
        return _properties;
    }

    protected abstract LayoutProperty[] createProperties();

    @Override
    public final void resetPropertyValues() {
        resetProperties();
        firePropertyChange("resetPropertyValues", null, this);
    }

    protected abstract void resetProperties();

    @Override
    public final LayoutBuilder<? extends Layout> getBuilder() {
        return builder;
    }

    @Override
    public final void initAlgo() {
        prefuseLayoutEnabled = display.isLayoutActionEnabled();
        display.setLayoutActionEnabled(false);
        canceled = false;
        step = 0;
        setEnabled(!isSelectionOnly() || isEnabledOnSelectionOnly());
        prepare();
    }
    private boolean prefuseLayoutEnabled;
    private int step;

    protected boolean isEnabledOnSelectionOnly() {
        return getSelectedNodes().hasNext();
    }
    private boolean enabled = true;

    protected boolean isEnabled() {
        return enabled;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected abstract void prepare();

    @Override
    public final void goAlgo() {
        run(++step);
    }

    protected abstract void run(int step);

    @Override
    public final void endAlgo() {
        display.setLayoutActionEnabled(prefuseLayoutEnabled);
        finish(canceled);
    }

    protected abstract void finish(boolean canceled);

    @Override
    public final boolean hasNextStep() {
        return isEnabled() && !canceled && more();
    }

    protected abstract boolean more();

    @Override
    public final boolean cancelAlgo() {
        canceled = true;
        return true;
    }
    private volatile boolean canceled;

    protected final boolean isSelectionOnly() {
        return supportsSelectionOnly()
                && Lookup.getDefault().lookup(LayoutController.class).getModel().isSelectionOnly(this);
    }

    protected final Iterator<NodeItem> getSelectedNodes() {
        return Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedNodes(display.getVisualization());
    }

    protected final TupleSet getSelectedItems() {
        return Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedItems(display.getVisualization());
    }

    protected TupleSet getLayoutTargetNodes() {
        return isSelectionOnly() ? getSelectedItems() : display.getVisualGraph().getNodes();
    }

    protected void setX(VisualItem item, double x) {
        PrefuseLib.setX(item, null, x);
    }

    protected void setY(VisualItem item, double y) {
        PrefuseLib.setY(item, null, y);
    }
    // Copied from prefuse.action.layout.Layout
    /**
     * The explicitly set layout bounds. May be null.
     */
    private Rectangle2D m_bounds = null;
    /**
     * The explicitly set anchor point at which the layout can
     * be centered or rooted. May be null.
     */
    private Point2D m_anchor = null;
    private boolean m_margin = false;
    private Insets m_insets = new Insets(0, 0, 0, 0);
    private double[] m_bpts = new double[4];
    private Rectangle2D m_tmpb = new Rectangle2D.Double();
    private Point2D m_tmpa = new Point2D.Double();

    /**
     * Set the margins the layout should observe within its layout bounds.
     *
     * @param top    the top margin, in pixels
     * @param left   the left margin, in pixels
     * @param bottom the bottom margin, in pixels
     * @param right  the right margin, in pixels
     */
    public void setMargin(int top, int left, int bottom, int right) {
        m_insets.top = top;
        m_insets.left = left;
        m_insets.bottom = bottom;
        m_insets.right = right;
        m_margin = true;
    }

    /**
     * Returns the bounds in which the layout should be computed. If the
     * bounds have been explicitly set, that value is used. Otherwise,
     * an attempt is made to compute the bounds based upon the display
     * region of the first display found in this action's associated
     * Visualization.
     *
     * @return the layout bounds within which to contain the layout.
     */
    public Rectangle2D getLayoutBounds() {
        if (m_bounds != null) {
            return m_bounds;
        }
        Insets i = m_margin ? m_insets : display.getInsets(m_insets);
        m_bpts[0] = i.left;
        m_bpts[1] = i.top;
        m_bpts[2] = display.getWidth() - i.right;
        m_bpts[3] = display.getHeight() - i.bottom;
        display.getInverseTransform().transform(m_bpts, 0, m_bpts, 0, 2);
        m_tmpb.setRect(m_bpts[0], m_bpts[1],
                m_bpts[2] - m_bpts[0],
                m_bpts[3] - m_bpts[1]);
        return m_tmpb;
    }

    /**
     * Explicitly set the layout bounds. A reference to the input rectangle
     * instance is maintained, not a copy, and so any subsequent changes to
     * the rectangle object will also change the layout bounds.
     *
     * @param b a rectangle specifying the layout bounds. A reference to this
     *          same instance is kept.
     */
    public void setLayoutBounds(Rectangle2D b) {
        m_bounds = b;
    }

    /**
     * Return the layout anchor at which to center or root the layout. How this
     * point is used (if it is used at all) is dependent on the particular
     * Layout implementation. If no anchor point has been explicitly set, the
     * center coordinate for the first display found in this action's
     * associated Visualization is used, if available.
     *
     * @return the layout anchor point.
     */
    public Point2D getLayoutAnchor() {
        if (m_anchor != null) {
            return m_anchor;
        }

        m_tmpa.setLocation(0, 0);
        m_tmpa.setLocation(display.getWidth() / 2.0, display.getHeight() / 2.0);
        display.getInverseTransform().transform(m_tmpa, m_tmpa);
        return m_tmpa;
    }

    /**
     * Explicitly set the layout anchor point. The provided object will be
     * used directly (rather than copying its values), so subsequent
     * changes to that point object will change the layout anchor.
     *
     * @param a the layout anchor point to use
     */
    public void setLayoutAnchor(Point2D a) {
        m_anchor = a;
    }

    /**
     * Convenience method for setting an x-coordinate. The start value of the
     * x-coordinate will be set to the current value, and the current and end
     * values will be set to the provided x-coordinate. If the current value
     * is not a number (NaN), the x-coordinate of the provided referrer
     * item (if non null) will be used to set the start coordinate.
     *
     * @param item     the item to set
     * @param referrer the referrer item to use for the start location if
     *                 the current value is not a number (NaN)
     * @param x        the x-coordinate value to set. This will be set for both
     *                 the current and end values.
     * @see prefuse.util.PrefuseLib#setX(VisualItem, VisualItem, double)
     */
    public void setX(VisualItem item, VisualItem referrer, double x) {
        PrefuseLib.setX(item, referrer, x);
    }

    /**
     * Convenience method for setting an y-coordinate. The start value of the
     * y-coordinate will be set to the current value, and the current and end
     * values will be set to the provided y-coordinate. If the current value
     * is not a number (NaN), the y-coordinate of the provided referrer
     * item (if non null) will be used to set the start coordinate.
     *
     * @param item     the item to set
     * @param referrer the referrer item to use for the start location if
     *                 the current value is not a number (NaN)
     * @param y        the y-coordinate value to set. This will be set for both
     *                 the current and end values.
     * @see prefuse.util.PrefuseLib#setY(VisualItem, VisualItem, double)
     */
    public void setY(VisualItem item, VisualItem referrer, double y) {
        PrefuseLib.setY(item, referrer, y);
    }
}
