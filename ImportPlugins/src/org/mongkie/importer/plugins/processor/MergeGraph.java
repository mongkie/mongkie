/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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

import org.mongkie.importer.GraphContainer;
import org.mongkie.importer.spi.Processor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = Processor.class, position = 200)
public class MergeGraph implements Processor<GraphContainer> {

    private GraphContainer container;

    @Override
    public void process() {
        System.out.println("Merge processing...");
    }

    @Override
    public void setContainer(GraphContainer container) {
        this.container = container;
    }

    @Override
    public GraphContainer getContainer() {
        return container;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(MergeGraph.class, "MergeGraphProcessor.displayName");
    }

    @Override
    public boolean isEnabled(GraphContainer container) {
        return false;
    }
}
