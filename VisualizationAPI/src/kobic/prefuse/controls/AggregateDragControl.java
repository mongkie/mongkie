/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse.controls;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;
    private Predicate filter;
    private String activity;

    /**
     * Creates a new drag control that issues repaint requests as an item
     * is dragged.
     */
    public AggregateDragControl() {
        this(null, null);
    }

    public AggregateDragControl(String activity) {
        this(null, activity);
    }

    public AggregateDragControl(Predicate filter) {
        this(filter, null);
    }

    public AggregateDragControl(Predicate filter, String activity) {
        this.filter = filter;
        this.activity = activity;
    }

    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display) e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if (!(item instanceof AggregateItem)) {
            if (isMultipleItemsSelected(item)) {
                setFocusedItemsFixed(item.getVisualization(), true);
            } else {
                setFixed(item, true);
            }
        }
        if (activity != null) {
            item.getVisualization().rerun(activity);
        }
    }

    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemExited(VisualItem item, MouseEvent e) {
        if (activeItem == item) {
            activeItem = null;
            if (isMultipleItemsSelected(item)) {
                setFocusedItemsFixed(item.getVisualization(), false);
            } else {
                setFixed(item, false);
            }
        }
        Display d = (Display) e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
        if (activity != null) {
            item.getVisualization().rerun(activity);
        }
    }

    /**
     * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemPressed(VisualItem item, MouseEvent e) {
        if (!isValid(item, e)) {
            return;
        }

        dragged = false;
        Display d = (Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if (item instanceof AggregateItem) {
            if (isMultipleItemsSelected(item)) {
                setFocusedItemsFixed(item.getVisualization(), true);
            } else {
                setFixed(item, true);
            }
        }
    }

    /**
     * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!isValid(item, e) || !dragged) {
            return;
        }

        activeItem = null;
        if (isMultipleItemsSelected(item)) {
            setFocusedItemsFixed(item.getVisualization(), false);
        } else {
            setFixed(item, false);
        }
        dragged = false;
    }

    /**
     * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    @Override
    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!isValid(item, e)) {
            return;
        }

        dragged = true;
        Display d = (Display) e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX() - down.getX();
        double dy = temp.getY() - down.getY();

        if (isMultipleItemsSelected(item)) {
            multipleItemsDragged(item, dx, dy);
        } else {
            singleItemDragged(item, dx, dy);
        }

        down.setLocation(temp);
        if (activity == null) {
            item.getVisualization().repaint();
        } else {
            item.getVisualization().rerun(activity);
        }
    }

    private boolean isMultipleItemsSelected(VisualItem item) {
        TupleSet focusedTupleSet = item.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
        return focusedTupleSet.getTupleCount() > 1 && focusedTupleSet.containsTuple(item);
    }

    protected boolean isValid(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e) || item instanceof EdgeItem
                || (filter != null && !filter.getBoolean(item))) {
            return false;
        }
        return true;
    }

    protected void setFixed(VisualItem item, boolean fixed) {
        if (item instanceof AggregateItem) {
            Iterator items = ((AggregateItem) item).items();
            while (items.hasNext()) {
                setFixed((VisualItem) items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }

    private void setFocusedItemsFixed(Visualization v, boolean fixed) {
        TupleSet focusedTupleSet = v.getFocusGroup(Visualization.FOCUS_ITEMS);
        for (Iterator<VisualItem> ftemIter = focusedTupleSet.tuples(); ftemIter.hasNext();) {
            setFixed(ftemIter.next(), fixed);
        }
    }

    protected void singleItemDragged(VisualItem item, double dx, double dy) {
        if (item instanceof AggregateItem) {
            for (Iterator<VisualItem> itemIter = ((AggregateItem) item).items(); itemIter.hasNext();) {
                move(itemIter.next(), dx, dy);
            }
        } else {
            move(item, dx, dy);
        }
    }

    protected void multipleItemsDragged(VisualItem item, double dx, double dy) {
        TupleSet focusedTupleSet = item.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
        for (Iterator<VisualItem> ftemIter = focusedTupleSet.tuples(); ftemIter.hasNext();) {
            VisualItem ftem = ftemIter.next();
            if (ftem instanceof AggregateItem) {
                for (Iterator<VisualItem> atemIter = ((AggregateItem) ftem).items(); atemIter.hasNext();) {
                    VisualItem atem = atemIter.next();
                    if (!focusedTupleSet.containsTuple(atem)) {
                        move(atem, dx, dy);
                    }
                }
            } else {
                move(ftem, dx, dy);
            }
        }
    }

    private void move(VisualItem item, double dx, double dy) {
        if (filter == null || filter.getBoolean(item)) {
            double x = item.getEndX();
            double y = item.getEndY();
            item.setStartX(x);
            item.setStartY(y);
            item.setEndX(x + dx);
            item.setEndY(y + dy);
            item.setX(x + dx);
            item.setY(y + dy);
        }
    }
}
