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
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.data.io.CSVTableWriter;
import prefuse.data.io.DataIOException;

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
                        for (Iterator<Tuple> nodes = g.getNodes().tuples(); nodes.hasNext();) {
                            Tuple node = nodes.next();
                            int r = table.addRow();
                            table.setInt(r, Graph.INTERNAL_NODE_ID, node.getRow());
                            for (int i = 0; i < outline.getColumnCount(); i++) {
                                String field = outline.getColumnName(i);
                                table.set(r, outline.getColumnName(i), node.get(field));
                            }
                        }
                        writer.writeTable(table, out);
                    } else {
                        prefuse.data.Table table = outline.instantiate();
                        for (Iterator<Tuple> nodes = display.getGraph().getNodeTable().tuples(); nodes.hasNext();) {
                            Tuple node = nodes.next();
                            int r = table.addRow();
                            for (int i = 0; i < outline.getColumnCount(); i++) {
                                String field = outline.getColumnName(i);
                                table.set(r, outline.getColumnName(i), node.get(field));
                            }
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
                        for (Iterator<Edge> edges = g.edges(); edges.hasNext();) {
                            Edge edge = edges.next();
                            int r = table.addRow();
                            table.setInt(r, Graph.DEFAULT_SOURCE_KEY, edge.getSourceNode().getRow());
                            table.setInt(r, Graph.DEFAULT_TARGET_KEY, edge.getTargetNode().getRow());
                            for (int i = 0; i < outline.getColumnCount(); i++) {
                                String field = outline.getColumnName(i);
                                table.set(r, outline.getColumnName(i), edge.get(field));
                            }
                        }
                        writer.writeTable(table, out);
                    } else {
                        prefuse.data.Table table = outline.instantiate();
                        for (Iterator<Tuple> edges = g.getEdges().tuples(); edges.hasNext();) {
                            Tuple edge = edges.next();
                            int r = table.addRow();
                            for (int i = 0; i < outline.getColumnCount(); i++) {
                                String field = outline.getColumnName(i);
                                table.set(r, outline.getColumnName(i), edge.get(field));
                            }
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
