/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.ui.series;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.mongkie.lib.heatmap.Gradient;
import org.mongkie.lib.heatmap.HeatMap;
import org.mongkie.lib.widgets.BusyLabel;
import org.mongkie.perspective.spi.BottomTopComponent;
import org.mongkie.series.*;
import static org.mongkie.visualization.Config.MODE_CONTEXT;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualization.color.ColorProvider;
import org.mongkie.visualization.color.NodeColorProvider;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.series//HeatMap//EN",
autostore = false)
@TopComponent.Description(preferredID = HeatMapTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/series/resources/heatmap.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_CONTEXT, openAtStartup = false, roles = ROLE_NETWORK, position = 200)
public final class HeatMapTopComponent extends TopComponent implements SeriesModelListener {

    private static HeatMapTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String PREFERRED_ID = "HeatMapTopComponent";
    private HeatMap heatmapPanel;
    private ColorProvider<NodeItem> nodeColorizer;
    private SeriesModel model;
    private BusyLabel loadingLabel;
    private int slidingValue = 0;

    public HeatMapTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(HeatMapTopComponent.class, "CTL_HeatMapTopComponent"));
        setToolTipText(NbBundle.getMessage(HeatMapTopComponent.class, "HINT_HeatMapTopComponent"));

        // you can use a pre-defined gradient:
        heatmapPanel = new HeatMap(SeriesData.EMPTY, true, Gradient.GRADIENT_BLUE_TO_RED);

        // or you can also make a custom gradient:
        Color[] gradientColors = new Color[]{Color.blue, Color.yellow, Color.red};
        Color[] customGradient = Gradient.createMultiGradient(gradientColors, 250);
        heatmapPanel.updateGradient(customGradient);

        // set miscelaneous settings
        heatmapPanel.setDrawLegend(true);

        heatmapPanel.setTitle("UNTITLED");
        heatmapPanel.setDrawTitle(true);

        heatmapPanel.setXAxisTitle("Samples");
        heatmapPanel.setDrawXAxisTitle(true);

        heatmapPanel.setYAxisTitle("Genes ");
        heatmapPanel.setDrawYAxisTitle(true);

//        heatmapPanel.setCoordinateBounds(0, 10, 0, 20);
        heatmapPanel.setDrawXTicks(false);
        heatmapPanel.setDrawYTicks(false);

        heatmapPanel.setColorForeground(Color.black);
        heatmapPanel.setColorBackground(Color.white);

        resultPanel.add(heatmapPanel, BorderLayout.CENTER);

        loadingLabel = new BusyLabel(
                //                NbBundle.getMessage(HeatMapTopComponent.class, "HeatMapTopComponent.heatmapScroll.loadingMessage"),
                null,
                24, 24, heatmapScroll, resultPanel);

        nodeColorizer = new NodeColorProvider() {

            @Override
            protected Color getItemTextColor(NodeItem item) {
                return null;
            }

            @Override
            protected Color getItemStrokeColor(NodeItem item) {
                return null;
            }

            @Override
            protected Color getItemFillColor(NodeItem item) {
                return heatmapPanel.getColor(slidingValue, item.getRow());
            }
        };

        Lookup.getDefault().lookup(SeriesController.class).addModelChangeListener(new SeriesModelChangeListener() {

            @Override
            public void modelChanged(SeriesModel oldModel, SeriesModel newModel) {
                if (oldModel != null) {
                    oldModel.removeModelListener(HeatMapTopComponent.this);
                }
                model = newModel;
                if (model != null) {
                    model.addModelListener(HeatMapTopComponent.this);
                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        refreshModel();
                    }
                });
            }
        });

        model = Lookup.getDefault().lookup(SeriesController.class).getModel();
        if (model != null) {
            model.addModelListener(HeatMapTopComponent.this);
        }

        refreshModel();
    }

    private void refreshModel() {
        if (model == null || model.getDisplay().getGraph().getNodeCount() < 1) {
            loadSeriesLink.setEnabled(false);
            loadingLabel.setBusy(false, loadSeriesLink);
            return;
        }

        if (model.isLoading()) {
            loadingLabel.setBusy(true);
            ((SliderTopComponent) Lookup.getDefault().lookup(BottomTopComponent.class)).setEnabled(false);
        } else if (model.isEmpty()) {
            loadSeriesLink.setEnabled(true);
            loadingLabel.setBusy(false, loadSeriesLink);
            ((SliderTopComponent) Lookup.getDefault().lookup(BottomTopComponent.class)).setEnabled(false);
        } else {
            updateHeatmap(model.getData());
            loadingLabel.setBusy(false);
        }
    }

    private void updateHeatmap(final SeriesData series) {
        heatmapPanel.setTitle(series.getTitle());
        heatmapPanel.setCoordinateBounds(0, series.getColumnCount(), 0, series.getRowCount());
        heatmapPanel.updateData(series.get(), true);
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ((SliderTopComponent) Lookup.getDefault().lookup(BottomTopComponent.class)).heatmapUpdated(series);
            }
        });
    }

    public void setSlidingValue(int slidingValue) {
        this.slidingValue = slidingValue;
        if (model != null) {
            model.getDisplay().getVisualization().rerun(Visualization.DRAW);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        resultPanel = new javax.swing.JPanel();
        resultToolbar = new javax.swing.JToolBar();
        clearButton = new javax.swing.JButton();
        heatmapScroll = new javax.swing.JScrollPane();
        loadSeriesLink = new org.jdesktop.swingx.JXHyperlink();

        resultPanel.setLayout(new java.awt.BorderLayout());

        resultToolbar.setFloatable(false);
        resultToolbar.setRollover(true);

        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/series/resources/clear.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(clearButton, org.openide.util.NbBundle.getMessage(HeatMapTopComponent.class, "HeatMapTopComponent.clearButton.text")); // NOI18N
        clearButton.setFocusable(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        resultToolbar.add(clearButton);

        resultPanel.add(resultToolbar, java.awt.BorderLayout.NORTH);

        setLayout(new java.awt.BorderLayout());

        heatmapScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));

        loadSeriesLink.setClickedColor(new java.awt.Color(0, 51, 255));
        loadSeriesLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/series/resources/external.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(loadSeriesLink, org.openide.util.NbBundle.getMessage(HeatMapTopComponent.class, "HeatMapTopComponent.loadSeriesLink.text")); // NOI18N
        loadSeriesLink.setToolTipText(org.openide.util.NbBundle.getMessage(HeatMapTopComponent.class, "HeatMapTopComponent.loadSeriesLink.toolTipText")); // NOI18N
        loadSeriesLink.setFocusPainted(false);
        loadSeriesLink.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        loadSeriesLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSeriesLinkActionPerformed(evt);
            }
        });
        heatmapScroll.setViewportView(loadSeriesLink);

        add(heatmapScroll, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void loadSeriesLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSeriesLinkActionPerformed
        Lookup.getDefault().lookup(SeriesControllerUI.class).loadSeries();
    }//GEN-LAST:event_loadSeriesLinkActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        Lookup.getDefault().lookup(SeriesController.class).clearSeries();
    }//GEN-LAST:event_clearButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearButton;
    private javax.swing.JScrollPane heatmapScroll;
    private org.jdesktop.swingx.JXHyperlink loadSeriesLink;
    private javax.swing.JPanel resultPanel;
    private javax.swing.JToolBar resultToolbar;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized HeatMapTopComponent getDefault() {
        if (instance == null) {
            instance = new HeatMapTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HeatMapTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HeatMapTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HeatMapTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HeatMapTopComponent) {
            return (HeatMapTopComponent) win;
        }
        Logger.getLogger(HeatMapTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        if (model != null && !model.isEmpty()) {
            Lookup.getDefault().lookup(ColorController.class).setNodeColorProvider(nodeColorizer);
        }
        ((SliderTopComponent) Lookup.getDefault().lookup(BottomTopComponent.class)).setVisible(true);
    }

    @Override
    public void componentClosed() {
        Lookup.getDefault().lookup(ColorController.class).unsetNodeColorProvider(nodeColorizer);
        ((SliderTopComponent) Lookup.getDefault().lookup(BottomTopComponent.class)).setVisible(false);
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

    @Override
    public void seriesChanged(Graph g, SeriesData oldData, SeriesData newData) {
        updateHeatmap(newData);
    }

    @Override
    public void seriesCleared() {
        loadSeriesLink.setEnabled(true);
        loadingLabel.setBusy(false, loadSeriesLink);
        ((SliderTopComponent) Lookup.getDefault().lookup(BottomTopComponent.class)).setEnabled(false);
        Lookup.getDefault().lookup(ColorController.class).unsetNodeColorProvider(nodeColorizer);    }

    @Override
    public void loadingStarted() {
        loadingLabel.setBusy(true);
    }

    @Override
    public void loadingFinished() {
        loadingLabel.setBusy(false);
        if (!model.isEmpty()) {
            Lookup.getDefault().lookup(ColorController.class).setNodeColorProvider(nodeColorizer);
        }
    }
}
