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
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import kobic.prefuse.NodeShape;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class NodeShapeEditor extends EnumTagsPropertyEditor<NodeShape>
        implements ExPropertyEditor, VetoableChangeListener {

    private PropertyEnv env;

    public NodeShapeEditor() {
        super(NodeShape.class);
    }

    @Override
    public void setAsText(String name) throws IllegalArgumentException {
        setValue(NodeShape.get(name));
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener(this);
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        if (chooser == null) {
            chooser = new JList(NodeShape.values());
            chooser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            chooser.setCellRenderer(new ShapeListCellRenderer());
            chooser.setBorder(BorderFactory.createEmptyBorder(5, 5, 8, 10));
            chooser.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    NodeShape selected;
                    if (e.getValueIsAdjusting() || (selected = (NodeShape) chooser.getSelectedValue()) == shape) {
                        return;
                    }
                    shape = selected;
                    /*
                     * Enable to preview the change
                     */
                    env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                    env.setState(PropertyEnv.STATE_VALID);
                }
            });
        }
        shape = (NodeShape) getValue();
        chooser.setSelectedValue(shape, true);
        return chooser;
    }
    private NodeShape shape = null;
    private JList chooser = null;

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (shape != null && shape != getValue()) {
            setValue(shape);
        }
    }

    static class ShapeListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(SampleIcon.get((NodeShape) value));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setText(value.toString());
            return label;
        }

        static class SampleIcon implements Icon {

            private NodeShape shape;

            SampleIcon(NodeShape shape) {
                this.shape = shape;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Shape s = shape.getSample(x + 3, y + 3, getIconWidth() - 6, getIconHeight() - 6);
                Graphics2D g2d = (Graphics2D) g;
                Object origAa = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Stroke origStroke = g2d.getStroke();
                Color origColor = g2d.getColor();
                g2d.setStroke(StrokeLib.getStroke(2.0F));
                g2d.setColor(c.getForeground());
                g2d.draw(s);
                g2d.setColor(ColorLib.getColor(ColorLib.setAlpha(ColorLib.color(c.getForeground()), 100)));
                g2d.fill(s);
                g2d.setStroke(origStroke);
                g2d.setColor(origColor);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, origAa);
            }

            @Override
            public int getIconWidth() {
                return 30;
            }

            @Override
            public int getIconHeight() {
                return 30;
            }

            static Icon get(NodeShape shape) {
                Icon icon = icons.get(shape);
                if (icon == null) {
                    icons.put(shape, icon = new SampleIcon(shape));
                }
                return icon;
            }
            private static final Map<NodeShape, Icon> icons = new EnumMap<NodeShape, Icon>(NodeShape.class);
        }
    }
}
