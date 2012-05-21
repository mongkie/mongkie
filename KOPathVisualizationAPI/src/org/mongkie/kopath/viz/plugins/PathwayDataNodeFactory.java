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
package org.mongkie.kopath.viz.plugins;

import static kobic.prefuse.Constants.NODES;
import static kobic.prefuse.Constants.PROPKEY_DATAGROUP;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.datatable.DataNode;
import org.mongkie.datatable.spi.DataNodeFactory;
import static org.mongkie.kopath.Config.FIELD_INTERACTIONID;
import static org.mongkie.kopath.Config.FIELD_NAME;
import org.mongkie.kopath.viz.PathwayDataNode;
import org.mongkie.kopath.viz.PathwayDisplay;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataNodeFactory.class)
public class PathwayDataNodeFactory implements DataNodeFactory {

    @Override
    public boolean readyFor(Table table) {
        return table.getClientProperty(NetworkDisplay.PROP_KEY).getClass() == PathwayDisplay.class;
    }

    @Override
    public DataNode createDataNode(Tuple data, String labelColumn) {
        String dataGroup = (String) data.getTable().getClientProperty(PROPKEY_DATAGROUP);
        if (NODES.equals(dataGroup)) {
            labelColumn = FIELD_NAME;
        } else if (Graph.EDGES.equals(dataGroup)) {
            labelColumn = FIELD_INTERACTIONID;
        }
        return new PathwayDataNode(data, labelColumn);
    }
}
