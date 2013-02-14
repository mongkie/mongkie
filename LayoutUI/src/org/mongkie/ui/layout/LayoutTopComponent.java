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
package org.mongkie.ui.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.mongkie.layout.LayoutController;
import org.mongkie.layout.LayoutModel;
import org.mongkie.layout.LayoutModelChangeListener;
import org.mongkie.layout.spi.Layout;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutBuilder.UI;
import org.mongkie.lib.widgets.richtooltip.RichTooltip;
import org.mongkie.ui.layout.LayoutPresetStoreImpl.Preset;
import static org.mongkie.visualization.Config.MODE_CONTROL;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which lists layouts and displays their settings
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.layout//Layout//EN",
autostore = false)
@TopComponent.Description(preferredID = LayoutTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/layout/resources/layout.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_CONTROL, openAtStartup = true, roles = ROLE_NETWORK, position = 100)
public final class LayoutTopComponent extends TopComponent implements PropertyChangeListener {

    private static LayoutTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
    static final String PREFERRED_ID = "LayoutTopComponent";
    private final String NO_SELECTION;
    private LayoutModel model;

    public LayoutTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(LayoutTopComponent.class, "CTL_LayoutTopComponent"));
        setToolTipText(NbBundle.getMessage(LayoutTopComponent.class, "HINT_LayoutTopComponent"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        NO_SELECTION = NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.choose.displayText");
        initializeChooser();

        addEventListeners();
    }

    private void initializeChooser() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
        comboBoxModel.addElement(NO_SELECTION);
        comboBoxModel.setSelectedItem(NO_SELECTION);
        for (LayoutBuilder builder : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
            comboBoxModel.addElement(builder);
        }
        layoutCombobox.setModel(comboBoxModel);
    }

    private void addEventListeners() {
        Lookup.getDefault().lookup(LayoutController.class).addModelChangeListener(new LayoutModelChangeListener() {
            @Override
            public void modelChanged(LayoutModel oldModel, LayoutModel newModel) {
                if (oldModel != null) {
                    oldModel.removePropertyChangeListener(LayoutTopComponent.this);
                }
                model = newModel;
                if (model != null) {
                    model.addPropertyChangeListener(LayoutTopComponent.this);
                }
                refreshModel();
            }
        });
        layoutCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (layoutCombobox.getSelectedItem().equals(NO_SELECTION) && model != null) {
                    setSelectedLayout(null);
                } else if (layoutCombobox.getSelectedItem() instanceof LayoutBuilder) {
                    setSelectedLayout((LayoutBuilder) layoutCombobox.getSelectedItem());
                }
            }
        });
        infoLabel.addMouseListener(new MouseAdapter() {
            RichTooltip richTooltip;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (infoLabel.isEnabled() && !layoutCombobox.getSelectedItem().equals(NO_SELECTION)) {
                    richTooltip = buildTooltip((LayoutBuilder) layoutCombobox.getSelectedItem());
                    richTooltip.showTooltip(infoLabel);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (richTooltip != null) {
                    richTooltip.hideTooltip();
                    richTooltip = null;
                }
            }
        });
    }

    private void setSelectedLayout(LayoutBuilder builder) {
        if (model.getSelectedLayout() != null && model.getSelectedLayout().getBuilder() == builder) {
            return;
        }
        Lookup.getDefault().lookup(LayoutController.class).setLayout(builder);
    }

    private void refreshModel() {
        refreshChooser();
        refreshProperties();
        refreshEnabled();
        selectionOnlyButton.setSelected(model != null && model.isSelectionOnly());
    }

    private void refreshChooser() {
        Layout l = model != null ? model.getSelectedLayout() : null;
        layoutCombobox.getModel().setSelectedItem(l == null ? NO_SELECTION : l.getBuilder());
    }

    private void refreshProperties() {
        Layout l = model != null ? model.getSelectedLayout() : null;
        if (layoutNodes != null) {
            for (Node n : layoutNodes) {
                try {
                    n.destroy();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        layoutNodes = (l == null) ? new Node[0] : new Node[]{new LayoutNode(l)};
        ((PropertySheet) propertySheet).setNodes(layoutNodes);
    }
    private Node[] layoutNodes;

    private void refreshEnabled() {
        if (model == null || !model.isRunning()) {
            runButton.setText(NbBundle.getMessage(getClass(), "LayoutTopComponent.runButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/layout/resources/run.gif", false));
            runButton.setToolTipText(NbBundle.getMessage(getClass(), "LayoutTopComponent.runButton.tooltip"));
        } else if (model.isRunning()) {
            runButton.setText(NbBundle.getMessage(getClass(), "LayoutTopComponent.stopButton.text"));
            runButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/layout/resources/stop.png", false));
            runButton.setToolTipText(NbBundle.getMessage(getClass(), "LayoutTopComponent.stopButton.tooltip"));
        }

        Layout l = (model != null) ? model.getSelectedLayout() : null;
        boolean enabled = l != null && model.getDisplay().isFired();
        runButton.setEnabled(enabled);
        infoLabel.setEnabled(enabled);
        propertySheet.setEnabled(enabled);
        presetsButton.setEnabled(enabled && l.getProperties().length > 0);
        resetButton.setEnabled(presetsButton.isEnabled());
        selectionOnlyButton.setEnabled(enabled && !model.isRunning());

        layoutCombobox.setEnabled(model != null && !model.isRunning() && model.getDisplay().isFired());
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(LayoutModel.SELECTED_LAYOUT)) {
            refreshModel();
        } else if (e.getPropertyName().equals(LayoutModel.IS_RUNNING)) {
            refreshEnabled();
        }
    }

    private RichTooltip buildTooltip(LayoutBuilder builder) {
        String description = "";
        UI layoutUI;
        try {
            layoutUI = builder.getUI();
            description = layoutUI.getDescription();
            if (layoutUI.getQualityRank() < 0 || layoutUI.getSpeedRank() < 0) {
                layoutUI = null;
            }
        } catch (Exception e) {
            layoutUI = null;
        }

        RichTooltip richTooltip = new RichTooltip(builder.getName(), description);
        if (layoutUI != null) {
            LayoutDescriptionImage layoutDescriptionImage = new LayoutDescriptionImage(layoutUI);
            richTooltip.setMainImage(layoutDescriptionImage.getImage());
        }
        return richTooltip;
    }

    private void run() {
        if (model.isSelectionOnly() && !model.getSelectedLayout().supportsSelectionOnly()) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(LayoutTopComponent.class,
                    "LayoutTopComponent.selectionOnlyButton.toolTipText.notSupported", model.getSelectedLayout().getBuilder().getName()));
            return;
        }
        Lookup.getDefault().lookup(LayoutController.class).executeLayout();
    }

    private void stop() {
        Lookup.getDefault().lookup(LayoutController.class).stopLayout();
    }

    private void reset() {
        Layout l = model.getSelectedLayout();
        if (l != null) {
            l.resetPropertyValues();
        }
    }

    private static class LayoutDescriptionImage {

        private static final int STAR_WIDTH = 16;
        private static final int STAR_HEIGHT = 16;
        private static final int STAR_MAX = 5;
        private static final int TEXT_GAP = 5;
        private static final int LINE_GAP = 4;
        private static final int Y_BEGIN = 10;
        private static final int IMAGE_RIGHT_MARIN = 10;
        private Image greenIcon;
        private Image grayIcon;
        private Graphics g;
        private String qualityStr;
        private String speedStr;
        private int textMaxSize;
        private UI layoutUI;

        public LayoutDescriptionImage(UI layoutUI) {
            this.layoutUI = layoutUI;
            greenIcon = ImageUtilities.loadImage("org/mongkie/ui/layout/resources/yellow.png");
            grayIcon = ImageUtilities.loadImage("org/mongkie/ui/layout/resources/grey.png");
            qualityStr = NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.tooltip.quality.displayName");
            speedStr = NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.tooltip.speed.displayName");
        }

        public void paint(Graphics g) {
            g.setColor(Color.BLACK);
            g.drawString(qualityStr, 0, STAR_HEIGHT + Y_BEGIN - 2);
            paintStarPanel(g, textMaxSize + TEXT_GAP, Y_BEGIN, STAR_MAX, layoutUI.getQualityRank());
            g.drawString(speedStr, 0, STAR_HEIGHT * 2 + LINE_GAP + Y_BEGIN - 2);
            paintStarPanel(g, textMaxSize + TEXT_GAP, STAR_HEIGHT + LINE_GAP + Y_BEGIN, STAR_MAX, layoutUI.getSpeedRank());
        }

        public Image getImage() {
            //Image size
            BufferedImage im = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            textMaxSize = 0;
            textMaxSize = Math.max(im.getGraphics().getFontMetrics().stringWidth(qualityStr), textMaxSize);
            textMaxSize = Math.max(im.getGraphics().getFontMetrics().stringWidth(speedStr), textMaxSize);
            int imageWidth = STAR_MAX * STAR_WIDTH + TEXT_GAP + textMaxSize + IMAGE_RIGHT_MARIN;

            //Paint
            BufferedImage img = new BufferedImage(imageWidth, 100, BufferedImage.TYPE_INT_ARGB);
            this.g = img.getGraphics();
            paint(g);
            return img;
        }

        public void paintStarPanel(Graphics g, int x, int y, int max, int value) {
            for (int i = 0; i < max; i++) {
                if (i < value) {
                    g.drawImage(greenIcon, x + i * 16, y, null);
                } else {
                    g.drawImage(grayIcon, x + i * 16, y, null);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        layoutPanel = new javax.swing.JPanel();
        layoutCombobox = new javax.swing.JComboBox();
        infoLabel = new javax.swing.JLabel();
        runButton = new javax.swing.JButton();
        layoutToolbar = new javax.swing.JToolBar();
        presetsButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        selectionOnlyButton = new javax.swing.JToggleButton();
        propertySheet = new PropertySheet();

        setLayout(new java.awt.BorderLayout());

        layoutPanel.setLayout(new java.awt.GridBagLayout());

        layoutCombobox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        layoutPanel.add(layoutCombobox, gridBagConstraints);

        infoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        infoLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/layout/resources/info.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.infoLabel.text")); // NOI18N
        infoLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 7, 0, 0);
        layoutPanel.add(infoLabel, gridBagConstraints);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/layout/resources/run.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(runButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.runButton.text")); // NOI18N
        runButton.setEnabled(false);
        runButton.setFocusPainted(false);
        runButton.setIconTextGap(5);
        runButton.setMargin(new java.awt.Insets(2, 7, 2, 14));
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        layoutPanel.add(runButton, gridBagConstraints);

        layoutToolbar.setFloatable(false);
        layoutToolbar.setRollover(true);
        layoutToolbar.setOpaque(false);

        presetsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/layout/resources/preset.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(presetsButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.presetsButton.text")); // NOI18N
        presetsButton.setEnabled(false);
        presetsButton.setFocusPainted(false);
        presetsButton.setFocusable(false);
        presetsButton.setIconTextGap(0);
        presetsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                presetsButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(presetsButton);

        resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/layout/resources/reset.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.resetButton.text")); // NOI18N
        resetButton.setEnabled(false);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(resetButton);
        layoutToolbar.add(filler1);

        selectionOnlyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/layout/resources/selectionOnly.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(selectionOnlyButton, org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.selectionOnlyButton.text")); // NOI18N
        selectionOnlyButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.selectionOnlyButton.toolTipText")); // NOI18N
        selectionOnlyButton.setEnabled(false);
        selectionOnlyButton.setFocusPainted(false);
        selectionOnlyButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        selectionOnlyButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        selectionOnlyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectionOnlyButtonActionPerformed(evt);
            }
        });
        layoutToolbar.add(selectionOnlyButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        layoutPanel.add(layoutToolbar, gridBagConstraints);

        propertySheet.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        layoutPanel.add(propertySheet, gridBagConstraints);

        add(layoutPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        if (model.isRunning()) {
            stop();
        } else {
            run();
        }
    }//GEN-LAST:event_runButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        reset();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void presetsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_presetsButtonActionPerformed
        JPopupMenu menu = new JPopupMenu();
        Set<Preset> presets = LayoutPresetStoreImpl.getInstance().getValues(model.getSelectedLayout());
        if (presets != null && !presets.isEmpty()) {
            for (final Preset p : presets) {
                JMenuItem item = new JMenuItem(p.getName());
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutPresetStoreImpl.getInstance().load(model.getSelectedLayout(), p);
                        refreshProperties();
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.status.presetLoaded", model.getSelectedLayout().getBuilder().getName(), p.getName()));
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
                String lastPresetName = NbPreferences.forModule(LayoutTopComponent.class).get("LayoutTopComponent.lastPresetName", "");
                NotifyDescriptor.InputLine question = new NotifyDescriptor.InputLine("Name", "Preset name");
                question.setInputText(lastPresetName);
                if (DialogDisplayer.getDefault().notify(question) == NotifyDescriptor.OK_OPTION) {
                    String name = question.getInputText();
                    if (name != null && !name.isEmpty()
                            && LayoutPresetStoreImpl.getInstance().save(model.getSelectedLayout(), name)) {
                        StatusDisplayer.getDefault().setStatusText(
                                NbBundle.getMessage(LayoutTopComponent.class, "LayoutTopComponent.status.presetSaved", model.getSelectedLayout().getBuilder().getName(), name));
                        NbPreferences.forModule(LayoutTopComponent.class).put("LayoutTopComponent.lastPresetName", name);
                    }
                }
            }
        });
        menu.add(saveItem);
        menu.show(layoutToolbar, 0, -menu.getPreferredSize().height);
    }//GEN-LAST:event_presetsButtonActionPerformed

    private void selectionOnlyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectionOnlyButtonActionPerformed
        Lookup.getDefault().lookup(LayoutController.class).setSelectionOnly(selectionOnlyButton.isSelected());
    }//GEN-LAST:event_selectionOnlyButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JComboBox layoutCombobox;
    private javax.swing.JPanel layoutPanel;
    private javax.swing.JToolBar layoutToolbar;
    private javax.swing.JButton presetsButton;
    private javax.swing.JPanel propertySheet;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton runButton;
    private javax.swing.JToggleButton selectionOnlyButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized LayoutTopComponent getDefault() {
        if (instance == null) {
            instance = new LayoutTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the LayoutTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized LayoutTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(LayoutTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof LayoutTopComponent) {
            return (LayoutTopComponent) win;
        }
        Logger.getLogger(LayoutTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
