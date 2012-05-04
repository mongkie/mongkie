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
package org.mongkie.visualedit.editors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.mongkie.visualedit.editors.util.CleanComboUI;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @param <E>
 */
public class EnumTagsPropertyEditor<E extends Enum<E>> extends PropertyEditorSupport {

    private final Class<E> enumClass;
    private final String[] tags;
    public static final String NULL = "?";

    public EnumTagsPropertyEditor(Class<E> enumClass) {
        this.enumClass = enumClass;
        E[] es = enumClass.getEnumConstants();
        tags = new String[es.length];
        for (int i = 0; i < es.length; i++) {
            tags[i] = es[i].toString();
        }
    }

    @Override
    public String getAsText() {
        E e = (E) getValue();
        return e == null ? NULL : e.toString();
    }

    @Override
    public void setAsText(String name) throws IllegalArgumentException {
        if (!name.equals(NULL)) {
            setValue(Enum.valueOf(enumClass, name));
        }
    }

    @Override
    public String[] getTags() {
        return tags;
    }

    protected static class ComboInplaceEditor<E extends Enum<E>> extends JComboBox implements InplaceEditor {

        private transient boolean inSetUI = false;
        private PropertyEditor pe = null;
        private PropertyModel model = null;

        public ComboInplaceEditor(Class<E> enumClass) {
            for (E e : enumClass.getEnumConstants()) {
                addItem(e);
            }
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    for (ActionListener l : listenerList.getListeners(ActionListener.class)) {
                        if (this != l) {
                            l.actionPerformed(new ActionEvent(ComboInplaceEditor.this, 0, COMMAND_SUCCESS));
                        }
                    }
                }
            });
            addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    for (ActionListener l : listenerList.getListeners(ActionListener.class)) {
                        l.actionPerformed(new ActionEvent(ComboInplaceEditor.this, 0, COMMAND_FAILURE));
                    }
                }
            });
        }

        @Override
        public void setUI(ComboBoxUI ui) {
            inSetUI = true;

            try {
                super.setUI(ui);
            } finally {
                inSetUI = false;
            }
        }

        @Override
        public void updateUI() {
            LookAndFeel lf = UIManager.getLookAndFeel();
            String id = lf.getID();
            boolean useClean = lf instanceof MetalLookAndFeel
                    || "GTK".equals(id) //NOI18N
                    || ("Aqua".equals(id) && "10.5".compareTo(System.getProperty("os.version")) <= 0) //NOI18N
                    || "Kunststoff".equals(id); //NOI18N

            if (useClean) {
                super.setUI(new CleanComboUI(true));
            } else {
                super.updateUI();
            }

            if (getEditor().getEditorComponent() instanceof JComponent) {
                ((JComponent) getEditor().getEditorComponent()).setBorder(null);
            }
        }

        @Override
        public void addFocusListener(FocusListener fl) {
            if (!inSetUI) {
                super.addFocusListener(fl);
            }
        }

        @Override
        public void connect(PropertyEditor pe, PropertyEnv env) {
            this.pe = pe;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return this;
        }

        @Override
        public void clear() {
            pe = null;
            model = null;
        }

        @Override
        public Object getValue() {
            return getSelectedItem();
        }

        @Override
        public void setValue(Object o) {
            setSelectedItem(o);
        }

        @Override
        public boolean supportsTextEntry() {
            return true;
        }

        @Override
        public void reset() {
            E e = (E) pe.getValue();
            if (getSelectedItem() != e) {
                setSelectedItem(e);
            }
        }

        @Override
        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return pe;
        }

        @Override
        public PropertyModel getPropertyModel() {
            return model;
        }

        @Override
        public void setPropertyModel(PropertyModel pm) {
            this.model = pm;
        }

        @Override
        public boolean isKnownComponent(Component c) {
            return c == this || isAncestorOf(c);
        }
    }
}
