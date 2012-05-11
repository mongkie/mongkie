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
package org.mongkie.ui.datatable.graph;

import javax.swing.Icon;
import kobic.prefuse.Constants;
import org.mongkie.datatable.spi.DataTable;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataTable.class, position = 200)
public class EdgeDataTable extends AbstractDataTable {

    @Override
    protected String getLabelColumn(MongkieDisplay d) {
        return d.getGraph().getEdgeLabelField();
    }

    @Override
    public String getDataGroup() {
        return Constants.EDGES;
    }

    @Override
    public String getName() {
        return EDGES;
    }

    @Override
    public Icon getIcon() {
        if (icon == null) {
            icon = ImageUtilities.loadImageIcon("org/mongkie/ui/datatable/resources/edges.png", false);
        }
        return icon;
    }
    private Icon icon;
}
