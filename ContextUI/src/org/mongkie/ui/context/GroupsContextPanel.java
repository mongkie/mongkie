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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import kobic.prefuse.Constants;
import org.jdesktop.swingx.JXTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.util.SortOrder;
import org.mongkie.context.spi.ContextUI;
import org.mongkie.lib.widgets.WidgetUtilities;
import org.mongkie.util.AccumulativeEventsProcessor;
import org.mongkie.util.io.DialogFileFilter;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.group.GroupListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import prefuse.Visualization;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ContextUI.class, position = 0)
public class GroupsContextPanel extends javax.swing.JPanel implements ContextUI, GroupListener, TableListener {

    private final GroupsPie pie;

    /**
     * Creates new form DefaultContextPanel
     */
    public GroupsContextPanel() {
        initComponents();
        groupListScrollPane.setVisible(false);
        groupsPiePanel.add(pie = new GroupsPie(), BorderLayout.CENTER);
        Lookup.getDefault().lookup(VisualizationController.class).getGroupManager().addGroupListener(GroupsContextPanel.this);
    }

    @Override
    public String getName() {
        return "Groups";
    }

    @Override
    public void tableChanged(Table t, int start, int end, int col, int type) {
        if (type == EventConstants.UPDATE
                && (col == VisualItem.IDX_FILLCOLOR || col == t.getColumnNumber(AggregateItem.AGGR_NAME))) {
            refreshLazy(currentDisplay);
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
                DefaultPieDataset data = pie.update(display);
                if (data != null) {
                    ((GroupListView) groupListScrollPane).refresh(display, data);
                    if (!groupListScrollPane.isVisible()) {
                        groupListScrollPane.setVisible(true);
                        revalidate();
                        repaint();
                    }
                } else {
                    ((GroupListView) groupListScrollPane).unload();
                    groupListScrollPane.setVisible(false);
                }
                if (currentDisplay != display) {
                    if (currentDisplay != null) {
                        ((AggregateTable) currentDisplay.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS)).removeTableListener(GroupsContextPanel.this);
                    }
                    currentDisplay = display;
                    ((AggregateTable) currentDisplay.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS)).addTableListener(GroupsContextPanel.this);
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
                ((GroupListView) groupListScrollPane).unload();
                if (currentDisplay != null) {
                    ((AggregateTable) currentDisplay.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS)).removeTableListener(GroupsContextPanel.this);
                    currentDisplay = null;
                }
                setVisible(false);
            }
        });
    }

    @Override
    public void memberChanged(AggregateItem group, List<NodeItem> olds) {
        if (currentDisplay == (MongkieDisplay) group.getVisualization().getDisplay(0)) {
            refreshLazy(currentDisplay);
        }
    }

    @Override
    public void grouped(AggregateItem group) {
        if (currentDisplay == (MongkieDisplay) group.getVisualization().getDisplay(0)) {
            refreshLazy(currentDisplay);
        }
    }

    @Override
    public void ungrouped(AggregateItem group, List<NodeItem> olds) {
        if (currentDisplay == (MongkieDisplay) group.getVisualization().getDisplay(0)) {
            refreshLazy(currentDisplay);
        }
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/mongkie/ui/context/resources/groups.png", false);
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

        groupsPiePanel = new javax.swing.JPanel();
        groupListScrollPane = new GroupListView();

        groupsPiePanel.setLayout(new java.awt.BorderLayout());

        groupListScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 45, 10, 45));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(groupListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                    .addComponent(groupsPiePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(groupsPiePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane groupListScrollPane;
    private javax.swing.JPanel groupsPiePanel;
    // End of variables declaration//GEN-END:variables

    private static class GroupsPie extends ChartPanel {

        private final PiePlot plot;

        GroupsPie() {
            super(ChartFactory.createPieChart("Groups", new DefaultPieDataset(), false, false, false), true, true, true, false, true);
            setMinimumDrawWidth(0);
            setMaximumDrawWidth(Integer.MAX_VALUE);
            setMinimumDrawHeight(0);
            setMaximumDrawHeight(Integer.MAX_VALUE);
            JFreeChart chart = getChart();
            chart.setTitle(new TextTitle());
            chart.setBackgroundPaint(null);
            plot = (PiePlot) chart.getPlot();
            // plot.setSimpleLabels(true);
            plot.setLabelBackgroundPaint(null);
            plot.setLabelPaint(Color.BLACK);
            plot.setLabelOutlineStroke(null);
            plot.setLabelShadowPaint(null);
            plot.setLabelFont(new java.awt.Font("Dialog", 0, 10));
            // plot.setLabelGap(0.05);
            // plot.setInteriorGap(0);
            plot.setOutlineVisible(false);
            plot.setShadowPaint(null);
            plot.setLabelGenerator(new PieSectionLabelGenerator() {
                @Override
                public String generateSectionLabel(PieDataset pd, Comparable section) {
                    if (section instanceof AggregateItem) {
                        AggregateItem group = (AggregateItem) section;
                        return group.getString(AggregateItem.AGGR_NAME) + " (" + group.getAggregateSize() + ")";
                    } else {
                        return section.toString();
                    }
                }

                @Override
                public AttributedString generateAttributedSectionLabel(PieDataset pd, Comparable cmprbl) {
                    throw new UnsupportedOperationException("Not supported.");
                }
            });
            plot.setCircular(true);
            plot.setBackgroundPaint(null);
            plot.setBackgroundAlpha(1f);
            setOpaque(false);
            // setPopupMenu(null);
        }

        DefaultPieDataset update(MongkieDisplay display) {
            DefaultPieDataset data = (DefaultPieDataset) plot.getDataset();
            data.clear();
            List<AggregateItem> aggregates = new ArrayList<AggregateItem>();
            for (Iterator<AggregateItem> groups = display.getVisualization().items(Visualization.AGGR_ITEMS); groups.hasNext();) {
                AggregateItem group = groups.next();
                if (group.isValid()) {
                    aggregates.add(group);
                }
            }
            if (aggregates.isEmpty()) {
                String nogroup = NO_GROUP + " (" + display.getGraph().getNodeCount() + ")";
                data.setValue(nogroup, 1.0);
                plot.setSectionPaint(nogroup, new Color(0xAAAAAA));
                return null;
            } else {
                for (AggregateItem group : aggregates) {
                    data.setValue(group, group.getAggregateSize());
                    // plot.setSectionPaint(group, ColorLib.getColor(group.getFillColor()));
                    plot.setSectionPaint(group, ColorLib.getColor(ColorLib.setAlpha(group.getFillColor(), 255)));
                }
                data.sortByKeys(SortOrder.DESCENDING);
            }
            return data;
        }
    }
    private static final String NO_GROUP = "No group";

    private static class GroupListView extends JScrollPane {

        private static final String LAST_SCREENSHOT_PATH = "GroupListView_ScreenShot_Last_Path";
        private JXTable table;
        private JPopupMenu screenshotPopup;

        GroupListView() {
            table = new JXTable();
            // Disable strange hack that overwrite JLabel's setBackground() by a Highlighter color
            table.putClientProperty(JXTable.USE_DTCR_COLORMEMORY_HACK, Boolean.FALSE);
            table.setColumnControlVisible(false);
            table.setEditable(false);
            table.setSortable(false);
            table.setRolloverEnabled(false);
            table.setShowHorizontalLines(false);
            table.setShowVerticalLines(false);
            table.setRowSelectionAllowed(false);
            table.setColumnSelectionAllowed(false);
            table.setCellSelectionEnabled(false);
            table.setTableHeader(null);
            table.setDefaultRenderer(Cell.class, new CellRenderer());
            setViewportView(table);

            screenshotPopup = new JPopupMenu();
            JMenuItem screenshot = new JMenuItem("Take Screenshot");
            screenshot.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        BufferedImage image = WidgetUtilities.createComponentScreenshot(table);
                        writeImage(image);
                    } catch (Exception ex) {
                        String msg = NbBundle.getMessage(GroupListView.class, "GroupListView.screenshot.errorMsg",
                                new Object[]{ex.getClass().getSimpleName(),
                                    ex.getLocalizedMessage(), ex.getStackTrace()[0].getClassName(),
                                    ex.getStackTrace()[0].getLineNumber()});
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            });
            screenshotPopup.add(screenshot);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    showPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        screenshotPopup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }

        private void writeImage(BufferedImage image) throws Exception {
            String lastPath = NbPreferences.forModule(GroupListView.class).get(LAST_SCREENSHOT_PATH, null);
            final JFileChooser chooser = new JFileChooser(lastPath);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setDialogTitle("Save As...");
            DialogFileFilter dialogFileFilter = new DialogFileFilter("PNG");
            dialogFileFilter.addExtension(".png");
            chooser.addChoosableFileFilter(dialogFileFilter);
            File selectedFile = new File(chooser.getCurrentDirectory(), "group_list.png");
            chooser.setSelectedFile(selectedFile);
            int returnFile = chooser.showSaveDialog(null);
            if (returnFile != JFileChooser.APPROVE_OPTION) {
                return;
            }
            selectedFile = chooser.getSelectedFile();
            if (!selectedFile.getPath().endsWith(".png")) {
                selectedFile = new File(selectedFile.getPath() + ".png");
            }
            String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
            NbPreferences.forModule(GroupListView.class).put(LAST_SCREENSHOT_PATH, defaultDirectory);
            if (!ImageIO.write(image, "png", selectedFile)) {
                throw new IOException("Unsupported file format");
            }
        }

        void refresh(MongkieDisplay display, DefaultPieDataset data) {
            List<AggregateItem> aggregates = data.getKeys();
            Cell[] cells = new Cell[aggregates.size() + 1];
            int i = 0;
            for (AggregateItem aggregate : aggregates) {
                cells[i++] = new Cell(aggregate);
            }
            AggregateTable aggregateTable = (AggregateTable) display.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS);
            int nogroup = 0;
            for (Iterator<NodeItem> nodes = display.getVisualization().items(Constants.NODES); nodes.hasNext();) {
                NodeItem n = nodes.next();
                if (!aggregateTable.containsItem(n)) {
                    nogroup++;
                }
            }
            cells[i] = new Cell(nogroup);
            table.setModel(new GroupListTableModel(cells));
            setViewportView(table);
        }

        void unload() {
            table.setModel(new GroupListTableModel(new Cell[0]));
            setViewportView(null);
        }

        private class GroupListTableModel implements TableModel {

            private Cell[] groups;

            GroupListTableModel(Cell[] groups) {
                this.groups = groups;
            }

            @Override
            public int getRowCount() {
                return groups.length;
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public String getColumnName(int columnIndex) {
                return columnIndex == 0 ? "Size" : "Name";
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Cell.class : String.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return columnIndex == 0 ? groups[rowIndex] : groups[rowIndex].getLabel();
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    groups[rowIndex] = (Cell) aValue;
                } else {
                    groups[rowIndex].setLabel((String) aValue);
                }
            }

            @Override
            public void addTableModelListener(TableModelListener l) {
            }

            @Override
            public void removeTableModelListener(TableModelListener l) {
            }
        }

        private static class Cell {

            private final Color color;
            private final int size;
            private String label;

            Cell(AggregateItem group) {
                // this.color = ColorLib.getColor(group.getFillColor());
                this.color = ColorLib.getColor(ColorLib.setAlpha(group.getFillColor(), 255));
                this.size = group.getAggregateSize();
                this.label = group.getString(AggregateItem.AGGR_NAME);
            }

            Cell(int nogroup) {
                this.color = new Color(0x222222);
                this.size = nogroup;
                this.label = "No group";
            }

            void render(JLabel label) {
                label.setBackground(color);
                label.setForeground(WidgetUtilities.getForegroundColorForBackground(color));
                label.setText(String.valueOf(size));
            }

            String getLabel() {
                return label;
            }

            void setLabel(String label) {
                this.label = label;
            }
        }

        private static class CellRenderer extends DefaultTableCellRenderer {

            public CellRenderer() {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Cell cell = (Cell) value;
                cell.render(this);
                return this;
            }
        }
    }
}
