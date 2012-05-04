/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.visualmap.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.group.GroupingSupport;
import org.mongkie.visualmap.VisualMapping;
import org.mongkie.visualmap.partition.PartitionController;
import org.mongkie.visualmap.spi.partition.Part;
import org.mongkie.visualmap.spi.partition.Partition;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = PartitionController.class)
public class PartitionControllerImpl extends PartitionController<PartitionModelImpl> {

    private GroupingSupport<Part> gs = new GroupingSupport<Part>(this);

    @Override
    public void setCurrentElementType(String elementType) {
        if (model != null) {
            model.setCurrentElementType(elementType);
        }
    }

    @Override
    public void setCurrentPartition(Partition partition) {
        if (model != null) {
            model.setCurrentPartition(partition);
        }
    }

    @Override
    protected Class<PartitionModelImpl> getModelClass() {
        return PartitionModelImpl.class;
    }

    @Override
    protected PartitionModelImpl createModel(MongkieDisplay d) {
        return new PartitionModelImpl(d);
    }

    @Override
    public Collection<Node> getNodes(Part p) {
        if (p.getPartition().getElementType().equals(VisualMapping.NODE_ELEMENT)) {
            List<Node> nodes = new ArrayList<Node>();
            for (VisualItem vi : p.getVisualItems()) {
                nodes.add(p.getPartition().getGraph().getNode(vi.getSourceTuple().getRow()));
            }
            return nodes;
        } else {
            return null;
        }
    }

    @Override
    public String getName(Part p) {
        return p.getValue().toString();
    }

    @Override
    public Color getColor(Part p) {
        return p.getColor();
    }

    @Override
    public void ungrouped(AggregateItem group) {
    }

    @Override
    public AggregateItem group(Part p) {
        return gs.group(p);
    }

    @Override
    public void ungroup(Part p) {
        gs.ungroup(p);
    }

    @Override
    public boolean isGrouped(Part p) {
        return gs.isGrouped(p);
    }

    @Override
    public AggregateItem getGroup(Part p) {
        return gs.getGroup(p);
    }
}
