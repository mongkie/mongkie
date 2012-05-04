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
package org.mongkie.ui.datatable;

import org.mongkie.datatable.DataTableControllerUI;
import org.mongkie.datatable.spi.DataTable;
import static org.mongkie.datatable.spi.DataTable.EDGES;
import static org.mongkie.datatable.spi.DataTable.NODES;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataTableControllerUI.class)
public class DataTableControllerUIImpl implements DataTableControllerUI {

    @Override
    public DataTable selectTable(String name) {
        DataTableTopComponent tc = DataTableTopComponent.findInstance();
        if (!tc.isOpened()) {
            tc.open();
            tc.requestActive();
        }
        return tc.selectTable(name);
    }

    @Override
    public DataTable selectNodeTable() {
        return selectTable(NODES);
    }

    @Override
    public DataTable selectEdgeTable() {
        return selectTable(EDGES);
    }

    @Override
    public DataTable getSelectedTable() {
        return DataTableTopComponent.findInstance().getSelectedTable();
    }

    @Override
    public void setActivatedNodes(Node... nodes) {
        DataTableTopComponent.findInstance().setActivatedNodes(nodes);
    }
}
