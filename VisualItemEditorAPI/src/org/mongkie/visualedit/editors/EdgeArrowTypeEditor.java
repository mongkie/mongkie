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
package org.mongkie.visualedit.editors;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalComboBoxIcon;
import kobic.prefuse.EdgeArrow;
import static org.mongkie.lib.widgets.WidgetUtilities.*;
import org.mongkie.visualedit.editors.util.CleanComboUI;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class EdgeArrowTypeEditor extends EnumTagsPropertyEditor<EdgeArrow>
        implements ExPropertyEditor, VetoableChangeListener, InplaceEditor.Factory {

    private PropertyEnv env;

    public EdgeArrowTypeEditor() {
        super(EdgeArrow.class);
    }

    @Override
    public void setAsText(String name) throws IllegalArgumentException {
        setValue(EdgeArrow.get(name));
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addVetoableChangeListener(this);
        env.registerInplaceEditorFactory(this);
    }

    @Override
    public boolean isPaintable() {
        return true;
    }

    @Override
    public void paintValue(Graphics gfx, Rectangle box) {
        Icon comboArrow = UIManager.getIcon("ComboBox.icon"); //NOI18N
        if (comboArrow == null) {
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {
                comboArrow = new CleanComboUI.AquaComboIcon();
            } else {
                comboArrow = new MetalComboBoxIcon();
            }
        }
        gfx.drawImage(ImageUtilities.icon2Image(comboArrow),
                box.x + box.width - comboArrow.getIconWidth() * 2 + 4,
                box.y + (box.height / 2) - (comboArrow.getIconHeight() / 2),
                null);
        String arrowName = getAsText();
        if (arrowName.equals(NULL)) {
            gfx.drawString(NULL, box.x, box.y + 14);
        } else {
            paintEdgeArrow(EdgeArrow.get(arrowName),
                    (Graphics2D) gfx, box.x, box.y, box.width - comboArrow.getIconWidth() * 2, box.height, 0.7d);
        }
    }

    @Override
    public InplaceEditor getInplaceEditor() {
        if (inplace == null) {
            inplace = new ComboInplaceEditor(EdgeArrow.class);
            inplace.setRenderer(new InplaceArrowListCellRenderer());
        }
        return inplace;
    }
    private ComboInplaceEditor inplace = null;

    static class InplaceArrowListCellRenderer extends JComponent implements ListCellRenderer {

        private EdgeArrow arrow;
        private Dimension preferredSize;

        public InplaceArrowListCellRenderer() {
            this(null);
        }

        public InplaceArrowListCellRenderer(EdgeArrow arrow) {
            this.arrow = arrow;
            this.preferredSize = new Dimension(100, 24);
            setPreferredSize(preferredSize);
        }

        @Override
        public Dimension getPreferredSize() {
            return preferredSize;
        }

        @Override
        public void paintComponent(final Graphics g) {
            if (this.arrow != null) {
                final Dimension size = getSize();
                final Insets insets = getInsets();
                final double xx = insets.left;
                final double yy = insets.top;
                final double ww = size.getWidth() - insets.left - insets.right;
                final double hh = size.getHeight() - insets.top - insets.bottom;
                paintEdgeArrow(this.arrow, (Graphics2D) g, xx, yy, ww, hh, getBackground(), getForeground(), 0.7d);
            }
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setBackground(isSelected ? COMBOBOX_SELECTION_BACKGROUND : COMBOBOX_BACKGROUND);
            setForeground(isSelected ? COMBOBOX_SELECTION_FOREGROUND : COMBOBOX_FOREGROUND);
            this.arrow = value instanceof EdgeArrow ? (EdgeArrow) value : null;
            repaint();
            return this;

        }        
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        if (chooser == null) {
            chooser = new JList(EdgeArrow.values());
            chooser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            chooser.setCellRenderer(new CustomArrowListCellRenderer());
            chooser.setBorder(BorderFactory.createEmptyBorder(5, 5, 8, 10));
            chooser.addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    EdgeArrow selected;
                    if (e.getValueIsAdjusting() || (selected = (EdgeArrow) chooser.getSelectedValue()) == arrow) {
                        return;
                    }
                    arrow = selected;
                    /*
                     * Enable to preview the change
                     */
                    env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
                    env.setState(PropertyEnv.STATE_VALID);
                }
            });
        }
        arrow = (EdgeArrow) getValue();
        chooser.setSelectedValue(arrow, true);
        return chooser;
    }
    private JList chooser = null;
    private EdgeArrow arrow = null;

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (arrow != null && !arrow.equals(getValue())) {
            setValue(arrow);
        }
    }

    static class CustomArrowListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setIcon(ArrowIcon.get((EdgeArrow) value));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setText(value.toString());
            return label;
        }

        static class ArrowIcon implements Icon {

            private EdgeArrow arrow;

            ArrowIcon(EdgeArrow arrow) {
                this.arrow = arrow;
            }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                paintEdgeArrow(arrow, (Graphics2D) g, x, y, getIconWidth(), getIconHeight(), c.getBackground(), c.getForeground(), 1.0D);
            }

            @Override
            public int getIconWidth() {
                return 120;
            }

            @Override
            public int getIconHeight() {
                return 24;
            }

            static Icon get(EdgeArrow arrow) {
                Icon icon = icons.get(arrow);
                if (icon == null) {
                    icons.put(arrow, icon = new ArrowIcon(arrow));
                }
                return icon;
            }
            private static final Map<EdgeArrow, Icon> icons = new EnumMap<EdgeArrow, Icon>(EdgeArrow.class);
        }
    }

    protected static void paintEdgeArrow(EdgeArrow arrow, Graphics2D g, double x, double y, double w, double h) {
        paintEdgeArrow(arrow, g, x, y, w, h, null, null, 1.0d);

    }

    protected static void paintEdgeArrow(EdgeArrow arrow, Graphics2D g, double x, double y, double w, double h, double scale) {
        paintEdgeArrow(arrow, g, x, y, w, h, null, null, scale);

    }

    protected static void paintEdgeArrow(EdgeArrow arrow, Graphics2D g, double x, double y, double w, double h, Color bg, Color fg, double scale) {
        Object oldAntialias = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // draw background
        if (bg != null) {
            g.setColor(bg);
            g.fillRect((int) x, (int) y, (int) w, (int) h);
            g.setColor(fg);
        }
        // calculate point one
        final Point2D p1 = new Point2D.Double(x + 3, y + h / 2);
        // calculate point two
        final Point2D p2 = new Point2D.Double(x + w - 2, y + h / 2);
        g.setStroke(LINE_STROKE);
        g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX() - (int) (arrow.getGap() * scale), (int) p2.getY());
        if (arrow != EdgeArrow.NONE) {
            HEAD_TRANS.setToTranslation(p2.getX(), p2.getY());
            if (Double.compare(scale, 1.0d) != 0) {
                HEAD_TRANS.scale(scale, scale);
            }
            HEAD_TRANS.rotate(-HALF_PI + Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
            g.fill(HEAD_TRANS.createTransformedShape(arrow.getArrowHead()));
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialias);
    }
    protected static final Stroke LINE_STROKE = new BasicStroke(1.0f);
    protected static final AffineTransform HEAD_TRANS = new AffineTransform();
    protected static final double HALF_PI = Math.PI / 2;
}
