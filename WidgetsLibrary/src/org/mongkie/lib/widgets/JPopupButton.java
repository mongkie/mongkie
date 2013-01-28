/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.lib.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 */
public class JPopupButton extends JButton {

    private ArrayList<JPopupButtonItem> items;
    private JPopupButtonItem selectedItem;
    private ChangeListener listener;

    public JPopupButton() {

        items = new ArrayList<JPopupButtonItem>();
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu menu = createPopup();
                menu.show(JPopupButton.this, 0, getHeight());
            }
        });
    }

    public JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();
        for (final JPopupButtonItem item : items) {
            JRadioButtonMenuItem r = new JRadioButtonMenuItem(item.object.toString(), item.icon, item.equals(selectedItem));
            r.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (item != selectedItem) {
                        selectedItem = item;
                        fireChangeEvent();
                    }
                }
            });
            menu.add(r);
        }
        return menu;
    }

    public void clearItems() {
        clearItems(true);
    }

    public void clearItems(boolean fireEvent) {
        items.clear();
        selectedItem = null;
        if (fireEvent) {
            fireChangeEvent();
        }
    }

    public void addItem(Object object, Icon icon) {
        items.add(new JPopupButtonItem(object, icon));
    }

    public boolean setSelectedItem(Object item) {
        if (item == null) {
            fireChangeEvent();
            return true;
        }
        for (JPopupButtonItem i : items) {
            if (i.object.equals(item)) {
                if (i != selectedItem) {
                    selectedItem = i;
                    fireChangeEvent();
                    return true;
                }
                return false;
            }
        }
        throw new IllegalArgumentException("This elemen doesn't exist: " + item);
    }

    public Object getSelectedItem() {
        return selectedItem != null ? selectedItem.object : null;
    }

    public void setChangeListener(ChangeListener changeListener) {
        listener = changeListener;
    }

    private void fireChangeEvent() {
        if (listener != null) {
            listener.stateChanged(selectedItem != null ? new ChangeEvent(selectedItem.object) : null);
        }
    }

    private class JPopupButtonItem {

        private final Object object;
        private final Icon icon;

        public JPopupButtonItem(Object object, Icon icon) {
            this.object = object;
            this.icon = icon;
        }
    }
}
