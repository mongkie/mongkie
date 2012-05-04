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
package org.mongkie.visualmap.partition;

import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.group.GroupingSupportable;
import org.mongkie.visualization.workspace.AbstractController;
import static org.mongkie.visualmap.VisualMapping.EDGE_ELEMENT;
import static org.mongkie.visualmap.VisualMapping.NODE_ELEMENT;
import org.mongkie.visualmap.spi.partition.Part;
import org.mongkie.visualmap.spi.partition.Partition;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PartitionController<M extends PartitionModel>
        extends AbstractController<M, Partition[]> implements GroupingSupportable<Part> {

    private final String[] ELEMENT_TYPES = new String[]{NODE_ELEMENT, EDGE_ELEMENT};

    public String[] getElementTypes() {
        return ELEMENT_TYPES;
    }

    public abstract void setCurrentElementType(String elementType);

    public abstract void setCurrentPartition(Partition partition);

    @Override
    public M getModel() {
        return super.getModel();
    }

    @Override
    public synchronized M getModel(MongkieDisplay d) {
        return super.getModel(d);
    }
}
