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
package org.mongkie.visualization.group;

import java.util.List;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface GroupListener {

    /**
     * Notify that membership of a group has been changed.
     * <p/>
     * Current members of the group can be get through {@link AggregateItem#items() }
     *
     * @param group a group its membership is changed
     * @param olds  members were contained in the group <b>before membership being changed</b>
     */
    public void memberChanged(AggregateItem group, List<NodeItem> olds);

    /**
     * Notify that a group has been added.
     * <p/>
     * <b>This just added group is empty</b> thus, no member exits in the group at this time.
     * <br/>
     * You can observe membership change event of the group
     * through {@link #memberChanged(prefuse.visual.AggregateItem, java.util.List) }
     *
     * @param group a group just added
     */
    public void grouped(AggregateItem group);

    /**
     * Notify that a group has been removed.
     *
     * @param group a group has been removed
     * @param olds  members were contained in the group <b>before being removed</b>
     */
    public void ungrouped(AggregateItem group, List<NodeItem> olds);
}
