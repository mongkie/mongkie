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
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static kobic.prefuse.Constants.NODES;
import kobic.prefuse.NodeShape;
import org.mongkie.lib.widgets.JColorButton;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.VisualStyle;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class NodeSettingPanel extends javax.swing.JPanel implements VisualStyle.UI<NodeItem> {

    private final MongkieDisplay display;
    private boolean eventEnabled = true;
    private final PropertyEditor fontChooser = PropertyEditorManager.findEditor(Font.class);
    private final VisualStyle<NodeItem> style;
    private Iterable<NodeItem> nodes;
    private boolean applied = false;

    NodeSettingPanel(final MongkieDisplay display) {
        this(display, new Iterable<NodeItem>() {
            @Override
            public Iterator<NodeItem> iterator() {
                return display.getVisualization().items(NODES);
            }
        });
    }

    /**
     * Creates new form NodeSettingPanel
     */
    NodeSettingPanel(MongkieDisplay display, Iterable<NodeItem> nodes) {
        this.display = display;
        this.style = new VisualStyle.Node() {
            @Override
            public boolean assign(String field, NodeItem n) {
                boolean redraw = super.assign(field, n);
                applied = true;
                return redraw;
            }
        };
        this.nodes = nodes;

        initComponents();

        for (NodeShape s : NodeShape.values()) {
            shapeChooser.addItem(s);
        }
        shapeChooser.setSelectedItem(NodeShape.get((Integer) style.get(VisualItem.SHAPE)));
        shapeChooser.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (eventEnabled) {
                    getVisualStyle().apply(VisualItem.SHAPE,
                            ((NodeShape) shapeChooser.getSelectedItem()).getCode(),
                            getVisualItems());
                }
            }
        });

        sizeSpinner.setValue(style.get(VisualItem.SIZE));
        sizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (eventEnabled) {
                    getVisualStyle().apply(VisualItem.SIZE,
                            ((Double) sizeSpinner.getValue()).doubleValue(),
                            getVisualItems());
                }
            }
        });

        fontFamilyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fontChooser.setValue(getVisualStyle().get(VisualItem.FONT));
                DialogDescriptor dd = new DialogDescriptor(fontChooser.getCustomEditor(), "Node Font");
                DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
                if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                    if (eventEnabled) {
                        Font f = (Font) fontChooser.getValue();
                        getVisualStyle().apply(VisualItem.FONT, f, getVisualItems());
                        updateFontButton(f);
                    }
                }
            }
        });
        ((JColorButton) fillColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (eventEnabled) {
                            getVisualStyle().apply(VisualItem.FILLCOLOR,
                                    ColorLib.color(((JColorButton) fillColorButton).getColor()),
                                    getVisualItems());
                        }
                    }
                });
        ((JColorButton) fontColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (eventEnabled) {
                            getVisualStyle().apply(VisualItem.TEXTCOLOR,
                                    ColorLib.color(((JColorButton) fontColorButton).getColor()),
                                    getVisualItems());
                        }
                    }
                });
        ((JColorButton) borderColorButton).addPropertyChangeListener(JColorButton.EVENT_COLOR,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (eventEnabled) {
                            getVisualStyle().apply(VisualItem.STROKECOLOR,
                                    ColorLib.color(((JColorButton) borderColorButton).getColor()),
                                    getVisualItems());
                        }
                    }
                });
    }

    private void updateFontButton(Font f) {
        fontFamilyButton.setFont(FontLib.getFont(f.getName(), f.getStyle(), 12));
        fontFamilyButton.setText(f.getFontName() + ", " + f.getSize());
    }

    private void reset() {
        style.reset();
        // Reload the style
        loadVisualStyle(style, true);
    }

    @Override
    public VisualStyle<NodeItem> loadVisualStyle(VisualStyle<NodeItem> style, boolean apply) {
        this.style.reset(style);
        // Update UI
        eventEnabled = false;
        shapeChooser.setSelectedItem(NodeShape.get((Integer) this.style.get(VisualItem.SHAPE)));
        sizeSpinner.setValue(this.style.get(VisualItem.SIZE));
        updateFontButton((Font) this.style.get(VisualItem.FONT));
        ((JColorButton) fillColorButton).setColor(ColorLib.getColor((Integer) this.style.get(VisualItem.FILLCOLOR)));
        ((JColorButton) fontColorButton).setColor(ColorLib.getColor((Integer) this.style.get(VisualItem.TEXTCOLOR)));
        ((JColorButton) borderColorButton).setColor(ColorLib.getColor((Integer) this.style.get(VisualItem.STROKECOLOR)));
        eventEnabled = true;
        if (apply) {
            // Apply the new style to visual items
            this.style.apply(getVisualItems());
        }
        applied = apply;
        return this.style;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public VisualStyle<NodeItem> getVisualStyle() {
        return style;
    }

    @Override
    public Iterator<NodeItem> getVisualItems() {
        return nodes.iterator();
    }

    @Override
    public boolean isApplied() {
        return applied;
    }

    @Override
    public String getItemType() {
        return VisualStyle.NODES;
    }

    /**
     * This method is called from within the constructor to
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
        fontColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor((Integer) style.get(VisualItem.TEXTCOLOR)));
        fillColorLabel = new javax.swing.JLabel();
        fillColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor((Integer) style.get(VisualItem.FILLCOLOR)));
        borderColorLabel = new javax.swing.JLabel();
        borderColorButton = new JColorButton(WindowManager.getDefault().getMainWindow(), ColorLib.getColor((Integer) style.get(VisualItem.STROKECOLOR)));
        sizeLabel = new javax.swing.JLabel();
        sizeSpinner = new javax.swing.JSpinner();
        resetAllButton = new javax.swing.JButton();
        presetsButton = new javax.swing.JButton();

        shapeLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.shapeLabel.text")); // NOI18N

        fontLabel.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.fontLabel.text")); // NOI18N

        fontFamilyButton.setFont(FontLib.getFont(((Font) style.get(VisualItem.FONT)).getName(), ((Font) style.get(VisualItem.FONT)).getStyle(), 12));
        fontFamilyButton.setText(((Font) style.get(VisualItem.FONT)).getFontName() + ", " + ((Font) style.get(VisualItem.FONT)).getSize());
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
        resetAllButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        resetAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetAllButtonActionPerformed(evt);
            }
        });

        presetsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/preset.png"))); // NOI18N
        presetsButton.setText(org.openide.util.NbBundle.getMessage(NodeSettingPanel.class, "NodeSettingPanel.presetsButton.text")); // NOI18N
        presetsButton.setFocusPainted(false);
        presetsButton.setFocusable(false);
        presetsButton.setIconTextGap(0);
        presetsButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        presetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(shapeLabel)
                            .addComponent(fontLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(shapeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(sizeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fontFamilyButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(fontColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(resetAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(presetsButton))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(fillColorLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(borderColorLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(borderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(presetsButton)
                    .addComponent(resetAllButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetAllButtonActionPerformed
        if (DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation("All node options will be reset. Continue?", NotifyDescriptor.OK_CANCEL_OPTION)) == NotifyDescriptor.OK_OPTION) {
            reset();
        }
    }//GEN-LAST:event_resetAllButtonActionPerformed

    private void presetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetsButtonActionPerformed
        JPopupMenu menu = new JPopupMenu();
        Set<VisualStyle> presets = VisualStyle.getPersistence().getValues(this);
        if (presets != null && !presets.isEmpty()) {
            for (final VisualStyle preset : presets) {
                JMenuItem item = new JMenuItem(preset.getName());
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        loadVisualStyle(preset, true);
                        StatusDisplayer.getDefault().setStatusText("Visual style \"" + preset.getName() + "\" is loaded for " + getItemType());
                    }
                });
                menu.add(item);
            }
        } else {
            menu.add("<html><i>No preset</i></html>");
        }
        menu.add(new JSeparator());
        JMenuItem saveItem = new JMenuItem("Save preset...");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String lastPresetName = NbPreferences.forModule(NodeSettingPanel.class).get("NodeSettingPanel.lastPresetName", "");
                NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine("Name", "Preset name");
                question.setInputText(lastPresetName);
                if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                    String name = question.getInputText();
                    if (name != null && !name.isEmpty()
                            && VisualStyle.getPersistence().save(NodeSettingPanel.this, name)) {
                        StatusDisplayer.getDefault().setStatusText("Visual style \"" + name + "\" is saved for " + getItemType());
                        NbPreferences.forModule(NodeSettingPanel.class).put("NodeSettingPanel.lastPresetName", name);
                    }
                }
            }
        });
        menu.add(saveItem);
        menu.show(presetsButton, 0, presetsButton.getPreferredSize().height);
    }//GEN-LAST:event_presetsButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton borderColorButton;
    private javax.swing.JLabel borderColorLabel;
    private javax.swing.JButton fillColorButton;
    private javax.swing.JLabel fillColorLabel;
    private javax.swing.JButton fontColorButton;
    private javax.swing.JButton fontFamilyButton;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JButton presetsButton;
    private javax.swing.JButton resetAllButton;
    private javax.swing.JComboBox shapeChooser;
    private javax.swing.JLabel shapeLabel;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JSpinner sizeSpinner;
    // End of variables declaration//GEN-END:variables
}
