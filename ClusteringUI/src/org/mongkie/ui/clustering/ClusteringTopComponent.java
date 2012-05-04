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
package org.mongkie.ui.clustering;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.mongkie.clustering.ClusteringController;
import org.mongkie.clustering.ClusteringModel;
import org.mongkie.clustering.ClusteringModelListener;
import org.mongkie.clustering.spi.Cluster;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import org.mongkie.lib.widgets.BusyLabel;
import org.mongkie.ui.clustering.explorer.ClusteringResultView;
import static org.mongkie.visualization.Config.MODE_ACTION;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.mongkie.visualization.workspace.ModelChangeListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.clustering//Clustering//EN",
autostore = false)
@TopComponent.Description(preferredID = ClusteringTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/clustering/resources/clustering.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_ACTION, openAtStartup = true, roles = ROLE_NETWORK, position = 300)
@ActionID(category = "Window", id = "org.mongkie.ui.clustering.ClusteringTopComponent")
@ActionReference(path = "Menu/Window", position = 30)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ClusteringAction",
preferredID = ClusteringTopComponent.PREFERRED_ID)
public final class ClusteringTopComponent extends TopComponent implements ClusteringModelListener, ExplorerManager.Provider {

    /** path to the icon used by the component and its open action */
    static final String PREFERRED_ID = "ClusteringTopComponent";
    private static final String NO_SELECTION =
            NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.choose.displayText");
    private ClusteringModel model;
    private BusyLabel clusterizing;
//    private ClusteringResultPanel resultPanel;
    private ExplorerManager em = new ExplorerManager();

    public ClusteringTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ClusteringTopComponent.class, "CTL_ClusteringTopComponent"));
        setToolTipText(NbBundle.getMessage(ClusteringTopComponent.class, "HINT_ClusteringTopComponent"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        clusterizing = new BusyLabel(
                NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.clusterizing.text"),
                resultView, noResultLabel);
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));

        initializeChooser();
        addEventListeners();
    }

    private void initializeChooser() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        for (ClusteringBuilder builder : Lookup.getDefault().lookupAll(ClusteringBuilder.class)) {
            comboBoxModel.addElement(builder);
        }
        clusteringComboBox.setModel(comboBoxModel);
    }

    private void addEventListeners() {
        Lookup.getDefault().lookup(ClusteringController.class).addModelChangeListener(new ModelChangeListener<ClusteringModel>() {

            @Override
            public void modelChanged(ClusteringModel o, ClusteringModel n) {
                if (o != null) {
                    o.removeModelListener(ClusteringTopComponent.this);
                }
                model = n;
                if (model != null) {
                    model.addModelListener(ClusteringTopComponent.this);
                }
                refreshModel();
            }
        });
        clusteringComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clusteringComboBox.getSelectedItem().equals(NO_SELECTION) && model != null) {
                    setSelectedClustering(null);
                } else if (clusteringComboBox.getSelectedItem() instanceof ClusteringBuilder) {
                    setSelectedClustering((ClusteringBuilder) clusteringComboBox.getSelectedItem());
                }
            }
        });
    }

    private void setSelectedClustering(ClusteringBuilder builder) {
        if (model.get() != null && model.get().getBuilder() == builder) {
            return;
        }
        Lookup.getDefault().lookup(ClusteringController.class).setClustering(builder != null ? builder.getClustering() : null);
    }

    private void refreshModel() {
        refreshChooser();
        refreshEnabled();
        refreshResult();
    }

    private void refreshChooser() {
        Clustering cl = model != null ? model.get() : null;
        clusteringComboBox.getModel().setSelectedItem(cl != null ? cl.getBuilder() : NO_SELECTION);
    }

    private void refreshEnabled() {
        if (model == null || !model.isRunning()) {
            runButton.setText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/clustering/resources/run.gif", false));
            runButton.setToolTipText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.toolTipText"));
        } else if (model.isRunning()) {
            runButton.setText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.cancelButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/clustering/resources/stop.png", false));
            runButton.setToolTipText(NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.cancelButton.toolTipText"));
        }

        boolean enabled = model != null && model.get() != null && model.getDisplay().getGraph().getNodeCount() > 0;
        runButton.setEnabled(enabled);
        infoLabel.setEnabled(enabled);
        noResultLabel.setEnabled(enabled);
        settingButtion.setEnabled(enabled && !model.isRunning() && model.get().getBuilder().getSettingUI() != null);
        groupAllLink.setEnabled(enabled && !model.isRunning() && !model.get().getClusters().isEmpty());
        ungroupAllLink.setEnabled(enabled && !model.isRunning() && !model.get().getClusters().isEmpty());
        pieLink.setEnabled(enabled && !model.isRunning() && !model.get().getClusters().isEmpty());

        clusteringComboBox.setEnabled(model != null && !model.isRunning() && model.getDisplay().getGraph().getNodeCount() > 0);
    }

    private void refreshResult() {
        Clustering cl;
        if (model == null || (cl = model.get()) == null || model.getDisplay().getGraph().getNodeCount() < 1) {
            clusterizing.setBusy(false);
        } else if (model.isRunning()) {
            clusterizing.setBusy(true);
        } else {
            clusterizing.setBusy(false, cl.getClusters().isEmpty() ? noResultLabel : ((ClusteringResultView) resultView).setClustering(cl));
        }
    }

    @Override
    public void clusteringChanged(Clustering o, Clustering n) {
        refreshModel();
    }

    @Override
    public void clusteringStarted(Clustering cl) {
        refreshEnabled();
        clusterizing.setBusy(true);
    }

    @Override
    public void clusteringFinished(final Clustering cl) {
        refreshEnabled();
        clusterizing.setBusy(false, cl.getClusters().isEmpty() ? noResultLabel : ((ClusteringResultView) resultView).setClustering(cl));
    }

    private void run() {
        Lookup.getDefault().lookup(ClusteringController.class).clusterize();
    }

    private void cancel() {
        Lookup.getDefault().lookup(ClusteringController.class).cancelClustering();
    }

    private void openSettingDialog() {
        ClusteringBuilder builder = model.get().getBuilder();
        ClusteringBuilder.SettingUI settings = builder.getSettingUI();
        DialogDescriptor dialog = new DialogDescriptor(settings.getPanel(),
                NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.settings.title", builder.getName()));
        settings.setup(builder.getClustering());
        if (DialogDisplayer.getDefault().notify(dialog).equals(NotifyDescriptor.OK_OPTION)) {
            settings.apply(builder.getClustering());
            run();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        chooserPanel = new javax.swing.JPanel();
        clusteringComboBox = new javax.swing.JComboBox();
        settingButtion = new javax.swing.JButton();
        infoLabel = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        resultView = new org.mongkie.ui.clustering.explorer.ClusteringResultView(em);
        noResultLabel = new javax.swing.JLabel();
        controlPanel = new javax.swing.JPanel();
        groupAllLink = new org.jdesktop.swingx.JXHyperlink();
        ungroupAllLink = new org.jdesktop.swingx.JXHyperlink();
        pieLink = new org.jdesktop.swingx.JXHyperlink();

        setLayout(new java.awt.BorderLayout());

        chooserPanel.setOpaque(false);
        chooserPanel.setLayout(new java.awt.GridBagLayout());

        clusteringComboBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 6;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 2, 2, 0);
        chooserPanel.add(clusteringComboBox, gridBagConstraints);

        settingButtion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/setting.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(settingButtion, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.settingButtion.text")); // NOI18N
        settingButtion.setToolTipText(org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.settingButtion.toolTipText")); // NOI18N
        settingButtion.setEnabled(false);
        settingButtion.setFocusPainted(false);
        settingButtion.setMargin(new java.awt.Insets(3, 3, 3, 3));
        settingButtion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingButtionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 2, 2);
        chooserPanel.add(settingButtion, gridBagConstraints);

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/information.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.infoLabel.text")); // NOI18N
        infoLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 4);
        chooserPanel.add(infoLabel, gridBagConstraints);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/run.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runButton, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.text")); // NOI18N
        runButton.setToolTipText(org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.runButton.toolTipText")); // NOI18N
        runButton.setEnabled(false);
        runButton.setFocusPainted(false);
        runButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        chooserPanel.add(runButton, gridBagConstraints);

        add(chooserPanel, java.awt.BorderLayout.PAGE_START);

        resultView.setBorder(null);
        resultView.setOpaque(false);

        noResultLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Label.disabledShadow"));
        noResultLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noResultLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/pin-small.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(noResultLabel, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.noResultLabel.text")); // NOI18N
        resultView.setViewportView(noResultLabel);

        add(resultView, java.awt.BorderLayout.CENTER);

        controlPanel.setOpaque(false);
        controlPanel.setLayout(new java.awt.GridBagLayout());

        groupAllLink.setClickedColor(new java.awt.Color(0, 51, 255));
        groupAllLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/groupAll.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(groupAllLink, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.groupAllLink.text")); // NOI18N
        groupAllLink.setToolTipText(org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.groupAllLink.toolTipText")); // NOI18N
        groupAllLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        groupAllLink.setFocusPainted(false);
        groupAllLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupAllLinkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 10);
        controlPanel.add(groupAllLink, gridBagConstraints);

        ungroupAllLink.setClickedColor(new java.awt.Color(0, 51, 255));
        ungroupAllLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/ungroupAll.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(ungroupAllLink, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.ungroupAllLink.text")); // NOI18N
        ungroupAllLink.setToolTipText(org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.ungroupAllLink.toolTipText")); // NOI18N
        ungroupAllLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        ungroupAllLink.setFocusPainted(false);
        ungroupAllLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ungroupAllLinkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 5, 0);
        controlPanel.add(ungroupAllLink, gridBagConstraints);

        pieLink.setClickedColor(new java.awt.Color(0, 51, 255));
        pieLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/clustering/resources/pie.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(pieLink, org.openide.util.NbBundle.getMessage(ClusteringTopComponent.class, "ClusteringTopComponent.pieLink.text")); // NOI18N
        pieLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        pieLink.setFocusPainted(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 5, 2);
        controlPanel.add(pieLink, gridBagConstraints);

        add(controlPanel, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        if (model.isRunning()) {
            cancel();
        } else {
            run();
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void settingButtionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingButtionActionPerformed
        openSettingDialog();
    }//GEN-LAST:event_settingButtionActionPerformed

    private void groupAllLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupAllLinkActionPerformed
        for (Cluster c : model.get().getClusters()) {
            Lookup.getDefault().lookup(ClusteringController.class).group(c);
        }
    }//GEN-LAST:event_groupAllLinkActionPerformed

    private void ungroupAllLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ungroupAllLinkActionPerformed
        for (Cluster c : model.get().getClusters()) {
            Lookup.getDefault().lookup(ClusteringController.class).ungroup(c);
        }
    }//GEN-LAST:event_ungroupAllLinkActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JComboBox clusteringComboBox;
    private javax.swing.JPanel controlPanel;
    private org.jdesktop.swingx.JXHyperlink groupAllLink;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JLabel noResultLabel;
    private org.jdesktop.swingx.JXHyperlink pieLink;
    private javax.swing.JScrollPane resultView;
    private javax.swing.JButton runButton;
    private javax.swing.JButton settingButtion;
    private org.jdesktop.swingx.JXHyperlink ungroupAllLink;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(em, true);
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(em, false);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
