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

import java.awt.Color;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualization.color.ColorModel;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.openide.util.Lookup;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GroupManager implements WorkspaceListener, TableListener, DisplayListener {

    private final List<GroupListener> listeners;
    private final Map<AggregateItem, List<NodeItem>> groups;

    public GroupManager() {
        listeners = Collections.synchronizedList(new ArrayList<GroupListener>());
        groups = Collections.synchronizedMap(new HashMap<AggregateItem, List<NodeItem>>());
    }

    public void addGroupListener(GroupListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeGroupListener(GroupListener l) {
        listeners.remove(l);
    }

    public AggregateItem group(Collection<Node> nodes, String name) {
        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        return d != null ? d.aggregateNodes(nodes, name) : null;
    }

    public AggregateItem group(Collection<Node> nodes, String name, Color c) {
        AggregateItem group = group(nodes, name);
        if (group != null && c != null) {
            Lookup.getDefault().lookup(ColorController.class).setFillColor(group, c);
        }
        return group;
    }

    public void ungroup(AggregateItem aggrItem) {
        //TODO require to check display equaility elsewhere
        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            d.unaggregateItems(aggrItem);
        }
    }

    public List<NodeItem> getNodeItems(AggregateItem group) {
        return groups.get(group);
    }

    @Override
    public void tableChanged(Table t, int start, int end, int col, int type) {
        if (col != EventConstants.ALL_COLUMNS) {
            return;
        }
        for (int i = start; i < end + 1; i++) {
            AggregateItem group = (AggregateItem) t.getTuple(i);
            List<NodeItem> members = groups.get(group);
            switch (type) {
                case EventConstants.UPDATE:
                    synchronized (listeners) {
                        for (Iterator<GroupListener> iter = listeners.iterator(); iter.hasNext();) {
                            iter.next().memberChanged(group, members);
                        }
                    }
                    if (group.getAggregateSize() > 0) {
                        members.clear();
                        for (Iterator<NodeItem> memberIter = group.items(); memberIter.hasNext();) {
                            members.add(memberIter.next());
                        }
                    }
                    break;
                case EventConstants.INSERTED:
                    if (groups.containsKey(group)) {
                        Logger.getLogger(GroupManager.class.getName()).log(Level.WARNING,
                                "The group exists already: {0}", group.getString(AggregateItem.AGGR_NAME));
                        groups.get(group).clear();
                    } else {
                        groups.put(group, new ArrayList<NodeItem>());
                    }
                    synchronized (listeners) {
                        for (Iterator<GroupListener> iter = listeners.iterator(); iter.hasNext();) {
                            iter.next().grouped(group);
                        }
                    }
                    break;
                case EventConstants.DELETE:
                    synchronized (listeners) {
                        for (Iterator<GroupListener> iter = listeners.iterator(); iter.hasNext();) {
                            iter.next().ungrouped(group, members);
                        }
                    }
                    if (groups.containsKey(group)) {
                        ColorModel colorModel = Lookup.getDefault().lookup(ColorController.class).getModel();
                        colorModel.getGroupColorProvider().removeFillColor(group);
                        for (NodeItem nodeItem : members) {
                            //TODO virtual aggregates produce invalid memebrs on deletion, ex. PathwayDisplay
                            if (nodeItem.isValid()) {
                                colorModel.getGroupColorProvider().removeStrokeColor(nodeItem);
                                PrefuseLib.update(nodeItem, VisualItem.STROKECOLOR, ColorLib.color(colorModel.getNodeColorProvider().getStrokeColor(nodeItem)));
                            }
                        }
                        members.clear();
                        groups.remove(group);
                    } else {
                        Logger.getLogger(GroupManager.class.getName()).log(Level.WARNING,
                                "The group does not exist: {0}", group.getString(AggregateItem.AGGR_NAME));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void displaySelected(MongkieDisplay display) {
        display.addDisplayListener(this);
        for (Iterator<AggregateItem> aggregateIter = display.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS).tuples();
                aggregateIter.hasNext();) {
            AggregateItem group = aggregateIter.next();
            if (!groups.containsKey(group)) {
                List<NodeItem> members = new ArrayList<NodeItem>();
                for (Iterator<NodeItem> memberIter = group.items(); memberIter.hasNext();) {
                    members.add(memberIter.next());
                }
                groups.put(group, members);
            }
        }
        ((AggregateTable) display.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS)).addTableListener(this);
    }

    @Override
    public void displayDeselected(MongkieDisplay display) {
        display.removeDisplayListener(this);
        ((AggregateTable) display.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS)).removeTableListener(this);
    }

    @Override
    public void displayClosed(MongkieDisplay display) {
    }

    @Override
    public void displayClosedAll() {
    }

    @Override
    public void graphDisposing(NetworkDisplay d, Graph g) {
    }

    @Override
    public void graphChanged(NetworkDisplay d, Graph g) {
        displaySelected((MongkieDisplay) d);
    }
}
