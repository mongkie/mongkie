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
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class HyperlinkPropertyEditor extends PropertyEditorSupport
        implements ExPropertyEditor, InplaceEditor.Factory {

    public HyperlinkPropertyEditor() {
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplace == null) {
            inplace = new Inplace();
        }
        return inplace;
    }
    private InplaceEditor inplace = null;

//    @Override
//    public boolean isPaintable() {
//        return true;
//    }
//
//    @Override
//    public void paintValue(Graphics gfx, Rectangle box) {
//    }
//
    public static class Inplace extends JLabel implements InplaceEditor {

        private ActionListener listener = null;
        private PropertyEditor editor = null;
        private PropertyModel model = null;

        private Inplace() {
            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                    if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                        try {
                            java.net.URI uri = new java.net.URI("http://" + getValue());
                            desktop.browse(uri);
                        } catch (IOException ex) {
                            Logger.getLogger(HyperlinkPropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (URISyntaxException ex) {
                            Logger.getLogger(HyperlinkPropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                }
            });
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public static Inplace getInstance() {
            return Holder.inplace;

        }

        private static class Holder {

            private static final Inplace inplace = new Inplace();
        }

        @Override
        public void connect(PropertyEditor pe, PropertyEnv env) {
            editor = pe;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return this;
        }

        @Override
        public void clear() {
            editor = null;
            model = null;
            listener = null;
        }

        @Override
        public Object getValue() {
            return editor.getValue();
        }

        @Override
        public void setValue(Object o) {
            setText("http://" + o);
        }

        @Override
        public boolean supportsTextEntry() {
            return false;
        }

        @Override
        public void reset() {
            setText("<html><a href=\"" + editor.getValue() + "\">http://" + editor.getValue() + "</a></html>");
        }

        @Override
        public void addActionListener(ActionListener l) {
            listener = l;
        }

        @Override
        public void removeActionListener(ActionListener l) {
            if (listener == l) {
                listener = null;
            }
        }

        @Override
        public KeyStroke[] getKeyStrokes() {
            return new KeyStroke[0];
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return editor;
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
