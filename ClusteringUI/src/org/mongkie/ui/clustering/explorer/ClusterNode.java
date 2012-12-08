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
package org.mongkie.ui.clustering.explorer;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.mongkie.clustering.ClusteringController;
import org.mongkie.clustering.spi.Cluster;
import org.mongkie.visualization.color.ColorController;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ClusterNode extends AbstractNode {

    public ClusterNode(Cluster c) {
        super(Children.LEAF, Lookups.singleton(c));
        setDisplayName(c.getName());
    }

    @Override
    protected Sheet createSheet() {
        final Cluster c = getCluster();

        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property<Color> colorProperty = new PropertySupport.ReadWrite<Color>(
                NbBundle.getMessage(ClusterNode.class, "ClusterNode.property.color.name"), Color.class,
                NbBundle.getMessage(ClusterNode.class, "ClusterNode.property.color.displayName"), null) {

            @Override
            public Color getValue() throws IllegalAccessException, InvocationTargetException {
                return c.getColor();
            }

            @Override
            public void setValue(Color val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                c.setColor(val);
                if (Lookup.getDefault().lookup(ClusteringController.class).isGrouped(c)) {
                    Lookup.getDefault().lookup(ColorController.class).setFillColor(
                            Lookup.getDefault().lookup(ClusteringController.class).getGroup(c), val);
                }
            }
        };
        colorProperty.setValue("canEditAsText", false);
        colorProperty.setValue("suppressCustomEditor", true);
        set.put(colorProperty);
        Property<String> nameProperty = new PropertySupport.ReadWrite<String>(
                NbBundle.getMessage(ClusterNode.class, "ClusterNode.property.name.name"), String.class,
                NbBundle.getMessage(ClusterNode.class, "ClusterNode.property.name.displayName"), null) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return c.getName();
            }

            @Override
            public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                c.setName(val);
            }
        };
        nameProperty.setValue("suppressCustomEditor", true);
        set.put(nameProperty);
        Property<String> sizeProperty = new PropertySupport.ReadOnly<String>(
                NbBundle.getMessage(ClusterNode.class, "ClusterNode.property.size.name"), String.class,
                NbBundle.getMessage(ClusterNode.class, "ClusterNode.property.size.displayName"), null) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                int nodesCount = c.getNodesCount();
                return nodesCount + String.format(" (%.2f%%)", (nodesCount / (float) c.getGraph().getNodeCount()) * 100);
            }
        };
        sizeProperty.setValue("suppressCustomEditor", true);
        set.put(sizeProperty);

        sheet.put(set);
        return sheet;
    }

    public Cluster getCluster() {
        return getLookup().lookup(Cluster.class);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>(Utilities.actionsForPath("Actions/Clustering"));
        actions.addAll(Arrays.asList(super.getActions(context)));
        return actions.toArray(new Action[actions.size()]);
    }
}
