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
package kobic.prefuse.action.layout;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.Predicate;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SubNodesCircleLayout extends Layout {

    protected double radius;
    protected final Predicate pSuperNode;
    protected final String corespondingNodeField;
    private boolean pie;

    public SubNodesCircleLayout(double radius, final String corespondingNodeField) {
        this(radius, corespondingNodeField, false);
    }

    public SubNodesCircleLayout(double radius, final String corespondingNodeField, boolean pie) {
        super(Visualization.AGGR_ITEMS);
        this.radius = radius;
        this.corespondingNodeField = corespondingNodeField;
        this.pSuperNode = new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                return t.get(corespondingNodeField) != null;
            }
        };
        this.pie = pie;
    }

    public boolean isPie() {
        return pie;
    }

    public void setPie(boolean pie) {
        this.pie = pie;
    }

    /**
     * Return the radius of the layout circle.
     * @return the circle radius
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Set the radius of the layout circle.
     * @param radius the circle radius to use
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @see prefuse.action.Action#run(double)
     */
    @Override
    public void run(double frac) {
        Iterator<AggregateItem> superAggrItems = ((AggregateTable) m_vis.getVisualGroup(m_group)).tuples(pSuperNode);
        while (superAggrItems.hasNext()) {
            AggregateItem superAggr = superAggrItems.next();
            NodeItem superNode = (NodeItem) superAggr.get(corespondingNodeField);
            Rectangle2D r = superNode.getBounds();
            double height = r.getHeight();
            double width = r.getWidth();
            double cx = r.getCenterX();
            double cy = r.getCenterY();
            double _radius = radius;
            if (_radius <= 0) {
                _radius = 0.45 * (height < width ? height : width);
            }
            if (pie) {
                Iterator<VisualItem> items = superAggr.items();
                double totalSize = 0;
                while (items.hasNext()) {
                    totalSize += items.next().getSize();
                }
                totalSize *= 2;
                items = superAggr.items();
                double prevSize = 0;
                double relativeSize = 0;
                while (items.hasNext()) {
                    VisualItem item = items.next();
                    relativeSize += (prevSize + item.getSize());
                    prevSize = item.getSize();
                    double angle = (2 * Math.PI * relativeSize) / totalSize;
                    double x = Math.cos(angle) * _radius + cx;
                    double y = Math.sin(angle) * _radius + cy;
                    setX(item, null, x);
                    setY(item, null, y);
                }
            } else {
                int nn = superAggr.getAggregateSize();
                Iterator<VisualItem> items = superAggr.items();
                for (int i = 0; items.hasNext(); i++) {
                    VisualItem n = items.next();
                    double angle = (2 * Math.PI * i) / nn;
                    double x = Math.cos(angle) * _radius + cx;
                    double y = Math.sin(angle) * _radius + cy;
                    setX(n, null, x);
                    setY(n, null, y);
                }
            }
        }
    }
}
