/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.ui.context;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.AttributedString;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import kobic.prefuse.Constants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.mongkie.context.spi.ContextUI;
import org.mongkie.filter.FilterController;
import org.mongkie.filter.FilterModelListener;
import org.mongkie.filter.spi.Filter;
import org.mongkie.util.AccumulativeEventsProcessor;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ContextUI.class, position = 1)
public class VisibilityContextPanel extends javax.swing.JPanel implements ContextUI, FilterModelListener, GraphListener {

    private final VisibilityDataSet nodesVisibility, edgesVisibility;
    private static final String NA = "NA";

    /**
     * Creates new form VisibilityContextPanel
     */
    public VisibilityContextPanel() {
        Lookup.getDefault().lookup(FilterController.class); // Bootstrap the filter controller instance
        initComponents();
        nodesPiePanel.add(
                new VisibilityPie("Nodes", nodesVisibility = new VisibilityDataSet(Constants.NODES)),
                BorderLayout.CENTER);
        edgesPiePanel.add(
                new VisibilityPie("Edges", edgesVisibility = new VisibilityDataSet(Constants.EDGES)),
                BorderLayout.CENTER);
    }

    @Override
    public String getName() {
        return "Visibility";
    }

    @Override
    public void graphChanged(Graph g, String table, int start, int end, int col, int type) {
        if (!currentDisplay.isLoading()
                && col == EventConstants.ALL_COLUMNS) {
            // refreshLazy(currentDisplay);
            currentDisplay.getVisualization().invokeAfterDataProcessing(this,
                    new Runnable() {
                        @Override
                        public void run() {
                            refresh(currentDisplay);
                        }
                    });
        }
    }

    private void refreshLazy(final MongkieDisplay display) {
        if (refreshQ != null && refreshQ.isAccumulating()) {
            refreshQ.eventAttended();
        } else {
            refreshQ = new AccumulativeEventsProcessor(new Runnable() {
                @Override
                public void run() {
                    refresh(display);
                }
            });
            refreshQ.start();
        }
    }
    private AccumulativeEventsProcessor refreshQ;

    @Override
    public void refresh(final MongkieDisplay display) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                nodesVisibility.update(display);
                int nodesFullCount = nodesVisibility.getFullCount();
                nodesCountInfoLabel.setText(nodesFullCount == 0 ? NA : nodesVisibility.getVisibleCount() + "/" + nodesFullCount);
                edgesVisibility.update(display);
                int edgesFullCount = edgesVisibility.getFullCount();
                edgesCountInfoLabel.setText(edgesFullCount == 0 ? NA : edgesVisibility.getVisibleCount() + "/" + edgesFullCount);
                if (currentDisplay != display) {
                    if (currentDisplay != null) {
                        Lookup.getDefault().lookup(FilterController.class).getModel(currentDisplay).removeModelListener(VisibilityContextPanel.this);
                        currentDisplay.getGraph().removeGraphModelListener(VisibilityContextPanel.this);
                    }
                    currentDisplay = display;
                    Lookup.getDefault().lookup(FilterController.class).getModel(currentDisplay).addModelListener(VisibilityContextPanel.this);
                    currentDisplay.getGraph().addGraphModelListener(VisibilityContextPanel.this);
                }
                if (!isVisible()) {
                    setVisible(true);
                }
            }
        });
    }
    private MongkieDisplay currentDisplay;

    @Override
    public void unload() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (currentDisplay != null) {
                    Lookup.getDefault().lookup(FilterController.class).getModel(currentDisplay).removeModelListener(VisibilityContextPanel.this);
                    currentDisplay.getGraph().removeGraphModelListener(VisibilityContextPanel.this);
                    currentDisplay = null;
                }
                setVisible(false);
            }
        });
    }

    @Override
    public void fitersApplied(Set<Filter> nodeFilters, Set<Filter> edgeFilters) {
        assert currentDisplay != null;
        refreshLazy(currentDisplay);
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/mongkie/ui/context/resources/context.png", false);
    }

    @Override
    public String getTooltip() {
        return null;
    }

    @Override
    public JPanel getPanel() {
        return this;
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
        java.awt.GridBagConstraints gridBagConstraints;

        jXTitledSeparator1 = new org.jdesktop.swingx.JXTitledSeparator();
        nodesPiePanel = new javax.swing.JPanel();
        jXTitledSeparator2 = new org.jdesktop.swingx.JXTitledSeparator();
        edgesPiePanel = new javax.swing.JPanel();
        nodesCountInfoLabel = new javax.swing.JLabel();
        edgesCountInfoLabel = new javax.swing.JLabel();

        jXTitledSeparator1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jXTitledSeparator1.setTitle(org.openide.util.NbBundle.getMessage(VisibilityContextPanel.class, "VisibilityContextPanel.jXTitledSeparator1.title")); // NOI18N

        nodesPiePanel.setLayout(new java.awt.BorderLayout());

        jXTitledSeparator2.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jXTitledSeparator2.setTitle(org.openide.util.NbBundle.getMessage(VisibilityContextPanel.class, "VisibilityContextPanel.jXTitledSeparator2.title")); // NOI18N

        edgesPiePanel.setLayout(new java.awt.BorderLayout());

        nodesCountInfoLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        nodesCountInfoLabel.setLabelFor(nodesPiePanel);
        org.openide.awt.Mnemonics.setLocalizedText(nodesCountInfoLabel, org.openide.util.NbBundle.getMessage(VisibilityContextPanel.class, "VisibilityContextPanel.nodesCountInfoLabel.text")); // NOI18N

        edgesCountInfoLabel.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        edgesCountInfoLabel.setLabelFor(edgesPiePanel);
        org.openide.awt.Mnemonics.setLocalizedText(edgesCountInfoLabel, org.openide.util.NbBundle.getMessage(VisibilityContextPanel.class, "VisibilityContextPanel.edgesCountInfoLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jXTitledSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nodesCountInfoLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(edgesCountInfoLabel, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addComponent(nodesPiePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .addComponent(edgesPiePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXTitledSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodesCountInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nodesPiePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jXTitledSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgesCountInfoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(edgesPiePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel edgesCountInfoLabel;
    private javax.swing.JPanel edgesPiePanel;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator1;
    private org.jdesktop.swingx.JXTitledSeparator jXTitledSeparator2;
    private javax.swing.JLabel nodesCountInfoLabel;
    private javax.swing.JPanel nodesPiePanel;
    // End of variables declaration//GEN-END:variables

    private static class VisibilityPie extends ChartPanel {

        VisibilityPie(String title, VisibilityDataSet data) {
            super(ChartFactory.createPieChart(title, data, false, false, false), true, true, true, false, true);
            setMinimumDrawWidth(0);
            setMaximumDrawWidth(Integer.MAX_VALUE);
            setMinimumDrawHeight(0);
            setMaximumDrawHeight(Integer.MAX_VALUE);
            JFreeChart chart = getChart();
            chart.setTitle(new TextTitle());
            chart.setBackgroundPaint(null);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setShadowPaint(null);
            plot.setOutlineVisible(false);
            plot.setInteriorGap(0);
            plot.setSimpleLabels(true);
            plot.setLabelBackgroundPaint(null);
            plot.setLabelPaint(Color.WHITE);
            plot.setLabelOutlineStroke(null);
            plot.setLabelShadowPaint(null);
            plot.setLabelFont(new java.awt.Font("Dialog", 0, 10));
            plot.setLabelGap(0.5);
            plot.setLabelGenerator(new PieSectionLabelGenerator() {
                @Override
                public String generateSectionLabel(PieDataset pd, Comparable section) {
                    VisibilityDataSet visibility = (VisibilityDataSet) pd;
                    int fullCount = visibility.getFullCount();
                    if (fullCount == 0) {
                        return section.toString();
                    } else if (VISIBLE.equals(section)) {
                        return VISIBLE + " (" + visibility.getVisibleCount() + ")";
                    } else if (NOT_VISIBLE.equals(section)) {
                        return NOT_VISIBLE + " (" + (fullCount - visibility.getVisibleCount()) + ")";
                    }
                    throw new AssertionError();
                }

                @Override
                public AttributedString generateAttributedSectionLabel(PieDataset pd, Comparable cmprbl) {
                    throw new UnsupportedOperationException("Not supported.");
                }
            });
            plot.setCircular(true);
            plot.setBackgroundPaint(null);
            plot.setBackgroundAlpha(1f);
            plot.setSectionPaint(VISIBLE, new Color(0x444444));
            plot.setSectionPaint(NOT_VISIBLE, new Color(0xAAAAAA));
            setOpaque(false);
            // setPopupMenu(null);
        }
    }

    private static class VisibilityDataSet extends DefaultPieDataset {

        private int visibleCount, fullCount;
        private final String group;

        VisibilityDataSet(String group) {
            setValue(VISIBLE, 1.0);
            setValue(NOT_VISIBLE, 0.0);
            this.group = group;
        }

        void update(MongkieDisplay display) {
            fullCount = count(display.getVisualization().items(group));
            visibleCount = count(display.getVisualization().visibleItems(group));
            setValue(VISIBLE, fullCount == 0 ? 1.0 : visibleCount);
            setValue(NOT_VISIBLE, fullCount - visibleCount);
        }

        private int count(Iterator items) {
            int i = 0;
            while (items.hasNext()) {
                items.next();
                i++;
            }
            return i;
        }

        int getVisibleCount() {
            return visibleCount;
        }

        int getFullCount() {
            return fullCount;
        }
    }
    private static final String VISIBLE = "Visible";
    private static final String NOT_VISIBLE = "Not visible";
}
