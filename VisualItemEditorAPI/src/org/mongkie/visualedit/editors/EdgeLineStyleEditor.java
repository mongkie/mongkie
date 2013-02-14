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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import kobic.prefuse.EdgeStroke;
import org.mongkie.lib.widgets.StrokeChooserPanel.StrokeSample;
import org.mongkie.visualedit.editors.util.CleanComboUI;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class EdgeLineStyleEditor extends EnumTagsPropertyEditor<EdgeStroke>
        implements ExPropertyEditor, VetoableChangeListener, InplaceEditor.Factory {

    private PropertyEnv env;

    public EdgeLineStyleEditor() {
        super(EdgeStroke.class);
    }

    @Override
    public void setAsText(String name) throws IllegalArgumentException {
        setValue(EdgeStroke.get(name));
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener(this);
        env.registerInplaceEditorFactory(this);
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplace == null) {
            inplace = new ComboInplaceEditor(EdgeStroke.class);
            inplace.setRenderer(new StrokeSample(null) {

                @Override
                protected Stroke getStroke(Object value) {
                    return value instanceof EdgeStroke ? ((EdgeStroke) value).getStroke() : null;
                }
            });
        }
        return inplace;
    }
    private ComboInplaceEditor inplace = null;

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        Icon arrow = UIManager.getIcon("ComboBox.icon"); //NOI18N
        if (arrow == null) {
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                arrow = new CleanComboUI.AquaComboIcon();
            } else {
                arrow = new MetalComboBoxIcon();
            }
        }
        gfx.drawImage(ImageUtilities.icon2Image(arrow),
                box.x + box.width - arrow.getIconWidth() * 2 + 4,
                box.y + (box.height / 2) - (arrow.getIconHeight() / 2),
                null);
        String strokeName = getAsText();
        if (strokeName.equals(NULL)) {
            gfx.drawString(NULL, box.x, box.y + 14);
        } else {
            StrokeSample.paintStroke(EdgeStroke.get(strokeName).getStroke(),
                    (Graphics2D) gfx, box.x, box.y, box.width - arrow.getIconWidth() * 2, box.height);
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        if (chooser == null) {
            chooser = new JList(EdgeStroke.values());
            chooser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            chooser.setCellRenderer(new StrokeListCellRenderer());
            chooser.setBorder(BorderFactory.createEmptyBorder(5, 5, 8, 10));
            chooser.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    EdgeStroke selected;
                    if (e.getValueIsAdjusting() || (selected = (EdgeStroke) chooser.getSelectedValue()) == stroke) {
                        return;
                    }
                    stroke = selected;
                    /*
                     * Enable to preview the change
                     */
                    env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                    env.setState(PropertyEnv.STATE_VALID);
                }
            });
        }
        stroke = (EdgeStroke) getValue();
        chooser.setSelectedValue(stroke, true);
        return chooser;
    }
    private JList chooser = null;
    private EdgeStroke stroke = null;

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (stroke != null && !stroke.equals(getValue())) {
            setValue(stroke);
        }
    }

    static class StrokeListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(StrokeIcon.getIcon(((EdgeStroke) value).getStroke()));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setText(value.toString());
            return label;
        }

        static class StrokeIcon implements Icon {

            private Stroke stroke;

            StrokeIcon(Stroke stroke) {
                this.stroke = stroke;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                StrokeSample.paintStroke(stroke, (Graphics2D) g, x, y, getIconWidth(), getIconHeight(), c.getBackground(), c.getForeground());
            }

            @Override
            public int getIconWidth() {
                return 120;
            }

            @Override
            public int getIconHeight() {
                return 24;
            }

            static Icon getIcon(Stroke stroke) {
                Icon icon = icons.get(stroke);
                if (icon == null) {
                    icons.put(stroke, icon = new StrokeIcon(stroke));
                }
                return icon;
            }
            private static final Map<Stroke, Icon> icons = new HashMap<Stroke, Icon>();
        }
    }
}
