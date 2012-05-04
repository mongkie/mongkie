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

import java.awt.Color;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface ColorController {

    public Color getTextColor(VisualItem item);

    public Color setTextColor(VisualItem item, Color c);

    public Color unsetTextColor(VisualItem item);

    public Color getStrokeColor(VisualItem item);

    public Color setStrokeColor(VisualItem item, Color c);

    public Color unsetStrokeColor(VisualItem item);

    public Color getFillColor(VisualItem item);

    public Color setFillColor(VisualItem item, Color c);

    public Color unsetFillColor(VisualItem item);

    public ColorModel getModel();

    public ColorModel getModel(MongkieDisplay d);

    public void setNodeColorProvider(ColorProvider<NodeItem> nc);

    public void unsetNodeColorProvider(ColorProvider<NodeItem> nc);

    public void setEdgeColorProvider(ColorProvider<EdgeItem> ec);

    public void unsetEdgeColorProvider(ColorProvider<EdgeItem> ec);
}
