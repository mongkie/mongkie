/* 
 * JCommon : a free general purpose class library for the Java(tm) platform
 * 
 *
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * -----------------------
 * StrokeChooserPanel.java
 * -----------------------
 * (C) Copyright 2000-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Dirk Zeitz;
 *
 * $Id: StrokeChooserPanel.java,v 1.8 2009/02/27 13:58:41 mungady Exp $
 *
 * Changes (from 26-Oct-2001)
 * --------------------------
 * 26-Oct-2001 : Changed package to com.jrefinery.ui.*;
 * 14-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 16-Mar-2004 : Fix for focus problems (DZ);
 * 27-Feb-2009 : Fixed bug 2612649, NullPointerException (DG);
 *
 */
package org.mongkie.lib.widgets;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import static org.mongkie.lib.widgets.WidgetUtilities.*;

/**
 * A component for choosing a stroke from a list of available strokes.
 *
 * @author David Gilbert
 */
public class StrokeChooserPanel extends JPanel {

    /**
     * A combo for selecting the stroke.
     */
    private JComboBox selector;

    /**
     * Creates a panel containing a combo-box that allows the user to select one
     * stroke from a list of available strokes.
     *
     * @param current the current stroke sample.
     * @param available an array of 'available' stroke samples.
     */
    public StrokeChooserPanel(StrokeSample current, StrokeSample[] available) {
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setLayout(new BorderLayout());
        // we've changed the behaviour here to populate the combo box
        // with Stroke objects directly - ideally we'd change the signature
        // of the constructor too...maybe later.
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i < available.length; i++) {
            model.addElement(available[i].getStroke());
        }
        this.selector = new JComboBox(model);
        this.selector.setSelectedItem(current.getStroke());
        this.selector.setRenderer(new StrokeSample(null));
        add(this.selector);
        // Changes due to focus problems!! DZ
        this.selector.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent evt) {
                getSelector().transferFocus();
            }
        });
    }

    /**
     * Returns the selector component.
     *
     * @return Returns the selector.
     */
    public final JComboBox getSelector() {
        return this.selector;
    }

    /**
     * Returns the selected stroke.
     *
     * @return The selected stroke (possibly
     * <code>null</code>).
     */
    public Stroke getSelectedStroke() {
        return (Stroke) this.selector.getSelectedItem();
    }

    /*
     * JCommon : a free general purpose class library for the Java(tm) platform
     *
     *
     * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
     *
     * Project Info: http://www.jfree.org/jcommon/index.html
     *
     * This library is free software; you can redistribute it and/or modify it
     * under the terms of the GNU Lesser General Public License as published by
     * the Free Software Foundation; either version 2.1 of the License, or (at
     * your option) any later version.
     *
     * This library is distributed in the hope that it will be useful, but
     * WITHOUT ANY WARRANTY; without even the implied warranty of
     * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
     * General Public License for more details.
     *
     * You should have received a copy of the GNU Lesser General Public License
     * along with this library; if not, write to the Free Software Foundation,
     * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
     *
     * [Java is a trademark or registered trademark of Sun Microsystems, Inc. in
     * the United States and other countries.]
     *
     * ----------------- StrokeSample.java ----------------- (C) Copyright
     * 2000-2009, by Object Refinery Limited.
     *
     * Original Author: David Gilbert (for Object Refinery Limited);
     * Contributor(s): -;
     *
     * $Id: StrokeSample.java,v 1.5 2009/02/27 13:58:41 mungady Exp $
     *
     * Changes (from 26-Oct-2001) -------------------------- 26-Oct-2001 :
     * Changed package to com.jrefinery.ui.*; 14-Oct-2002 : Fixed errors
     * reported by Checkstyle (DG); 21-Mar-2003 : Fixed null pointer exception,
     * bug 705126 (DG);
     *
     */
    /**
     * A panel that displays a stroke sample.
     *
     * @author David Gilbert
     */
    public static class StrokeSample extends JComponent implements ListCellRenderer {

        /**
         * The stroke being displayed (may be null).
         */
        private Stroke stroke;
        /**
         * The preferred size of the component.
         */
        private Dimension preferredSize;

        /**
         * Creates a StrokeSample for the specified stroke.
         *
         * @param stroke the sample stroke (
         * <code>null</code> permitted).
         */
        public StrokeSample(final Stroke stroke) {
            this.stroke = stroke;
            this.preferredSize = new Dimension(100, 24);
            setPreferredSize(this.preferredSize);
        }

        /**
         * Creates arrays that contains newly created StrokeSample instances.
         *
         * @param strokes sample strokes
         * @return array of stroke samples
         */
        public static StrokeSample[] createSamples(Stroke[] strokes) {
            StrokeSample[] samples = new StrokeSample[strokes.length];
            for (int i = 0; i < strokes.length; i++) {
                samples[i] = new StrokeSample(strokes[i]);
            }
            return samples;
        }

        /**
         * Returns the current Stroke object being displayed.
         *
         * @return The stroke (possibly
         * <code>null</code>).
         */
        public Stroke getStroke() {
            return this.stroke;
        }

        /**
         * Sets the stroke object being displayed and repaints the component.
         *
         * @param stroke the stroke (
         * <code>null</code> permitted).
         */
        public void setStroke(final Stroke stroke) {
            this.stroke = stroke;
            repaint();
        }

        /**
         * Returns the preferred size of the component.
         *
         * @return the preferred size of the component.
         */
        @Override
        public Dimension getPreferredSize() {
            return this.preferredSize;
        }

        /**
         * Draws a line using the sample stroke.
         *
         * @param g the graphics device.
         */
        @Override
        public void paintComponent(final Graphics g) {
            if (this.stroke != null) {
                final Dimension size = getSize();
                final Insets insets = getInsets();
                final double xx = insets.left;
                final double yy = insets.top;
                final double ww = size.getWidth() - insets.left - insets.right;
                final double hh = size.getHeight() - insets.top - insets.bottom;
                paintStroke(this.stroke, (Graphics2D) g, xx, yy, ww, hh, getBackground(), getForeground());
            }
        }

        public static void paintStroke(Stroke stroke, Graphics2D g, double x, double y, double w, double h, Color bg, Color fg) {
            Object oldAntialias = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // draw background
            if (bg != null) {
                g.setColor(bg);
                g.fillRect((int) x, (int) y, (int) w, (int) h);
                g.setColor(fg);
            }
            // calculate point one
            final Point2D one = new Point2D.Double(x + 6, y + h / 2);
            // calculate point two
            final Point2D two = new Point2D.Double(x + w - 6, y + h / 2);
            // draw a circle at point one
            final Ellipse2D circle1 = new Ellipse2D.Double(one.getX() - 3, one.getY() - 3, 6, 6);
            final Ellipse2D circle2 = new Ellipse2D.Double(two.getX() - 4, two.getY() - 3, 6, 6);
            // draw a circle at point two
//            g.draw(circle1);
            g.fill(circle1);
//            g.draw(circle2);
            g.fill(circle2);
            // draw a line connecting the points
            final Line2D line = new Line2D.Double(one, two);
            g.setStroke(stroke);
            g.draw(line);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialias);
        }

        /**
         * Draws a line using the sample stroke.
         *
         * @param stroke a stroke for drawing a line
         * @param g the graphics
         * @param x the top-left x coordinate of the drawing area(rectangle)
         * @param y the top-left y coordinate of the drawing area(rectangle)
         * @param w the width of the drawing area(rectangle)
         * @param h the height of the drawing area(rectangle)
         */
        public static void paintStroke(Stroke stroke, Graphics2D g, double x, double y, double w, double h) {
            paintStroke(stroke, g, x, y, w, h, null, null);
        }

        /**
         * Returns a list cell renderer for the stroke, so the sample can be
         * displayed in a list or combo.
         *
         * @param list the list.
         * @param value the value.
         * @param index the index.
         * @param isSelected selected?
         * @param cellHasFocus focused?
         *
         * @return the component for rendering.
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setBackground(isSelected ? COMBOBOX_SELECTION_BACKGROUND : COMBOBOX_BACKGROUND);
            setForeground(isSelected ? COMBOBOX_SELECTION_FOREGROUND : COMBOBOX_FOREGROUND);
            setStroke(getStroke(value));
            return this;
        }

        protected Stroke getStroke(Object value) {
            return value instanceof Stroke ? (Stroke) value : null;
        }
    }
}
