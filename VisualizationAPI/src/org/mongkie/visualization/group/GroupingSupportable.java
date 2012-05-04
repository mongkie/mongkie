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
package org.mongkie.visualization.group;

import java.awt.Color;
import java.util.Collection;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface GroupingSupportable<T> {

    public Collection<Node> getNodes(T groupable);

    public String getName(T groupable);

    public Color getColor(T groupable);

    public void ungrouped(AggregateItem group);

    // Delegate methods
    public abstract AggregateItem group(T groupable);

    public abstract void ungroup(T groupable);

    public abstract boolean isGrouped(T groupable);

    public abstract AggregateItem getGroup(T groupable);
}
