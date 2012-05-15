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
package org.mongkie.ui.datatable.graph.actions.node;

import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.Map;
import org.mongkie.datatable.spi.DataAction;
import org.mongkie.ui.datatable.graph.AbstractDataTable;
import org.mongkie.ui.datatable.graph.AbstractDataTable.AbstractModel;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import static prefuse.Visualization.DRAW;
import prefuse.data.Node;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataAction.class, position = 10)
public class AddNode extends AbstractNodeAction {

    private final Map<String, Object> tupleData = new LinkedHashMap<String, Object>();

    void setTupleData(Map<String, Object> tupleData) {
        this.tupleData.clear();
        if (tupleData != null) {
            this.tupleData.putAll(tupleData);
        }
    }

    @Override
    public UI getUI() {
        return AddNodeUI.getInstance();
    }

    @Override
    public String getName() {
        return "Add node";
    }

    @Override
    public String getDescription() {
        return "Add a new node into the graph";
    }

    @Override
    public Image getIcon() {
        return ImageUtilities.loadImage("org/mongkie/ui/datatable/resources/plus-white.png", false);
    }

    @Override
    public void execute(final AbstractDataTable table) {
        final AbstractModel model = table.getModel();
        model.getDisplay().getVisualization().rerun(new Runnable() {

            @Override
            public void run() {
                Node n = model.getGraph().addNode();
                for (String field : tupleData.keySet()) {
                    n.set(field, tupleData.get(field));
                }
                table.setSelectedNodes(new org.openide.nodes.Node[]{table.getDataChildFactory().getNodeOf(n.getRow())});
            }
        }, DRAW);
    }

    @Override
    public boolean isEnabled(AbstractModel model) {
        return model.getDisplay().getNodeDataEditSupport().isAddDataSupported();
    }

    @Override
    public boolean hideActionText() {
        return false;
    }
}
