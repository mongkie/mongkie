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
import java.util.*;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.color.ColorController;
import org.openide.util.Lookup;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GroupingSupport<T> implements GroupListener {

    private final GroupingSupportable<T> supportable;
    private final GroupIndex<T> groupIndex;
    private boolean visible;
    private static final List<GroupingSupport> SUPPORTS = Collections.synchronizedList(new ArrayList<GroupingSupport>());

    public GroupingSupport(GroupingSupportable<T> supportable) {
        this(supportable, true);
    }

    public GroupingSupport(GroupingSupportable<T> supportable, boolean visible) {
        this.supportable = supportable;
        groupIndex = new GroupIndex<T>();
        this.visible = visible;
        Lookup.getDefault().lookup(VisualizationController.class).getGroupManager().addGroupListener(GroupingSupport.this);
        SUPPORTS.add(GroupingSupport.this);
    }

    public AggregateItem group(T groupable) {
        if (isGrouped(groupable)) {
            return getGroup(groupable);
        }
        AggregateItem group = Lookup.getDefault().lookup(VisualizationController.class).getGroupManager().group(
                supportable.getNodes(groupable), supportable.getName(groupable), supportable.getColor(groupable));
        if (group != null && group.isValid()) {
            groupIndex.put(groupable, group);
            if (!visible) {
                PrefuseLib.updateVisible(group, false);
            }
            return group;
        }
        return null;
    }

    public void ungroup(T groupable) {
        if (!isGrouped(groupable)) {
            return;
        }
        AggregateItem group = groupIndex.get(groupable);
        Lookup.getDefault().lookup(VisualizationController.class).getGroupManager().ungroup(group);
    }

    public boolean isGrouped(T groupable) {
        return groupIndex.containsKey(groupable);
    }

    public AggregateItem getGroup(T groupable) {
        return !isGrouped(groupable) ? null : groupIndex.get(groupable);
    }

    @Override
    public void grouped(AggregateItem group) {
    }

    @Override
    public void memberChanged(AggregateItem group, List<NodeItem> olds) {
    }

    @Override
    public void ungrouped(AggregateItem group, List<NodeItem> olds) {
        if (groupExists(group)) {
            groupIndex.remove(group);
            supportable.ungrouped(group);
            // Re-color overlapped nodes after a group removed
            synchronized (SUPPORTS) {
                for (Iterator<GroupingSupport> supports = SUPPORTS.iterator(); supports.hasNext();) {
                    supports.next().recolorizeGroups();
                }
            }
        }
    }

    private void recolorizeGroups() {
        for (T groupable : groupIndex.index.keySet()) {
            Color c = supportable.getColor(groupable);
            if (c != null) {
                Lookup.getDefault().lookup(ColorController.class).setFillColor(groupIndex.get(groupable), c);
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if (this.visible == visible) {
            return;
        }
        for (AggregateItem group : groupIndex.index.values()) {
            PrefuseLib.updateVisible(group, visible);
        }
    }

    public boolean groupExists(AggregateItem group) {
        return groupIndex.containsKey(group);
    }

    public void stopGroupListening() {
        Lookup.getDefault().lookup(VisualizationController.class).getGroupManager().removeGroupListener(GroupingSupport.this);
    }

    public void restartGroupListening() {
        stopGroupListening();
        Lookup.getDefault().lookup(VisualizationController.class).getGroupManager().addGroupListener(GroupingSupport.this);
    }

    private static class GroupIndex<T> {

        private final Map<T, AggregateItem> index = new LinkedHashMap<T, AggregateItem>();
        private final Map<AggregateItem, T> inverse = new LinkedHashMap<AggregateItem, T>();

        void put(T g, AggregateItem group) {
            index.put(g, group);
            inverse.put(group, g);
        }

        void remove(T g) {
            AggregateItem group = index.get(g);
            index.remove(g);
            inverse.remove(group);
        }

        void remove(AggregateItem group) {
            T g = inverse.get(group);
            index.remove(g);
            inverse.remove(group);
        }

        AggregateItem get(T g) {
            return index.get(g);
        }

        T get(AggregateItem group) {
            return inverse.get(group);
        }

        boolean containsKey(T g) {
            return index.containsKey(g);
        }

        boolean containsKey(AggregateItem group) {
            return inverse.containsKey(group);
        }
    }
}
