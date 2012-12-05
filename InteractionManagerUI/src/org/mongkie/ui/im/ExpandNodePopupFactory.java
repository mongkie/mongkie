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
package org.mongkie.ui.im;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import kobic.prefuse.controls.PopupControl;
import org.mongkie.im.InteractionController;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.ui.visualization.menu.spi.NodePopupMenuItemFactory;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = NodePopupMenuItemFactory.class)
public class ExpandNodePopupFactory extends NodePopupMenuItemFactory {

    @Override
    public List<JMenuItem> createMenuItems(final PopupControl<MongkieDisplay> control) {
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        JMenu m = new JMenu("More Interactions");
        m.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/interaction.png", false));
        final InteractionController ic = Lookup.getDefault().lookup(InteractionController.class);
        for (String category : ic.getCategories()) {
            for (final InteractionSource is : ic.getInteractionSources(category)) {
                m.add(new AbstractAction(is.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TupleSet focusedTupleSet = control.getDisplay().getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
                        NodeItem clickedItem = (NodeItem) control.getClickedItem();
                        if (focusedTupleSet.containsTuple(clickedItem)) {
                            Object[] keys = new Object[focusedTupleSet.getTupleCount()];
                            int i = 0;
                            for (Iterator<NodeItem> itemIter = focusedTupleSet.tuples(); itemIter.hasNext();) {
                                keys[i++] = itemIter.next().get(ic.getModel(is).getKeyField());
                            }
                            ic.executeExpand(is, keys);
                        } else {
                            ic.executeExpand(is, clickedItem.get(ic.getModel(is).getKeyField()));
                        }
                    }
                });
            }
        }
        m.getPopupMenu().setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        menuItems.add(m);
        return menuItems;
    }
}
