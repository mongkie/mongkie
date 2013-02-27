/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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

/*
 * PartitionTopPane.java
 *
 * Created on Dec 9, 2011, 2:05:58 PM
 */
package org.mongkie.ui.visualmap.partition;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.mongkie.visualization.workspace.ModelChangeListener;
import org.mongkie.visualmap.partition.PartitionController;
import org.mongkie.visualmap.partition.PartitionEvent;
import org.mongkie.visualmap.partition.PartitionModel;
import org.mongkie.visualmap.partition.PartitionModelListener;
import org.mongkie.visualmap.spi.VisualMapTopPaneUI;
import org.mongkie.visualmap.spi.partition.Partition;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualMapTopPaneUI.class, position = 200)
public class PartitionTopPane extends javax.swing.JPanel
        implements VisualMapTopPaneUI, PartitionModelListener, ItemListener, ExplorerManager.Provider, Lookup.Provider {

    private transient PartitionController controller;
    private transient PartitionModel model;
    private ExplorerManager em;
    private Lookup lookup;
    private volatile boolean internalPartionSelection = false;

    /**
     * Creates new form PartitionTopPane
     */
    public PartitionTopPane() {
        controller = Lookup.getDefault().lookup(PartitionController.class);
        controller.addModelChangeListener(new ModelChangeListener<PartitionModel>() {
            @Override
            public void modelChanged(PartitionModel o, PartitionModel n) {
                if (o != null) {
                    o.removeModelListener(PartitionTopPane.this);
                }
                model = n;
                if (model != null) {
                    model.addModelListener(PartitionTopPane.this);
                }
                refreshModel();
            }
        });

        initComponents();

        em = new ExplorerManager();
        lookup = ExplorerUtils.createLookup(em, getActionMap());

        partitionChooser.addItemListener(PartitionTopPane.this);
    }

    private void refreshModel() {
        boolean enabled = model != null;
        partitionChooser.setEnabled(enabled);
        partitionToolbar.setEnabled(enabled);
        ((PartitionToolbar) partitionToolbar).refreshModel(model);
        refreshChooser();
    }

    private void refreshChooser() {
        internalPartionSelection = true;
        partitionChooser.removeAllItems();
        partitionChooser.addItem(NO_SELECTION);
        partitionChooser.setSelectedItem(NO_SELECTION);
        refreshPartList(null);
        if (model != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Partition selectedPartition = model.getCurrentPartition();
                    for (Partition r : model.getPartitions()) {
                        partitionChooser.addItem(r);
                        if (selectedPartition != null
                                && selectedPartition.getElementType().equals(r.getElementType()) && selectedPartition.getName().equals(r.getName())) {
                            partitionChooser.setSelectedItem(r);
                        }
                    }
                    refreshPartList(model.getCurrentPartition()); // May have been refresh by the model
                    internalPartionSelection = false;
                }
            });
        }
    }
    private final String NO_SELECTION = "---Choose a mapping parameter";

    private void refreshPartList(Partition partition) {
        partitionResultView.setViewportView(((PartitionResultView) partitionResultView).setPartition(em, partition));
    }

    @Override
    public void processPartitionEvent(PartitionEvent e) {
        switch (e.getType()) {
            case CURRENT_PARTITION:
                Partition selectedPartition = (Partition) e.getNewValue();
                if (selectedPartition != null && partitionChooser.getSelectedItem() != selectedPartition) {
                    refreshChooser();
                } else {
                    refreshPartList(selectedPartition);
                }
                break;
            case CURRENT_ELEMENT_TYPE:
            case REFRESH_PARTITION:
                refreshChooser();
                break;
            default:
                break;
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (!internalPartionSelection) {
            Object selectedItem = e.getItem();
            if (e.getStateChange() == ItemEvent.SELECTED && model != null
                    && !selectedItem.equals(model.getCurrentPartition())) {
                if (!selectedItem.equals(NO_SELECTION)) {
                    controller.setCurrentPartition((Partition) selectedItem);
                } else {
                    controller.setCurrentPartition(null);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        partitionToolbar = new PartitionToolbar();
        partitionResultView = new org.mongkie.ui.visualmap.partition.PartitionResultView();
        partitionChooserPanel = new javax.swing.JPanel();
        partitionChooser = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        partitionToolbar.setFloatable(false);
        partitionToolbar.setRollover(true);
        partitionToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(partitionToolbar, gridBagConstraints);

        partitionResultView.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(partitionResultView, gridBagConstraints);

        partitionChooserPanel.setOpaque(false);
        partitionChooserPanel.setLayout(new java.awt.GridBagLayout());

        partitionChooser.setPreferredSize(new java.awt.Dimension(40, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        partitionChooserPanel.add(partitionChooser, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.04;
        add(partitionChooserPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox partitionChooser;
    private javax.swing.JPanel partitionChooserPanel;
    private javax.swing.JScrollPane partitionResultView;
    private javax.swing.JToolBar partitionToolbar;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getDisplayName() {
        return "Discrete";
    }

    @Override
    public String getShortDescription() {
        return "Discrete visual mapping";
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(em, true);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ExplorerUtils.activateActions(em, false);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
}
