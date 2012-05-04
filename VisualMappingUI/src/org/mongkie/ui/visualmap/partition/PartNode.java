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
package org.mongkie.ui.visualmap.partition;

import java.awt.Color;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualmap.partition.PartitionController;
import org.mongkie.visualmap.spi.partition.Part;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PartNode extends AbstractNode {

    public PartNode(Part p) {
        super(Children.create(new VisualEditChildFactory(p), true), Lookups.singleton(p));
        setDisplayName(Lookup.getDefault().lookup(PartitionController.class).getName(p));
    }

    @Override
    protected Sheet createSheet() {
        final Part p = getPart();

        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();

        Property<String> sizeProperty = new PropertySupport.ReadOnly<String>(
                NbBundle.getMessage(PartNode.class, "PartNode.column.visual.name"), String.class,
                NbBundle.getMessage(PartNode.class, "PartNode.column.visual.displayName"), null) {

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                return p.size() + String.format(" (%.2f%%)", p.getPortion());
            }
        };
        sizeProperty.setValue("suppressCustomEditor", true);
        set.put(sizeProperty);
        Property<Color> colorProperty = new PropertySupport.ReadWrite<Color>(
                NbBundle.getMessage(PartNode.class, "PartNode.column.color.name"), Color.class,
                NbBundle.getMessage(PartNode.class, "PartNode.column.color.displayName"), null) {

            @Override
            public Color getValue() throws IllegalAccessException, InvocationTargetException {
                return p.getColor();
            }

            @Override
            public void setValue(Color val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                p.setColor(val);
                if (Lookup.getDefault().lookup(PartitionController.class).isGrouped(p)) {
                    Lookup.getDefault().lookup(ColorController.class).setFillColor(
                            Lookup.getDefault().lookup(PartitionController.class).getGroup(p), val);
                }
            }
        };
        colorProperty.setValue("canEditAsText", false);
        colorProperty.setValue("suppressCustomEditor", true);
        set.put(colorProperty);

        sheet.put(set);
        return sheet;
    }

    public Part getPart() {
        return getLookup().lookup(Part.class);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/mongkie/ui/visualmap/resources/chart_bar.png");
//        return EMPTY_ICON;
    }
    private static final Image EMPTY_ICON = ImageUtilities.icon2Image(new EmptyIcon(1, 1));

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>(Utilities.actionsForPath("Actions/Partition"));
        actions.addAll(Arrays.asList(super.getActions(context)));
        return actions.toArray(new Action[actions.size()]);
    }
}
