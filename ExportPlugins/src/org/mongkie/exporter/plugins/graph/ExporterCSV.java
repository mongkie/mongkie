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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.exporter.spi.AbstractGraphExporter;
import prefuse.data.io.CSVTableWriter;
import prefuse.data.io.DataIOException;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ExporterCSV extends AbstractGraphExporter {

    private final CSVTableWriter writer = new CSVTableWriter(true);
    private Table tableToExport = Table.NODE_TABLE;
    private boolean tableSelectable = true;

    public ExporterCSV() {
    }

    public ExporterCSV(Table tableToExport) {
        this.tableToExport = tableToExport;
        this.tableSelectable = false;
    }

    boolean isTableSelectable() {
        return tableSelectable;
    }

    public boolean isPrintHeader() {
        return writer.isPrintHeader();
    }

    public void setPrintHeader(boolean printHeader) {
        writer.setPrintHeader(printHeader);
    }

    public Table getTableToExport() {
        return tableToExport;
    }

    public void setTableToExport(Table tableToExport) {
        if (tableSelectable) {
            this.tableToExport = tableToExport;
        }
    }

    @Override
    public boolean execute() {
        try {
            switch (tableToExport) {
                case NODE_TABLE:
                    writer.writeTable(display.getGraph().getNodeTable(), out);
                    break;
                case EDGE_TABLE:
                    writer.writeTable(display.getGraph().getEdgeTable(), out);
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

    @Override
    public boolean cancel() {
        return false;
    }

    public static enum Table {

        NODE_TABLE, EDGE_TABLE
    }
}
