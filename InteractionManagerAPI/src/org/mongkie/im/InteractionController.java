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
package org.mongkie.im;

import java.util.Iterator;
import java.util.List;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.VisualStyle;
import prefuse.data.Graph;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface InteractionController {

    public static final String CATEGORY_OTHERS = "Others";

    public String[] getCategories();

    public List<InteractionSource> getInteractionSources(String category);

    public <I extends InteractionSource, M extends SourceModel<I>> M getModel(I is);

    public boolean setKeyField(InteractionSource is, String key);

    public <K> void executeLink(InteractionSource<K> is);

    public void executeUnlink(InteractionSource is);

    public <K> void executeExpand(InteractionSource<K> is, K... keys);

    public InteractionSource getInteractionSource(String name);

    public void addInteractionSource(String name, Graph g, String nodeKeyCol);

    public boolean addModelChangeListener(MongkieDisplay d, SourceModelChangeListener l);

    public boolean removeModelChangeListener(MongkieDisplay d, SourceModelChangeListener l);

    public VisualStyle<EdgeItem> getEdgeVisualStyle(InteractionSource is);

    public VisualStyle<NodeItem> getNodeVisualStyle(InteractionSource is);

    public Iterator<EdgeItem> getEdgeItems(InteractionSource is);
}
