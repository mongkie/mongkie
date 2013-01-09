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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import kobic.prefuse.controls.PopupControl;
import org.mongkie.im.InteractionController;
import org.mongkie.im.SourceModel;
import org.mongkie.im.SourceModelChangeListener;
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
        final JMenu menu = new JMenu("More Interactions");
        menu.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/interaction.png", false));
        InteractionController ic = Lookup.getDefault().lookup(InteractionController.class);
        ic.addModelChangeListener(control.getDisplay(), new SourceModelChangeListener() {
            @Override
            public void modelAdded(SourceModel model) {
                addExapndAction(menu, model.getInteractionSource(), control);
            }

            @Override
            public void modelRemoved(SourceModel model) {
                for (Component c : menu.getMenuComponents()) {
                    if (c instanceof JMenuItem
                            && model.getInteractionSource().equals(((JMenuItem) c).getClientProperty(InteractionSource.class))) {
                        menu.remove(c);
                        menu.getPopupMenu().removePopupMenuListener(
                                (PopupMenuListener) ((JMenuItem) c).getClientProperty(PopupMenuListener.class));
                    }
                }
            }
        });
        for (String category : ic.getCategories()) {
            for (InteractionSource is : ic.getInteractionSources(category)) {
                addExapndAction(menu, is, control);
            }
        }
        menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        menuItems.add(menu);
        return menuItems;
    }

    private void addExapndAction(final JMenu menu, final InteractionSource is, final PopupControl<MongkieDisplay> control) {
        final InteractionController ic = Lookup.getDefault().lookup(InteractionController.class);
        final JMenuItem mi = new JMenuItem(new AbstractAction(is.getName()) {
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
        mi.putClientProperty(InteractionSource.class, is);
        menu.add(mi);
        PopupMenuListener l = new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                mi.setEnabled(ic.getModel(is).getKeyField() != null);
                mi.setToolTipText(mi.isEnabled() ? "Add more interactions from " + is.getName() : "Key field is not available");
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        };
        mi.putClientProperty(PopupMenuListener.class, l);
        menu.getPopupMenu().addPopupMenuListener(l);
    }
}
