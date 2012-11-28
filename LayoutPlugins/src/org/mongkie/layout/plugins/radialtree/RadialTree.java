/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.layout.plugins.radialtree;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import static kobic.prefuse.Constants.*;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.PrefuseLayout;
import org.openide.util.Exceptions;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.data.util.SortedTupleIterator;
import prefuse.util.DataLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualGraph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RadialTree extends PrefuseLayout.Delegation<RadialTreeLayout> {

    private double radiusIncrement = 50;
    private boolean autoScale = true;
    private LayoutProperty radiusIncrementProperty;

    RadialTree(LayoutBuilder<RadialTree> builder) {
        super(builder);
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        boolean o = this.autoScale;
        this.autoScale = autoScale;
        if (radiusIncrementProperty != null) {
            radiusIncrementProperty.setHidden(autoScale);
            firePropertyChange(radiusIncrementProperty.getName(), o, autoScale);
        }
    }

    public double getRadiusIncrement() {
        return radiusIncrement;
    }

    public void setRadiusIncrement(double radiusIncrement) {
        this.radiusIncrement = radiusIncrement;
    }

    @Override
    public void initAlgo() {
        super.initAlgo();
        VisualGraph vg = display.getVisualGraph();
        getDeligateLayout().initSchema(vg.getNodes()); // Atfer node table changed, spanning tree becomes null
        findAllSpanningTreeRoots(
                DataLib.asLinkedList(new SortedTupleIterator<NodeItem>(vg.getNodes().tuples(), vg.getNodeCount(),
                new Comparator<NodeItem>() {
                    @Override
                    public int compare(NodeItem n1, NodeItem n2) {
                        return n2.getDegree() - n1.getDegree();
                    }
                })), roots);
    }
    private final List<NodeItem> roots = new ArrayList<NodeItem>();

    private void findAllSpanningTreeRoots(LinkedList<NodeItem> nodes, List<NodeItem> roots) {
        if (nodes.isEmpty()) {
            return;
        }
        NodeItem root = nodes.removeFirst();
        for (Iterator<NodeItem> treeIter = display.getVisualGraph().getSpanningTree(root).nodes();
                treeIter.hasNext();) {
            nodes.remove(treeIter.next());
        }
        roots.add(root);
        findAllSpanningTreeRoots(nodes, roots);
    }

    @Override
    public void endAlgo() {
        super.endAlgo();
        display.getVisualGraph().clearSpanningTree();
        roots.clear();
    }

    @Override
    protected RadialTreeLayout createDeligateLayout() {
        RadialTreeLayout l = new RadialTreeLayout(GRAPH) {
            private final Rectangle2D b = new Rectangle2D.Double();
            private final Point2D c = new Point2D.Double();
            private final float GAP = 20;

            @Override
            public void run(double frac) {
                int i = 0;
                for (NodeItem root : roots) {
                    if (i > 0) {
                        DisplayLib.getBounds(display.getVisualGraph().getSpanningTree().nodes(), 0, b);
                        b.setFrame(b.getMinX() + b.getWidth() + GAP, b.getMinY(), b.getWidth(), b.getHeight());
                        int pSize = display.getVisualGraph().getSpanningTree().getNodeCount();
                        int cSize = display.getVisualGraph().getSpanningTree(root).getNodeCount(); // Also rebuild spanning tree
                        if (cSize > pSize) {
                            double width = b.getWidth();
                            GraphicsLib.expand(b, (width * cSize / pSize) - width);
                            b.setFrame(b.getMinX() + (b.getWidth() - width) / 2, b.getMinY(), b.getWidth(), b.getHeight());
                        } else {
                            GraphicsLib.expand(b, (b.getWidth() * cSize / pSize) - b.getWidth());
                        }
                        setLayoutBounds(b);
                        c.setLocation(b.getCenterX(), b.getCenterY());
                        setLayoutAnchor(c);
                    } else {
                        display.getVisualGraph().getSpanningTree(root); // Rebuild spanning tree
                    }
                    setLayoutRoot(root);
                    super.run(frac);
                    i++;
                }
                setLayoutBounds(null);
                setLayoutAnchor(null);
            }
        };
        l.setAutoScale(autoScale);
        return l;
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty("Auto scale",
                    "Set whether or not the layout should automatically scale itself to fit the display bounds.",
                    "Parameters",
                    this, boolean.class, "isAutoScale", "setAutoScale"));
            radiusIncrementProperty = LayoutProperty.createProperty("Radius increment",
                    "Set the radius increment to use between concentric circles. Note that this value is used only if auto-scaling is disabled.",
                    "Parameters",
                    this, double.class, "getRadiusIncrement", "setRadiusIncrement");
            radiusIncrementProperty.setHidden(autoScale);
            properties.add(radiusIncrementProperty);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[properties.size()]);
    }

    @Override
    public void resetProperties() {
        setRadiusIncrement(50);
        setAutoScale(true);
    }

    @Override
    protected void setLayoutParameters(RadialTreeLayout layout) {
        layout.setAutoScale(autoScale);
        layout.setRadiusIncrement(radiusIncrement);
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
