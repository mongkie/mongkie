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

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface PartitionEvent {

    /**
     * The types of events
     */
    public enum Type {

        /**
         * The list of available ranking has been updated. The listeners can
         * call <code>PartitionModel.getPartitions()</code> to get the newly created partitions.
         */
        REFRESH_PARTITION,
        CURRENT_PARTITION,
        CURRENT_ELEMENT_TYPE,
        BARCHART_VISIBLE,};

    public Type getType();

    public PartitionModel getSource();

    public Object getOldValue();

    public Object getNewValue();

    /**
     * Returns <code>true</code> if this event is one of these in parameters.
     * @param type  the event types that are to be compared with this event
     * @return      <code>true</code> if this event is <code>type</code>,
     *              <code>false</code> otherwise
     */
    public boolean is(Type... type);
}
