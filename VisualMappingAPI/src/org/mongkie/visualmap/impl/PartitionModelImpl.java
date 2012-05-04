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

import java.util.*;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualmap.VisualMapping;
import org.mongkie.visualmap.partition.PartitionEvent;
import org.mongkie.visualmap.partition.PartitionEvent.Type;
import static org.mongkie.visualmap.partition.PartitionEvent.Type.CURRENT_ELEMENT_TYPE;
import static org.mongkie.visualmap.partition.PartitionEvent.Type.CURRENT_PARTITION;
import org.mongkie.visualmap.partition.PartitionModel;
import org.mongkie.visualmap.partition.PartitionModelListener;
import org.mongkie.visualmap.spi.partition.Partition;
import org.mongkie.visualmap.spi.partition.PartitionBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class PartitionModelImpl extends PartitionModel {

    protected final Map<String, Partition> currentPartitions;
    private String currentElementType;

    PartitionModelImpl(MongkieDisplay display) {
        super(display);
        currentElementType = VisualMapping.NODE_ELEMENT;
        currentPartitions = new HashMap<String, Partition>();
    }

    void setCurrentElementType(String elementType) {
        if (this.currentElementType.equals(elementType)) {
            return;
        }
        String old = this.currentElementType;
        this.currentElementType = elementType;
        firePartitionEvent(CURRENT_ELEMENT_TYPE, old, elementType);
    }

    @Override
    public String getCurrentElementType() {
        return currentElementType;
    }

    void setCurrentPartition(Partition partition) {
        if ((currentPartitions.get(currentElementType) == null && partition == null)
                || (currentPartitions.get(currentElementType) != null && currentPartitions.get(currentElementType) == partition)) {
            return;
        }
        Partition old = currentPartitions.get(currentElementType);
        currentPartitions.put(currentElementType, partition);
        firePartitionEvent(CURRENT_PARTITION, old, partition);
    }

    @Override
    public Partition getCurrentPartition() {
        return currentPartitions.get(currentElementType);
    }

    @Override
    public Partition[] getNodePartitions() {
        return getPartitions(VisualMapping.NODE_ELEMENT);
    }

    @Override
    public Partition[] getEdgePartitions() {
        return getPartitions(VisualMapping.EDGE_ELEMENT);
    }

    @Override
    public Partition[] getPartitions(String elementType) {
        List<Partition> partitions = new ArrayList<Partition>();
        Collection<? extends PartitionBuilder> builders = Lookup.getDefault().lookupAll(PartitionBuilder.class);
        for (PartitionBuilder builder : builders) {
            // TODO: caching required?
            Partition[] builtPartitions = builder.buildPartitions(this);
            if (builtPartitions != null) {
                for (Partition p : builtPartitions) {
                    if (p.getElementType().equals(elementType)) {
                        partitions.add(p);
                    }
                }
            }
        }
        Partition current = getCurrentPartition();
        if (current != null) {
            //Update selectedRanking with latest version
            for (Partition p : partitions) {
                if (p.getName().equals(current.getName())) {
                    currentPartitions.put(elementType, p);
                    break;
                }
            }
        }
        return partitions.toArray(new Partition[0]);
    }

    @Override
    public Partition getPartition(String elementType, String name) {
        Partition[] partitions = getPartitions(elementType);
        for (Partition p : partitions) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    private void firePartitionEvent(Type eventType, Object oldValue, Object newValue) {
        PartitionEvent e = new PartitionEventImpl(eventType, this, oldValue, newValue);
        synchronized (listeners) {
            for (Iterator<PartitionModelListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                listenerIter.next().processPartitionEvent(e);
            }
        }
    }

    @Override
    protected void changed(Partition[] o, Partition[] n) {
    }

    @Override
    protected void load(Partition[] data) {
    }

    @Override
    protected void unload(Partition[] data) {
    }
}
