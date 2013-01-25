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
package org.mongkie.datatable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import kobic.prefuse.display.DataViewSupport;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.datatable.spi.DataNodeFactory;
import org.mongkie.util.AccumulativeEventsProcessor;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.EventConstants;
import prefuse.data.event.ExpressionListener;
import prefuse.data.event.TableListener;
import prefuse.data.expression.Expression;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class DataChildFactory extends ChildFactory<Tuple> implements TableListener, ExpressionListener {

    private Table table;
    private DataNodeFactory nodeFactory;
    private String labelColumn;
    private final Map<Integer, DataNode> tuple2Node = new HashMap<Integer, DataNode>();

    public DataChildFactory(Table table, String labelColumn) {
        this.table = table;
        this.labelColumn = labelColumn;
        table.addTableListener(DataChildFactory.this);
        // Add a listener for filter changes to refresh child nodes
        ((DataViewSupport) table.getClientProperty(DataViewSupport.PROP_KEY)).getFilter().addExpressionListener(DataChildFactory.this);
    }

    public DataChildFactory setTable(Table table, String labelColumn) {
        Table old = this.table;
        if (old != null) {
            old.removeTableListener(this);
            ((DataViewSupport) old.getClientProperty(DataViewSupport.PROP_KEY)).getFilter().removeExpressionListener(this);
        }
        if (table == null || this.table != table) {
            tuple2Node.clear();
        }
        this.table = table;
        if (table != null) {
            table.addTableListener(this);
            ((DataViewSupport) table.getClientProperty(DataViewSupport.PROP_KEY)).getFilter().addExpressionListener(this);
        }
        this.labelColumn = labelColumn;
        refresh();
        return this;
    }

    @Override
    protected boolean createKeys(List<Tuple> toPopulate) {
        if (null != table) {
            for (Iterator<Tuple> tuples =
                    ((DataViewSupport) table.getClientProperty(DataViewSupport.PROP_KEY)).tuples(); // Returns not filtered tuples only
                    tuples.hasNext();) {
                Tuple tuple = tuples.next();
                toPopulate.add(tuple);
            }
            // Remove invalid or filtered tuples
            for (Iterator<Integer> keys = tuple2Node.keySet().iterator(); keys.hasNext();) {
                Integer key = keys.next();
                if (!table.isValidRow(key)
                        || !toPopulate.contains(table.getTuple(key))) {
                    keys.remove();
                }
            }
            nodeFactory = Lookup.getDefault().lookup(DataTableController.class).getDataNodeFactory(table);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Tuple key) {
        assert nodeFactory.readyFor(key.getTable());
        assert !tuple2Node.containsKey(key.getRow());
        DataNode n = nodeFactory.createDataNode(key, labelColumn);
        tuple2Node.put(key.getRow(), n);
        return n;
    }

    public DataNode getNodeOf(Tuple tuple) {
        return getNodeOf(tuple.getRow());
    }

    public DataNode getNodeOf(int row) {
        return tuple2Node.get(row);
    }

    @Override
    public void tableChanged(Table t, int start, int end, int col, final int type) {
        NetworkDisplay d = (NetworkDisplay) t.getClientProperty(NetworkDisplay.PROP_KEY);
        // Tuples inserted or deleted
        if (!d.isLoading()
                && col == EventConstants.ALL_COLUMNS) {
            d.getVisualization().invokeAfterDataProcessing(this, new Runnable() {
                @Override
                public void run() {
                    // DO NOT USE refreshLazy() which is lazy, but tuple changes must be refreshed immediately
                    refresh();
                }
            });
        }
        switch (type) {
            // Column added
            case EventConstants.INSERT:
                if (col != EventConstants.ALL_COLUMNS) {
                    for (DataNode node : tuple2Node.values()) {
                        node.fireColumnInserted(col);
                    }
                }
                break;
            // Column deleted
            case EventConstants.DELETE:
                if (col != EventConstants.ALL_COLUMNS) {
                    for (DataNode node : tuple2Node.values()) {
                        node.fireColumnDeleted(col);
                    }
                }
                break;
            // Tuples updated
            case EventConstants.UPDATE:
                for (int row = start; row <= end; row++) {
                    DataNode node = getNodeOf(row);
                    if (node != null) {
                        node.fireDataUpdated(col);
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void expressionChanged(Expression expr) { // Filters changed
        refreshLazy(); // Filter changes allow laziness
    }

    public boolean isRefreshing() {
        return refreshing;
    }
    private volatile boolean refreshing = false;

    // Refresh nodes immediately
    private void refresh() {
        refreshing = true;
        refresh(true);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshing = false;
            }
        });

    }

    // Refresh nodes lazily
    private void refreshLazy() {
        if (refreshQ != null && refreshQ.isAccumulating()) {
            refreshQ.eventAttended();
        } else {
            refreshQ = new AccumulativeEventsProcessor(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
            refreshQ.start();
        }
    }
    private AccumulativeEventsProcessor refreshQ;
}
