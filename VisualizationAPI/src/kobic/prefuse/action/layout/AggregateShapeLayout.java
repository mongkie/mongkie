/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse.action.layout;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.AggregateShape;
import static kobic.prefuse.Constants.NODES;
import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.data.CascadedTable;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.util.NamedColumnProjection;
import prefuse.visual.*;
import prefuse.visual.expression.VisiblePredicate;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class AggregateShapeLayout extends Layout {

    public AggregateShapeLayout(Visualization v) {
        super(Visualization.AGGR_ITEMS);
        setVisualization(v);
    }

    @Override
    public void run(double frac) {
        Iterator<AggregateItem> aggregates = ((AggregateTable) m_vis.getVisualGroup(m_group)).tuples(VisiblePredicate.TRUE);
        while (aggregates.hasNext()) {
            AggregateItem aggregate = aggregates.next();
            AggregateShape s = AggregateShape.get(aggregate.getShape());
            if (s == null) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unknown aggregate shape code: {0}", aggregate.getShape());
                continue;
            }
            s.layout(aggregate);
        }
    }

    public static NodeItemsProjectionTable createNodeItemsProjectionTable(final Visualization v) {
        return new NodeItemsProjectionTable(v);
    }

    public static class NodeItemsProjectionTable extends CascadedTable {

        private final Visualization v;

        protected NodeItemsProjectionTable(final Visualization v) {
            super((VisualTable) v.getVisualGroup(NODES),
                    new AbstractPredicate() {

                        AggregateTable aggregates = (AggregateTable) v.getVisualGroup(Visualization.AGGR_ITEMS);

                        @Override
                        public boolean getBoolean(Tuple t) {
                            return aggregates.containsItem((NodeItem) t);
                        }
                    }, new NamedColumnProjection(new String[]{VisualItem.X, VisualItem.Y, VisualItem.BOUNDS}, true));
            ((AggregateTable) v.getVisualGroup(Visualization.AGGR_ITEMS)).addTableListener(
                    new TableListener() {

                        @Override
                        public void tableChanged(Table t, int start, int end, int col, int type) {
                            if (col == EventConstants.ALL_COLUMNS && type == EventConstants.UPDATE) {
                                filterRows();
                            }
                        }
                    });
            this.v = v;
        }

        public TableListener createNodeItemsProjectionListener() {
            return new NodeItemsProjectionListener(v);
        }
    }

    protected static class NodeItemsProjectionListener implements TableListener {

        private final AggregateTable aggregates;

        protected NodeItemsProjectionListener(Visualization v) {
            aggregates = (AggregateTable) v.getVisualGroup(Visualization.AGGR_ITEMS);
        }

        @Override
        public void tableChanged(Table projection, int start, int end, int col, int type) {
            if (type == EventConstants.UPDATE && col != EventConstants.ALL_COLUMNS) {
                VisualTable nodes = (VisualTable) ((CascadedTable) projection).getParentTable();
                String column = projection.getColumnName(col);
                for (int r = start; r <= end; ++r) {
                    int n = ((CascadedTable) projection).getParentRow(r);
                    if (column.equals(VisualItem.X) || column.equals(VisualItem.Y)) {
                        for (Iterator<AggregateItem> aggregateIter = aggregates.getAggregates(nodes.getItem(n)); aggregateIter.hasNext();) {
                            AggregateItem aggregate = aggregateIter.next();
                            if (aggregate.isVisible()) {
                                aggregate.setValidated(false);
                            }
                        }
                    } else if (column.equals(VisualItem.BOUNDS)) {
                        VisualItem nodeItem = nodes.getItem(n);
                        for (Iterator<AggregateItem> aggregateIter = aggregates.getAggregates(nodeItem); aggregateIter.hasNext();) {
                            AggregateItem aggregate = aggregateIter.next();
                            if (!aggregate.isVisible()) {
                                continue;
                            }
                            AggregateShape s = AggregateShape.get(aggregate.getShape());
                            if (s == null) {
                                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unknown aggregate shape code: {0}", aggregate.getShape());
                                continue;
                            }
                            if (s.layout(aggregate)) {
                                aggregate.validateBounds();
                            }
                        }
                    }
                }
            }
        }
    }
}
