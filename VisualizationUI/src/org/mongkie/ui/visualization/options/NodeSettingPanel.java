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

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import kobic.prefuse.Config;
import static kobic.prefuse.Constants.NODES;
import kobic.prefuse.NodeShape;
import org.mongkie.lib.widgets.JColorButton;
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
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class NodeSettingPanel extends javax.swing.JPanel {

    private final MongkieDisplay display;
    private boolean reset = false;
    private final PropertyEditor fontChooser = PropertyEditorManager.findEditor(Font.class);
    private Font currentFont = Config.FONT_DEFAULT_NODETEXT;

    /** Creates new form NodeSettingPanel */
    public NodeSettingPanel(final MongkieDisplay display) {
        this.display = display;
        initComponents();

        for (NodeShape s : NodeShape.values()) {
            shapeChooser.addItem(s);
        }
        shapeChooser.setSelectedItem(NodeShape.ELLIPSE);
        shapeChooser.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setNodeShape((NodeShape) e.getItem());
                }
            }
        });
        
        sizeSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setNodeSize(((Double) sizeSpinner.getValue()).doubleValue());
            }
        });

        fontFamilyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fontChooser.setValue(getCurrentFont());
                DialogDescriptor dd = new DialogDescriptor(fontChooser.getCustomEditor(), "Node Font");
                DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    setCurrentFont((Font) fontChooser.getValue());
                }
            }
        });
        ((JColorButton) fillColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (reset) {
                            return;
                        }
                        display.getVisualization().rerun(new Runnable() {

                            @Override
                            public void run() {
                                Color c = ((JColorButton) fillColorButton).getColor();
                                for (Iterator<NodeItem> nodesIter = display.getVisualization().items(NODES); nodesIter.hasNext();) {
                                    NodeItem n = nodesIter.next();
                                    Lookup.getDefault().lookup(ColorController.class).getModel().getNodeColorProvider().addFillColor(n, c);
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
                                for (Iterator<NodeItem> nodesIter = display.getVisualization().items(NODES); nodesIter.hasNext();) {
                                    NodeItem n = nodesIter.next();
                                    Lookup.getDefault().lookup(ColorController.class).getModel().getNodeColorProvider().addTextColor(n, c);
                                }
                            }
                        }, DRAW);
                    }
                });
        ((JColorButton) borderColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (reset) {
                            return;
                        }
                        display.getVisualization().rerun(new Runnable() {

                            @Override
                            public void run() {
                                Color c = ((JColorButton) borderColorButton).getColor();
                                for (Iterator<NodeItem> nodesIter = display.getVisualization().items(NODES); nodesIter.hasNext();) {
                                    NodeItem n = nodesIter.next();
                                    Lookup.getDefault().lookup(ColorController.class).getModel().getNodeColorProvider().addStrokeColor(n, c);
                                }
                            }
                        }, DRAW);
                    }
                });
    }

    private void setNodeShape(NodeShape s) {
        int code = s.getCode();
        for (Iterator<NodeItem> nodesIter = display.getVisualization().items(NODES); nodesIter.hasNext();) {
            NodeItem n = nodesIter.next();
            if (n.getShape() == code) {
                continue;
            }
            n.setShape(code);
        }
        if (!reset) {
            display.getVisualization().repaint();
        }
    }
    
    private void setNodeSize(double size) {
        for (Iterator<NodeItem> nodesIter = display.getVisualization().items(NODES); nodesIter.hasNext();) {
            NodeItem n = nodesIter.next();
            if (n.getSize() == size) {
                continue;
            }
            PrefuseLib.updateDouble(n, VisualItem.SIZE, size);
        }
        if (!reset) {
            display.getVisualization().repaint();
        }
    }

    private void setCurrentFont(Font f) {
        fontFamilyButton.setText(f.getFontName() + ", " + f.getSize());
        for (Iterator<NodeItem> nodesIter = display.getVisualization().items(NODES); nodesIter.hasNext();) {
            NodeItem n = nodesIter.next();
            if (!n.getFont().equals(f)) {
                PrefuseLib.update(n, VisualItem.FONT, f);
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

    private void resetOptions() {
        reset = true;
        display.getVisualization().rerun(new Runnable() {

            @Override
            public void run() {
                shapeChooser.setSelectedItem(NodeShape.ELLIPSE);
                setNodeSize(1.0D);
                setCurrentFont(Config.FONT_DEFAULT_NODETEXT);
                Lookup.getDefault().lookup(ColorController.class).getModel().getNodeColorProvider().clearFillColors();
                Lookup.getDefault().lookup(ColorController.class).getModel().getNodeColorProvider().clearTextColors();
                Lookup.getDefault().lookup(ColorController.class).getModel().getNodeColorProvider().clearStrokeColors();
                ((JColorButton) fillColorButton).setColor(ColorLib.getColor(Config.COLOR_DEFAULT_NODE_FILL));
                ((JColorButton) fontColorButton).setColor(ColorLib.getColor(Config.COLOR_DEFAULT_NODE_TEXT));
                ((JColorButton) borderColorButton).setColor(ColorLib.getColor(Config.COLOR_DEFAULT_NODE_STROKE));
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

        shapeLabel = new javax.swing.JLabel();
        shapeChooser = new javax.swing.JComboBox();
        fontLabel = new javax.swing.JLabel();
        fontFamilyButton = new javax.swing.JButton();
        fontColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor(Config.COLOR_DEFAULT_NODE_TEXT));
        fillColorLabel = new javax.swing.JLabel();
        fillColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor(Config.COLOR_DEFAULT_NODE_FILL));
        borderColorLabel = new javax.swing.JLabel();
        borderColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor(Config.COLOR_DEFAULT_NODE_STROKE));
        sizeLabel = new javax.swing.JLabel();
        sizeSpinner = new javax.swing.JSpinner();
        resetAllButton = new javax.swing.JButton();

        shapeLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.shapeLabel.text")); // NOI18N

        shapeChooser.setFont(shapeChooser.getFont().deriveFont(shapeChooser.getFont().getSize()-1f));

        fontLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.fontLabel.text")); // NOI18N

        fontFamilyButton.setFont(fontFamilyButton.getFont().deriveFont(fontFamilyButton.getFont().getSize()-1f));
        fontFamilyButton.setText(Config.FONT_DEFAULT_NODETEXT.getFontName() + ", " + Config.FONT_DEFAULT_NODETEXT.getSize());
        fontFamilyButton.setBorderPainted(false);

        fontColorButton.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.fontColorButton.text")); // NOI18N

        fillColorLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.fillColorLabel.text")); // NOI18N

        fillColorButton.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.fillColorButton.text")); // NOI18N

        borderColorLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.borderColorLabel.text")); // NOI18N

        borderColorButton.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.borderColorButton.text")); // NOI18N

        sizeLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.sizeLabel.text")); // NOI18N

        sizeSpinner.setFont(sizeSpinner.getFont().deriveFont(sizeSpinner.getFont().getSize()-1f));
        sizeSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.5d), null, Double.valueOf(0.2d)));

        resetAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/undo.png"))); // NOI18N
        resetAllButton.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.resetAllButton.text")); // NOI18N
        resetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(shapeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(shapeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fontLabel)
                                .addGap(18, 18, 18)
                                .addComponent(fontFamilyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fontColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fillColorLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(borderColorLabel)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(sizeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(borderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(resetAllButton))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(shapeLabel)
                    .addComponent(shapeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sizeLabel)
                    .addComponent(sizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fontColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontFamilyButton)
                    .addComponent(fontLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fillColorLabel)
                    .addComponent(borderColorLabel)
                    .addComponent(borderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resetAllButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllButtonActionPerformed
        if (DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation("All node options will be reset. Continue?", NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
            resetOptions();
        }
    }//GEN-LAST:event_resetAllButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton borderColorButton;
    private javax.swing.JLabel borderColorLabel;
    private javax.swing.JButton fillColorButton;
    private javax.swing.JLabel fillColorLabel;
    private javax.swing.JButton fontColorButton;
    private javax.swing.JButton fontFamilyButton;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JButton resetAllButton;
    private javax.swing.JComboBox shapeChooser;
    private javax.swing.JLabel shapeLabel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JSpinner sizeSpinner;
    // End of variables declaration//GEN-END:variables
}
