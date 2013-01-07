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
package org.mongkie.visualmap;

import org.mongkie.visualization.util.VisualStyle;
import prefuse.data.Edge;
import prefuse.data.Node;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface VisualMapping<T, V> {

    /**
     * Element type for nodes. The ranking receives a {@link Node} object.
     */
    public static final String NODE_ELEMENT = VisualStyle.NODES;
    /**
     * Element type for edges. The ranking receives a {@link Edge} object.
     */
    public static final String EDGE_ELEMENT = VisualStyle.EDGES;

    /**
     * Returns the value of the element. 
     * @param tuple the tuple to get the value from
     * @return the tuple's value
     */
    public V getValue(T tuple);

    /**
     * The <code>getElementType()</code> method should return either
     * <code>NODE_ELEMENT</code> or <code>EDGE_ELEMENT</code> to
     * define if it works with node or edge elements. This is important because it
     * defines which objects the <code>getValue()</code> eventually receives. For nodes,
     * it is given a {@link Node} object and for edges a {@link Edge}.
     * @return the type of element this visual mapping is manipulating. Value can either be
     * <code>NODE_ELEMENT</code> or <code>EDGE_ELEMENT</code>
     */
    public String getElementType();

    /**
     * Returns the display name of this mapping.
     * @return the display name of this mapping
     */
    public String getDisplayName();

    /**
     * Returns the name of this mapping. It should be unique.
     * @return the name of this mapping
     */
    public String getName();
}
