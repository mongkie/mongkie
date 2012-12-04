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
package org.mongkie.layout.plugins.grid;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import static kobic.prefuse.Constants.*;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.PrefuseLayout;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.spi.LayoutService.BigGraphLayout;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import prefuse.action.layout.GridLayout;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.SortedTupleIterator;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = BigGraphLayout.class)
public final class Grid extends PrefuseLayout.Delegation<GridLayout> implements BigGraphLayout {

    // Start of layout logics for the big graph
    public Grid() {
        super(null);
        bgLayout = createDeligateLayout();
        bgLayout.setAnalyze(true);
    }
    private GridLayout bgLayout;

    /**
     * Run layout algorithm for the big graph of the given display
     *
     * @param d displays the big graph
     */
    @Override
    public void layout(MongkieDisplay d) {
        bgLayout.setVisualization(d.getVisualization());
        d.setGraphLayout(bgLayout, 0);
        d.rerunLayoutAction();
    }
    // End of layout logics for the big graph
    private LayoutProperty propNumCols, propNumRows;

    Grid(LayoutBuilder<Grid> builder) {
        super(builder);
    }

    public int getNumCols() {
        return getDeligateLayout().getNumCols();
    }

    public void setNumCols(int cols) {
        if (cols < 1) {
            cols = 1;
        }
        int rows = (int) Math.ceil(display.getVisualGraph().getNodeCount() / cols) + 1;
        getDeligateLayout().setNumCols(cols);
        int old = getNumRows();
        getDeligateLayout().setNumRows(rows);
        firePropertyChange(propNumRows.getName(), old, rows);
    }

    public int getNumRows() {
        return getDeligateLayout().getNumRows();
    }

    public void setNumRows(int rows) {
        if (rows < 1) {
            rows = 1;
        }
        int cols = (int) Math.ceil(display.getVisualGraph().getNodeCount() / rows) + 1;
        int old = getNumCols();
        getDeligateLayout().setNumCols(cols);
        getDeligateLayout().setNumRows(rows);
        firePropertyChange(propNumCols.getName(), old, cols);
    }

    public boolean isAnalyze() {
        return getDeligateLayout().isAnalyze();
    }

    public void setAnalyze(boolean analyze) {
        GridLayout deligate = getDeligateLayout();
        boolean o = deligate.isAnalyze();
        deligate.setAnalyze(analyze);
        if (propNumCols != null) {
            propNumCols.setHidden(analyze);
        }
        if (propNumRows != null) {
            propNumRows.setHidden(analyze);
        }
        if (!analyze && (getNumRows() < 1 || getNumCols() < 1)) {
            int[] dim = GridLayout.analyzeGraphGrid(display.getVisualGraph().getNodes());
            getDeligateLayout().setNumCols(dim[0]);
            getDeligateLayout().setNumRows(dim[1]);
        }
        firePropertyChange("Auto dimensions", o, analyze);
    }

    @Override
    protected GridLayout createDeligateLayout() {
        GridLayout l = new GridLayout(NODES) {
            @Override
            public void run(double frac) {
                Rectangle2D b = getLayoutBounds();
                double bx = b.getMinX(), by = b.getMinY();
                double w = b.getWidth(), h = b.getHeight();

                TupleSet nodes = getLayoutTargetNodes();
                int m = rows, n = cols;
                if (analyze) {
                    int[] d = analyzeGraphGrid(nodes);
                    m = d[0];
                    n = d[1];
                }

                Iterator<NodeItem> nodeItems = new SortedTupleIterator(nodes.tuples(), nodes.getTupleCount(),
                        new Comparator<NodeItem>() {
                            @Override
                            public int compare(NodeItem n1, NodeItem n2) {
//                                int d1 = n1.getDegree(), d2 = n2.getDegree();
//                                return (d1 == 0 || d2 == 0) ? d2 - d1 : 0;
                                // ordered by in and out degree
                                return n2.getDegree() - n1.getDegree();
                            }
                        });
                // layout grid contents
                for (int i = 0; nodeItems.hasNext() && i < m * n; ++i) {
                    NodeItem nitem = nodeItems.next();
                    nitem.setVisible(true);
                    double x = bx + w * ((i % n) / (double) (n - 1));
                    double y = by + h * ((i / n) / (double) (m - 1));
                    setX(nitem, null, x);
                    setY(nitem, null, y);
                }
            }
        };
        l.setMargin(50, 50, 50, 50);
        return l;
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty("Auto dimensions",
                    "Set whether or not to analyze a set of nodes to determine grid dimensions automatically",
                    "Parameters",
                    this, boolean.class, "isAnalyze", "setAnalyze"));
            propNumCols = LayoutProperty.createProperty("Number of columns",
                    "Set the number of the grid columns",
                    "Parameters",
                    this, int.class, "getNumCols", "setNumCols");
            propNumCols.setHidden(isAnalyze());
            properties.add(propNumCols);
            propNumRows = LayoutProperty.createProperty("Number of rows",
                    "Set the number of the grid rows",
                    "Parameters",
                    this, int.class, "getNumRows", "setNumRows");
            propNumRows.setHidden(isAnalyze());
            properties.add(propNumRows);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetProperties() {
        setAnalyze(true);
        if (display != null) {
            int[] dim = GridLayout.analyzeGraphGrid(display.getVisualGraph().getNodes());
            getDeligateLayout().setNumCols(dim[0]);
            getDeligateLayout().setNumCols(dim[1]);
        }
    }

    @Override
    protected void setLayoutParameters(GridLayout layout) {
        //Do nothing
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }

    @Override
    public boolean supportsSelectionOnly() {
        return true;
    }
}
