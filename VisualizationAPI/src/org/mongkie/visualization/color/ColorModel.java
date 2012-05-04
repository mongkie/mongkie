/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualization.color;

import java.beans.PropertyChangeListener;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface ColorModel {

    public static final String NODE_COLORPROVIDER = "NodeColorProvider";
    public static final String EDGE_COLORPROVIDER = "EdgeColorProvider";
    public static final String GROUP_COLORPROVIDER = "GroupColorProvider";

    public ColorProvider<NodeItem> getNodeColorProvider();

    public ColorProvider<EdgeItem> getEdgeColorProvider();

    public ColorProvider<VisualItem> getGroupColorProvider();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
