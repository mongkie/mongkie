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

import org.mongkie.visualmap.partition.PartitionEvent;
import org.mongkie.visualmap.partition.PartitionModel;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class PartitionEventImpl implements PartitionEvent {

    private final PartitionEvent.Type eventType;
    private final PartitionModel source;
    private final Object oldValue, newValue;

    PartitionEventImpl(Type eventType, PartitionModel source, Object oldValue, Object newValue) {
        this.eventType = eventType;
        this.source = source;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public Type getType() {
        return eventType;
    }

    @Override
    public PartitionModel getSource() {
        return source;
    }

    @Override
    public Object getOldValue() {
        return oldValue;
    }

    @Override
    public Object getNewValue() {
        return newValue;
    }

    @Override
    public boolean is(Type... type) {
        for (Type e : type) {
            if (e.equals(eventType)) {
                return true;
            }
        }
        return false;
    }
}
