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
package org.mongkie.ui.datatable.graph.actions.column;

import java.awt.Image;
import org.mongkie.datatable.spi.DataAction;
import org.mongkie.datatable.spi.DataTable;
import org.mongkie.datatable.spi.GraphAddColumnAction;
import org.mongkie.datatable.spi.PopupAction;
import org.mongkie.ui.datatable.graph.AbstractDataTable;
import org.mongkie.ui.datatable.graph.AbstractDataTable.AbstractModel;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataAction.class, position = 20)
public class AddColumn extends AbstractColumnAction implements PopupAction {

    @Override
    public String getName() {
        return "Add column";
    }

    @Override
    public String getDescription() {
        return "Add a new column";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/mongkie/ui/datatable/resources/table-insert-column.png", false);
    }

    @Override
    public void execute(AbstractDataTable table) {
        throw new UnsupportedOperationException("This action can not be executed.");
    }

    @Override
    public boolean isEnabled(AbstractDataTable table) {
        AbstractModel model = table.getModel();
        return model != null && isEnabled(model);
    }

    @Override
    public boolean isEnabled(AbstractModel model) {
        return model.getDisplay().getDataEditSupport(model.getDataTable().getDataGroup()).isAddColumnSupported();
    }

    @Override
    public boolean isPopupOnly() {
        return true;
    }

    @Override
    public String getPopupDescription() {
        return "Add a new column into the table in a variety of ways using the pre-defined strategies, such as creating a new column, merging existing columns into the new one, adding a derived column from the existing one";
    }

    @Override
    public DataAction[] getDataActions(DataTable table) {
        return Lookup.getDefault().lookupAll(GraphAddColumnAction.class).toArray(new DataAction[]{});
    }

    @Override
    public UI getUI() {
        return null;
    }
}
