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
package org.mongkie.exporter.plugins.graph;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.exporter.spi.AbstractGraphExporter;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.openide.util.Lookup;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.io.CSVTableWriter;
import prefuse.data.io.DataIOException;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ExporterCSV extends AbstractGraphExporter {

    private final CSVTableWriter writer = new CSVTableWriter(true);
    private Table tableToExport = Table.NODE;
    private boolean tableSelectable = true;
    private boolean exportInternalIdColumns = false;

    ExporterCSV() {
    }

    ExporterCSV(Table tableToExport) {
        this.tableToExport = tableToExport;
        this.tableSelectable = false;
    }

    boolean isTableSelectable() {
        return tableSelectable;
    }

    boolean isPrintHeader() {
        return writer.isPrintHeader();
    }

    void setPrintHeader(boolean printHeader) {
        writer.setPrintHeader(printHeader);
    }

    boolean isExportInternalIdColumns() {
        return exportInternalIdColumns;
    }

    void setExportInternalIdColumns(boolean exportInternalIdColumns) {
        this.exportInternalIdColumns = exportInternalIdColumns;
    }

    Table getTableToExport() {
        return tableToExport;
    }

    void setTableToExport(Table tableToExport) {
        if (tableSelectable) {
            this.tableToExport = tableToExport;
        }
    }

    @Override
    public boolean execute() {
        try {
            Schema outline;
            Graph g = display.getGraph();
            switch (tableToExport) {
                case NODE:
                    outline = display.getNodeDataViewSupport().getOutlineSchema();
                    if (exportInternalIdColumns) {
                        prefuse.data.Table table = new prefuse.data.Table();
                        table.addColumn(Graph.INTERNAL_NODE_ID, int.class);
                        addOutlineColumns(table, outline);
                        for (Iterator<Node> nodes = getNodes(); nodes.hasNext();) {
                            Node node = nodes.next();
                            int row = addRowWithOutlineContents(table, node, outline);
                            table.setInt(row, Graph.INTERNAL_NODE_ID, node.getRow());
                        }
                        writer.writeTable(table, out);
                    } else {
                        prefuse.data.Table table = outline.instantiate();
                        for (Iterator<Node> nodes = getNodes(); nodes.hasNext();) {
                            Node node = nodes.next();
                            addRowWithOutlineContents(table, node, outline);
                        }
                        writer.writeTable(table, out);
                    }
                    break;
                case EDGE:
                    outline = display.getEdgeDataViewSupport().getOutlineSchema();
                    if (exportInternalIdColumns) {
                        prefuse.data.Table table = new prefuse.data.Table();
                        table.addColumn(Graph.DEFAULT_SOURCE_KEY, int.class);
                        table.addColumn(Graph.DEFAULT_TARGET_KEY, int.class);
                        addOutlineColumns(table, outline);
                        for (Iterator<Edge> edges = getEdges(); edges.hasNext();) {
                            Edge edge = edges.next();
                            int row = addRowWithOutlineContents(table, edge, outline);
                            table.setInt(row, Graph.DEFAULT_SOURCE_KEY, edge.getSourceNode().getRow());
                            table.setInt(row, Graph.DEFAULT_TARGET_KEY, edge.getTargetNode().getRow());
                        }
                        writer.writeTable(table, out);
                    } else {
                        prefuse.data.Table table = outline.instantiate();
                        for (Iterator<Edge> edges = getEdges(); edges.hasNext();) {
                            Edge edge = edges.next();
                            addRowWithOutlineContents(table, edge, outline);
                        }
                        writer.writeTable(table, out);
                    }
                    break;
                default:
                    return false;
            }
        } catch (DataIOException ex) {
            Logger.getLogger(ExporterCSV.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("An error happened when writing to the CSV file", ex);
        }
        return true;
    }

    private void addOutlineColumns(prefuse.data.Table table, Schema outline) {
        for (int i = 0; i < outline.getColumnCount(); i++) {
            table.addColumn(outline.getColumnName(i), outline.getColumnType(i), outline.getDefault(i));
        }
    }

    private int addRowWithOutlineContents(prefuse.data.Table table, Tuple source, Schema outline) {
        int row = table.addRow();
        for (int i = 0; i < outline.getColumnCount(); i++) {
            String field = outline.getColumnName(i);
            table.set(row, outline.getColumnName(i), source.get(field));
        }
        return row;
    }

    private Iterator<Node> getNodes() {
        return isExportSelectionOnly() ? new Iterator<Node>() {
            Iterator<NodeItem> items =
                    Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedNodes(display.getVisualization());

            @Override
            public boolean hasNext() {
                return items.hasNext();
            }

            @Override
            public Node next() {
                return (Node) items.next().getSourceTuple();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        } : display.getGraph().nodes();
    }

    private Iterator<Edge> getEdges() {
        return isExportSelectionOnly() ? new Iterator<Edge>() {
            Iterator<EdgeItem> items =
                    Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedEdges(display.getVisualization());

            @Override
            public boolean hasNext() {
                return items.hasNext();
            }

            @Override
            public Edge next() {
                return (Edge) items.next().getSourceTuple();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        } : display.getGraph().edges();
    }

    MongkieDisplay getDisplay() {
        return display;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public boolean supportsSelectionOnly() {
        return true;
    }

    static enum Table {

        NODE, EDGE
    }
}
