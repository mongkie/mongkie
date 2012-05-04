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
package org.mongkie.ui.visualization.options;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import kobic.prefuse.Config;
import static kobic.prefuse.Constants.EDGES;
import kobic.prefuse.EdgeArrow;
import kobic.prefuse.EdgeStroke;
import org.mongkie.lib.widgets.JColorButton;
import org.mongkie.lib.widgets.StrokeChooserPanel.StrokeSample;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.color.ColorController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import static prefuse.Visualization.DRAW;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import static org.mongkie.lib.widgets.WidgetUtilities.*;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class EdgeSettingPanel extends javax.swing.JPanel {

    private final MongkieDisplay display;
    private boolean reset = false;
    private final PropertyEditor fontChooser = PropertyEditorManager.findEditor(Font.class);
    private Font currentFont = Config.FONT_DEFAULT_EDGETEXT;

    /** Creates new form EdgeSettingPanel */
    public EdgeSettingPanel(final MongkieDisplay display) {
        this.display = display;
        initComponents();

        lineChooser.setRenderer(new StrokeSample(null) {

            @Override
            protected Stroke getStroke(Object value) {
                return value instanceof EdgeStroke ? ((EdgeStroke) value).getStroke() : null;
            }
        });
        for (EdgeStroke s : EdgeStroke.values()) {
            lineChooser.addItem(s);
        }
        lineChooser.setSelectedItem(EdgeStroke.SOLID);
        lineChooser.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setLineStyle((EdgeStroke) e.getItem());
                }
            }
        });

        arrowChooser.setRenderer(new ArrowListCellRenderer());
        for (EdgeArrow a : EdgeArrow.values()) {
            arrowChooser.addItem(a);
        }
        arrowChooser.setSelectedItem(EdgeArrow.ARROW);
        arrowChooser.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setArrowType((EdgeArrow) e.getItem());
                }
            }
        });

        sizeSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setLineWidth(((Double) sizeSpinner.getValue()).doubleValue());
            }
        });

        fontButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fontChooser.setValue(getCurrentFont());
                DialogDescriptor dd = new DialogDescriptor(fontChooser.getCustomEditor(), "Edge Font");
                DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    setCurrentFont((Font) fontChooser.getValue());
                }
            }
        });

        hideLabelCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setLabelVisible(!hideLabelCheckBox.isSelected());
            }
        });

        ((JColorButton) colorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (reset) {
                            return;
                        }
                        display.getVisualization().rerun(new Runnable() {

                            @Override
                            public void run() {
                                Color c = ((JColorButton) colorButton).getColor();
                                for (Iterator<EdgeItem> edgesIter = display.getVisualization().items(EDGES); edgesIter.hasNext();) {
                                    EdgeItem e = edgesIter.next();
                                    Lookup.getDefault().lookup(ColorController.class).getModel().getEdgeColorProvider().addFillColor(e, c);
                                    Lookup.getDefault().lookup(ColorController.class).getModel().getEdgeColorProvider().addStrokeColor(e, c);
                                }
                            }
                        }, DRAW);
                    }
                });
        ((JColorButton) fontColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (reset) {
                            return;
                        }
                        display.getVisualization().rerun(new Runnable() {

                            @Override
                            public void run() {
                                Color c = ((JColorButton) fontColorButton).getColor();
                                for (Iterator<EdgeItem> edgesIter = display.getVisualization().items(EDGES); edgesIter.hasNext();) {
                                    Lookup.getDefault().lookup(ColorController.class).getModel().getEdgeColorProvider().addTextColor(edgesIter.next(), c);
                                }
                            }
                        }, DRAW);
                    }
                });
    }

    private void setLineStyle(EdgeStroke s) {
        for (Iterator<EdgeItem> edgesIter = display.getVisualization().items(EDGES); edgesIter.hasNext();) {
            EdgeItem e = edgesIter.next();
            BasicStroke stroke = s.getStroke();
            if (e.getStroke().equals(stroke)) {
                continue;
            }
            e.setStroke(stroke);
        }
        if (!reset) {
            display.getVisualization().repaint();
        }
    }

    private void setArrowType(EdgeArrow a) {
        for (Iterator<EdgeItem> edgesIter = display.getVisualization().items(EDGES); edgesIter.hasNext();) {
            EdgeItem e = edgesIter.next();
            int code = a.getCode();
            if (e.getShape() == code) {
                continue;
            }
            e.setShape(code);
        }
        if (!reset) {
            display.getVisualization().repaint();
        }
    }

    private void setLineWidth(double width) {
        for (Iterator<EdgeItem> edgesIter = display.getVisualization().items(EDGES); edgesIter.hasNext();) {
            EdgeItem e = edgesIter.next();
            if (e.getSize() == width) {
                continue;
            }
            PrefuseLib.updateDouble(e, VisualItem.SIZE, width);
        }
        if (!reset) {
            display.getVisualization().repaint();
        }
    }

    private void setCurrentFont(Font f) {
        fontButton.setText(f.getFontName() + ", " + f.getSize());
        for (Iterator<EdgeItem> edgesIter = display.getVisualization().items(EDGES); edgesIter.hasNext();) {
            EdgeItem e = edgesIter.next();
            if (!e.getFont().equals(f)) {
                PrefuseLib.update(e, VisualItem.FONT, f);
            }
        }
        currentFont = f;
        if (!reset) {
            display.getVisualization().repaint();
        }
    }

    private Font getCurrentFont() {
        return currentFont;
    }

    private void setLabelVisible(boolean visible) {
        display.setEdgeLabelVisible(visible);
        fontButton.setEnabled(visible);
        fontColorButton.setEnabled(visible);
        fontLabel.setEnabled(visible);
        if (reset) {
            hideLabelCheckBox.setSelected(!visible);
        } else {
            display.getVisualization().repaint();
        }

    }

    private void resetOptions() {
        reset = true;
        display.getVisualization().rerun(new Runnable() {

            @Override
            public void run() {
                lineChooser.setSelectedItem(EdgeStroke.SOLID);
                arrowChooser.setSelectedItem(EdgeArrow.ARROW);
                sizeSpinner.setValue(1.0D);
                setCurrentFont(Config.FONT_DEFAULT_EDGETEXT);
                setLabelVisible(true);
                Lookup.getDefault().lookup(ColorController.class).getModel().getEdgeColorProvider().clearFillColors();
                Lookup.getDefault().lookup(ColorController.class).getModel().getEdgeColorProvider().clearTextColors();
                Lookup.getDefault().lookup(ColorController.class).getModel().getEdgeColorProvider().clearStrokeColors();
                ((JColorButton) colorButton).setColor(ColorLib.getColor(Config.COLOR_DEFAULT_EDGE_STROKE));
                ((JColorButton) fontColorButton).setColor(ColorLib.getColor(Config.COLOR_DEFAULT_EDGE_TEXT));
            }
        }, DRAW);
        reset = false;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lineLabel = new javax.swing.JLabel();
        lineChooser = new javax.swing.JComboBox();
        arrowLabel = new javax.swing.JLabel();
        arrowChooser = new javax.swing.JComboBox();
        widthLabel = new javax.swing.JLabel();
        sizeSpinner = new javax.swing.JSpinner();
        colorLabel = new javax.swing.JLabel();
        colorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor(Config.COLOR_DEFAULT_EDGE_STROKE));
        fontLabel = new javax.swing.JLabel();
        fontButton = new javax.swing.JButton();
        fontColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor(Config.COLOR_DEFAULT_EDGE_TEXT));
        resetAllButton = new javax.swing.JButton();
        hideLabelCheckBox = new javax.swing.JCheckBox();

        lineLabel.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.lineLabel.text")); // NOI18N

        lineChooser.setFont(lineChooser.getFont().deriveFont(lineChooser.getFont().getSize()-1f));

        arrowLabel.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.arrowLabel.text")); // NOI18N

        arrowChooser.setFont(arrowChooser.getFont().deriveFont(arrowChooser.getFont().getSize()-1f));

        widthLabel.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.widthLabel.text")); // NOI18N

        sizeSpinner.setFont(sizeSpinner.getFont().deriveFont(sizeSpinner.getFont().getSize()-1f));
        sizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.5d), null, Double.valueOf(0.2d)));

        colorLabel.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.colorLabel.text")); // NOI18N

        colorButton.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.colorButton.text")); // NOI18N

        fontLabel.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.fontLabel.text")); // NOI18N

        fontButton.setFont(fontButton.getFont().deriveFont(fontButton.getFont().getSize()-1f));
        fontButton.setText(Config.FONT_DEFAULT_EDGETEXT.getFontName() + ", " + Config.FONT_DEFAULT_EDGETEXT.getSize());
        fontButton.setBorderPainted(false);

        fontColorButton.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.fontColorButton.text")); // NOI18N

        resetAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/undo.png"))); // NOI18N
        resetAllButton.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.resetAllButton.text")); // NOI18N
        resetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllButtonActionPerformed(evt);
            }
        });

        hideLabelCheckBox.setText(org.openide.util.NbBundle.getMessage(EdgeSettingPanel.class, "EdgeSettingPanel.hideLabelCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fontLabel)
                        .addGap(18, 18, 18)
                        .addComponent(fontButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fontColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(hideLabelCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lineLabel)
                            .addComponent(arrowLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(arrowChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lineChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(widthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(colorLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(resetAllButton))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(colorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colorLabel)
                    .addComponent(sizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(widthLabel)
                    .addComponent(lineChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lineLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(arrowChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(arrowLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(hideLabelCheckBox)
                    .addComponent(fontColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontButton)
                    .addComponent(fontLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resetAllButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllButtonActionPerformed
        if (DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation("All edge options will be reset. Continue?", NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
            resetOptions();
        }
    }//GEN-LAST:event_resetAllButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox arrowChooser;
    private javax.swing.JLabel arrowLabel;
    private javax.swing.JButton colorButton;
    private javax.swing.JLabel colorLabel;
    private javax.swing.JButton fontButton;
    private javax.swing.JButton fontColorButton;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JCheckBox hideLabelCheckBox;
    private javax.swing.JComboBox lineChooser;
    private javax.swing.JLabel lineLabel;
    private javax.swing.JButton resetAllButton;
    private javax.swing.JSpinner sizeSpinner;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables

    private static class ArrowListCellRenderer extends JComponent implements ListCellRenderer {

        private EdgeArrow arrow;
        private Dimension preferredSize;

        public ArrowListCellRenderer() {
            this(null);
        }

        public ArrowListCellRenderer(EdgeArrow arrow) {
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

    private static void paintEdgeArrow(EdgeArrow arrow, Graphics2D g, double x, double y, double w, double h, Color bg, Color fg, double scale) {
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
    private static final Stroke LINE_STROKE = new BasicStroke(1.0f);
    private static final AffineTransform HEAD_TRANS = new AffineTransform();
    private static final double HALF_PI = Math.PI / 2;
}
