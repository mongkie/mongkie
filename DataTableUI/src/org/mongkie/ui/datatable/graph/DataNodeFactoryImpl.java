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

import kobic.prefuse.display.DataEditSupport;
import kobic.prefuse.display.DataViewSupport;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.datatable.DataNode;
import org.mongkie.datatable.spi.DataNodeFactory;
import org.mongkie.ui.visualization.spi.TupleNodeFactory;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProviders({
    @ServiceProvider(service = DataNodeFactory.class),
    @ServiceProvider(service = TupleNodeFactory.class)
})
public class DataNodeFactoryImpl implements DataNodeFactory, TupleNodeFactory {

    @Override
    public boolean readyFor(Table table) {
        return table.getClientProperty(NetworkDisplay.PROP_KEY).getClass() == MongkieDisplay.class;
    }

    @Override
    public DataNode createDataNode(Tuple data, String labelColumn) {
        return new DataNode(data, labelColumn) {
            @Override
            protected Set preparePropertySet(Tuple data) {
                Sheet.Set attributes = Sheet.createPropertiesSet();
                Property p;
                Schema properties = getPropertySchema(data);
                for (int i = 0; i < properties.getColumnCount(); i++) {
                    String field = properties.getColumnName(i);
                    p = new Property(data, field,
                            ((DataEditSupport) data.getTable().getClientProperty(DataEditSupport.PROP_KEY)).getColumnType(field));
                    attributes.put(p);
                }
                return attributes;
            }

            @Override
            protected Schema getPropertySchema(Tuple data) {
                return ((DataViewSupport) data.getTable().getClientProperty(DataViewSupport.PROP_KEY)).getPropertySchema();
            }
        };
    }

    @Override
    public Node createTupleNode(Tuple tuple, String labelColumn) {
        return createDataNode(tuple, labelColumn);
    }
}
