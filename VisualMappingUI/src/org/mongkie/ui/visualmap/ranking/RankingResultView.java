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
package org.mongkie.ui.visualmap.ranking;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import kobic.prefuse.Constants;
import org.jdesktop.swingx.JXTable;
import org.mongkie.lib.widgets.WidgetUtilities;
import org.mongkie.util.io.DialogFileFilter;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualmap.spi.ranking.AbstractColorTransformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.ranking.RankingEvent;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.ranking.RankingModelListener;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;
import static org.mongkie.visualmap.ranking.RankingEvent.Type.*;
import static org.mongkie.visualmap.VisualMapping.*;

/**
 *
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RankingResultView extends JScrollPane implements RankingModelListener {

    private final String LAST_PATH = "RankingListScrollPane_Screenshot_Last_Path";
    private final String LAST_PATH_DEFAULT = "RankingListScrollPane_Screenshot_Last_Path_Default";
    private JXTable table;
    private JPopupMenu popupMenu;
    private RankingModel model;

    public RankingResultView() {
        initTable();
        initTablePopup();
    }

    @Override
    public void processRankingEvent(RankingEvent e) {
        if (e.is(APPLY_TRANSFORMER)) {
            refreshTable();
        }
    }

    public void select(RankingModel model) {
        this.model = model;
        refreshTable();
        model.addModelListener(RankingResultView.this);
    }

    public void unselect() {
        if (model != null) {
            model.removeModelListener(RankingResultView.this);
        }
        model = null;
    }

    private void refreshTable() {
        Ranking ranking = model.getCurrentRanking();
        Transformer transformer = model.getCurrentTransformer();

        if (ranking != null && model.isRankingListVisible() && transformer instanceof AbstractColorTransformer) {
            fetchTable(ranking, transformer);
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    table.setModel(new ResultListTableModel(new RankCell[0]));
                    setViewportView(null);
                }
            });
        }
    }

    private void initTable() {
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
        //table.setHighlighters(HighlighterFactory.createAlternateStriping());

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);

        table.setBorder(null);

        setViewportView(table);
    }

    private void initTablePopup() {
        popupMenu = new JPopupMenu();
        JMenuItem screenshotItem = new JMenuItem(NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.screenshot.popupMenuItem.text"));
        screenshotItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage image = WidgetUtilities.createComponentScreenshot(table);
                    writeImage(image);
                } catch (Exception ex) {
                    String msg = NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.screenshot.errorMsg", new Object[]{ex.getClass().getSimpleName(), ex.getLocalizedMessage(), ex.getStackTrace()[0].getClassName(), ex.getStackTrace()[0].getLineNumber()});
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), msg, NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.screenshot.errorTitle"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        popupMenu.add(screenshotItem);

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
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void writeImage(BufferedImage image) throws Exception {
        //Get last directory
        String lastPathDefault = NbPreferences.forModule(RankingResultView.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(RankingResultView.class).get(LAST_PATH, lastPathDefault);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setDialogTitle(NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.screenshot.filechooser.title"));
        DialogFileFilter dialogFileFilter = new DialogFileFilter(NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.screenshot.filechooser.pngDescription"));
        dialogFileFilter.addExtension("png");
        chooser.addChoosableFileFilter(dialogFileFilter);
        File selectedFile = new File(chooser.getCurrentDirectory(), "ranking_table.png");
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showSaveDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }
        selectedFile = chooser.getSelectedFile();
        if (!selectedFile.getPath().endsWith(".png")) {
            selectedFile = new File(selectedFile.getPath() + ".png");
        }
        //Save last path
        String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
        NbPreferences.forModule(RankingResultView.class).put(LAST_PATH, defaultDirectory);
        if (!ImageIO.write(image, "png", selectedFile)) {
            throw new IOException("Unsupported file format");
        }
    }

    private void fetchTable(Ranking ranking, Transformer transformer) {
        final List<RankCell> cells = new ArrayList<RankCell>();

        MongkieDisplay display = model.getDisplay();
        Graph graph = display.getGraph();
        if (ranking.getElementType().equals(NODE_ELEMENT)) {
            for (Iterator<Node> nodeIter = graph.nodes(); nodeIter.hasNext();) {
                Node node = nodeIter.next();
                Number rank = ranking.getValue(node);
                VisualItem nitem = display.getVisualization().getVisualItem(Constants.NODES, node);
                String nlabel = graph.getNodeLabelField();
                ColorRankCell cell = new ColorRankCell(ColorLib.getColor(nitem.getFillColor()), rank, nlabel == null ? "" : node.getString(nlabel));
                cells.add(cell);
            }
        } else if (ranking.getElementType().equals(EDGE_ELEMENT)) {
            for (Iterator<Edge> edgeIter = graph.edges(); edgeIter.hasNext();) {
                Edge edge = edgeIter.next();
                Number rank = ranking.getValue(edge);
                VisualItem eitem = display.getVisualization().getVisualItem(Constants.EDGES, edge);
                String elabel = graph.getEdgeLabelField();
                ColorRankCell cell = new ColorRankCell(ColorLib.getColor(eitem.getStrokeColor()), rank, elabel == null ? "" : edge.getString(elabel));
                cells.add(cell);
            }
        }

        Collections.sort(cells);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ResultListTableModel m = new ResultListTableModel(cells.toArray(new RankCell[0]));
                table.setDefaultRenderer(RankCell.class, new RankCellRenderer());
                TableRowSorter tableRowSorter = new TableRowSorter(m);
                tableRowSorter.setComparator(0, new Comparator<RankCell>() {

                    @Override
                    public int compare(RankCell t, RankCell t1) {
                        return t.compareTo(t1);
                    }
                });
                List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
                sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
                tableRowSorter.setSortKeys(sortKeys);

                table.setRowSorter(tableRowSorter);
                table.setModel(m);
                setViewportView(table);
            }
        });
    }

    private class ResultListTableModel implements TableModel {

        private RankCell[] ranks;

        public ResultListTableModel(RankCell[] ranks) {
            this.ranks = ranks;
        }

        @Override
        public int getRowCount() {
            return ranks.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.columnName.rank");
            } else {
                return NbBundle.getMessage(RankingResultView.class, "RankingListScrollPane.columnName.label");
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return RankCell.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return ranks[rowIndex];
            } else {
                return ranks[rowIndex].getLabel();
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                ranks[rowIndex] = (RankCell) aValue;
            } else {
                ranks[rowIndex].setLabel((String) aValue);
            }
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
        }
    }

    private static interface RankCell extends Comparable<RankCell> {

        public void render(JLabel label);

        public String getLabel();

        public void setLabel(String label);
    }

    private static class ColorRankCell implements RankCell {

        private final Color color;
        private final Number rank;
        private String label;

        public ColorRankCell(Color color, Number rank, String label) {
            this.color = color;
            this.rank = rank;
            this.label = label;
        }

        @Override
        public void render(JLabel label) {
            label.setBackground(color);
            label.setForeground(WidgetUtilities.getForegroundColorForBackground(color));
            label.setText(rank.toString());
        }

        @Override
        public int compareTo(RankCell t) {
            double d2 = rank.doubleValue();
            double d1 = ((ColorRankCell) t).rank.doubleValue();
            if (d1 < d2) {
                return -1;
            } else if (d1 > d2) {
                return 1;
            }
            return 0;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public void setLabel(String label) {
            this.label = label;
        }
    }

    private static class RankCellRenderer extends DefaultTableCellRenderer {

        public RankCellRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            RankCell rankCell = (RankCell) value;
            rankCell.render(this);
            return this;
        }
    }
}
