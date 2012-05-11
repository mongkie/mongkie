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
package org.mongkie.ui.datatable.graph.actions;

import org.mongkie.datatable.spi.DataAction;
import org.mongkie.datatable.spi.DataTable;
import org.mongkie.ui.datatable.graph.AbstractDataTable;
import org.mongkie.ui.datatable.graph.AbstractDataTable.AbstractModel;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class AbstractDataAction implements DataAction<AbstractDataTable> {

    @Override
    public boolean isActionFor(DataTable table) {
        return DataTable.NODES.equals(table.getName()) || DataTable.EDGES.equals(table.getName());
    }

    @Override
    public boolean isEnabled(AbstractDataTable table) {
        AbstractModel m = table.getModel();
        return m != null && m.getDisplay().isFired() && isEnabled(table.getModel());
    }

    @Override
    public boolean hideActionText() {
        return true;
    }

    protected abstract boolean isEnabled(AbstractModel model);
}
