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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import kobic.prefuse.display.DataViewSupport;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.datatable.spi.DataNodeFactory;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class DataChildFactory extends ChildFactory<Tuple> implements TableListener {

    private Table table;
    private DataNodeFactory nodeFactory;
    private String labelColumn;
    private final Map<Integer, DataNode> tuple2Node = new HashMap<Integer, DataNode>();

    public DataChildFactory(Table table, String labelColumn) {
        this.table = table;
        this.table.addTableListener(DataChildFactory.this);
        this.labelColumn = labelColumn;
    }

    public DataChildFactory setTable(Table table, String labelColumn) {
        Table old = this.table;
        if (old != null) {
            old.removeTableListener(this);
        }
        if (table == null || this.table != table) {
            for (DataNode n : tuple2Node.values()) {
                try {
                    n.destroy();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            tuple2Node.clear();
        }
        this.table = table;
        if (this.table != null) {
            this.table.addTableListener(this);
        }
        this.labelColumn = labelColumn;
        refresh(true);
        return this;
    }

    @Override
    protected boolean createKeys(List<Tuple> toPopulate) {
        if (null != table) {
            for (Iterator<Tuple> tuples =
                    ((DataViewSupport) table.getClientProperty(DataViewSupport.PROP_KEY)).tuples();
                    tuples.hasNext();) {
                Tuple tuple = tuples.next();
                toPopulate.add(tuple);
            }
            // Remove keys not contained anymore
            for (Iterator<Integer> keys = tuple2Node.keySet().iterator(); keys.hasNext();) {
                Integer key = keys.next();
                if (!table.isValidRow(key)
                        || !toPopulate.contains(table.getTuple(key))) {
                    try {
                        tuple2Node.get(key).destroy();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
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
        // row inserted or deleted
        if (!d.isLoading()
                && col == EventConstants.ALL_COLUMNS) {
            d.getVisualization().invokeAfterDataProcessing(this, new Runnable() {
                @Override
                public void run() {
                    refresh(true);
                }
            });
        }
    }
}
