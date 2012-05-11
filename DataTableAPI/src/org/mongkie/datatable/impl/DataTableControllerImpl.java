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
package org.mongkie.datatable.impl;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.datatable.DataNode;
import org.mongkie.datatable.DataTableController;
import org.mongkie.datatable.spi.DataAction;
import org.mongkie.datatable.spi.DataNodeFactory;
import org.mongkie.datatable.spi.DataTable;
import org.mongkie.datatable.spi.GraphDataTable;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataTableController.class)
public class DataTableControllerImpl implements DataTableController {

    @Override
    public DataTable getDataTable(String name) {
        for (DataTable table : Lookup.getDefault().lookupAll(DataTable.class)) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    @Override
    public GraphDataTable getNodeDataTable() {
        return (GraphDataTable) getDataTable(DataTable.NODES);
    }

    @Override
    public GraphDataTable getEdgeDataTable() {
        return (GraphDataTable) getDataTable(DataTable.EDGES);
    }

    @Override
    public DataNode createDataNode(Tuple data, String labelColumn) {
        DataNodeFactory factory = getDataNodeFactory(data.getTable());
        assert factory != null;
        return factory.createDataNode(data, labelColumn);
    }

    @Override
    public DataNodeFactory getDataNodeFactory(Table table) {
        for (DataNodeFactory factory : Lookup.getDefault().lookupAll(DataNodeFactory.class)) {
            if (factory.readyFor(table)) {
                return factory;
            }
        }
        return null;
    }

    @Override
    public List<DataAction> getDataActionsFor(DataTable table) {
        List<DataAction> actions = new ArrayList<DataAction>();
        for (DataAction a : Lookup.getDefault().lookupAll(DataAction.class)) {
            if (a.isActionFor(table)) {
                actions.add(a);
            }
        }
        return actions;
    }
}
