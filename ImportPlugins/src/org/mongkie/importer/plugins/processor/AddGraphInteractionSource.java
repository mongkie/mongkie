/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.importer.plugins.processor;

import org.mongkie.im.InteractionController;
import org.mongkie.importer.GraphContainer;
import org.mongkie.importer.VizGraphContainer;
import org.mongkie.importer.spi.Processor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = Processor.class, position = 300)
public class AddGraphInteractionSource implements Processor<GraphContainer> {

    private GraphContainer container;
    private String name, nodeKeyCol;

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getNodeKeyCol() {
        return nodeKeyCol;
    }

    void setNodeKeyCol(String nodeKeyCol) {
        this.nodeKeyCol = nodeKeyCol;
    }

    @Override
    public GraphContainer getContainer() {
        return container;
    }

    @Override
    public void process() {
        Graph g = container.getGraph();
        Lookup.getDefault().lookup(InteractionController.class).addInteractionSource(name, g, nodeKeyCol);
    }

    @Override
    public void setContainer(GraphContainer container) {
        this.container = container;
        this.name = container.getSource().substring(0, container.getSource().lastIndexOf('.'));
        this.nodeKeyCol = container.getNodeIdColumn();
    }

    @Override
    public String getDisplayName() {
        return "Add into interaction manager";
    }

    @Override
    public boolean isEnabled(GraphContainer container) {
        if (container instanceof VizGraphContainer) {
            return false;
        }
        return true;
    }
}
