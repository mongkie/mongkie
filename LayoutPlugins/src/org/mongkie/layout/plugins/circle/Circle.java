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
package org.mongkie.layout.plugins.circle;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import org.openide.util.Exceptions;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.SortedTupleIterator;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class Circle extends PrefuseLayout {

    private double radius = 200D;
    private boolean autoScale = true;
    private LayoutProperty radiusProperty;

    Circle(CircleLayoutBuilder builder) {
        super(builder);
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        boolean o = this.autoScale;
        this.autoScale = autoScale;
        if (radiusProperty != null) {
            radiusProperty.setHidden(autoScale);
            firePropertyChange("Auto scale", o, autoScale);
        }
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    protected LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty("Auto scale",
                    "Set whether or not the layout should automatically scale itself to fit the display bounds.",
                    "Parameters",
                    this, boolean.class, "isAutoScale", "setAutoScale"));
            radiusProperty = LayoutProperty.createProperty("Radius",
                    "Radius of the layout circle",
                    "Parameters",
                    this, double.class, "getRadius", "setRadius");
            radiusProperty.setHidden(autoScale);
            properties.add(radiusProperty);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[properties.size()]);
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }

    @Override
    public void run(double frac) {
        TupleSet nodes = getLayoutTargetNodes();
        int nodeTotal = nodes.getTupleCount();

        Rectangle2D rect = getLayoutBounds();
        double height = rect.getHeight();
        double width = rect.getWidth();
        double cx = rect.getCenterX();
        double cy = rect.getCenterY();

        double r = (isAutoScale() || radius <= 0)
                ? 0.45 * (height < width ? height : width) : radius;

//        Iterator<NodeItem> nodeItems = nodes.tuples();
        Iterator<NodeItem> nodeItems = new SortedTupleIterator(nodes.tuples(), nodeTotal,
                new Comparator<NodeItem>() {
                    @Override
                    public int compare(NodeItem n1, NodeItem n2) {
                        int d1 = n1.getDegree(), d2 = n2.getDegree();
                        return (d1 == 0 || d2 == 0) ? d1 - d2 : 0;
                    }
                });
        for (int i = 0; nodeItems.hasNext(); i++) {
            NodeItem n = nodeItems.next();
            double angle = (2 * Math.PI * i) / nodeTotal;
            double x = Math.cos(angle) * r + cx;
            double y = Math.sin(angle) * r + cy;
            setX(n, null, x);
            setY(n, null, y);
        }
    }

    @Override
    public void resetProperties() {
        setRadius(200D);
        setAutoScale(true);
    }

    @Override
    public boolean supportsSelectionOnly() {
        return true;
    }
}
