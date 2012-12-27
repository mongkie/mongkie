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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
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
public class ExpandNodePopupFactory extends NodePopupMenuItemFactory implements SourceModelChangeListener {

    private JMenu menu;
    private final Map<InteractionSource, JMenuItem> actions = new HashMap<InteractionSource, JMenuItem>();
    private PopupControl<MongkieDisplay> control;

    @Override
    public List<JMenuItem> createMenuItems(final PopupControl<MongkieDisplay> control) {
        if (menu != null) {
            menu.removeAll();
            actions.clear();
        }
        this.control = control;
        menu = new JMenu("More Interactions");
        menu.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/interaction.png", false));
        InteractionController ic = Lookup.getDefault().lookup(InteractionController.class);
        ic.addModelChangeListener(control.getDisplay(), this);
        for (String category : ic.getCategories()) {
            for (InteractionSource is : ic.getInteractionSources(category)) {
                addExapndAction(is);
            }
        }
        menu.getPopupMenu().setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
        menuItems.add(menu);
        return menuItems;
    }

    private void addExapndAction(final InteractionSource is) {
        final InteractionController ic = Lookup.getDefault().lookup(InteractionController.class);
        final Action a = new AbstractAction(is.getName()) {
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
        };
        menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                a.setEnabled(ic.getModel(is).getKeyField() != null);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                actions.put(is, menu.add(a));
            }
        });
    }

    @Override
    public void modelAdded(SourceModel model) {
        addExapndAction(model.getInteractionSource());
    }

    @Override
    public void modelRemoved(final SourceModel model) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                menu.remove(actions.remove(model.getInteractionSource()));
            }
        });
    }
}
