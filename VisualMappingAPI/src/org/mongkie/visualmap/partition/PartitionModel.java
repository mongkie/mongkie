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
import org.mongkie.visualization.workspace.AbstractModel;
import org.mongkie.visualmap.spi.partition.Partition;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PartitionModel extends AbstractModel<Partition[], PartitionModelListener> {

    public PartitionModel(MongkieDisplay display) {
        super(display);
    }

    public Partition[] getPartitions() {
//        return get();
        return getPartitions(getCurrentElementType());
    }

    public abstract String getCurrentElementType();

    public abstract Partition getCurrentPartition();

    public abstract Partition[] getNodePartitions();

    public abstract Partition[] getEdgePartitions();

    public abstract Partition[] getPartitions(String elementType);

    public abstract Partition getPartition(String elementType, String name);
}
