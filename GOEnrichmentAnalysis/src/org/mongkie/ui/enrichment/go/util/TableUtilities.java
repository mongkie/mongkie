package org.mongkie.ui.enrichment.go.util;

import gobean.util.GoBeanUtils;
import javax.swing.JTable;
import javax.swing.tree.TreePath;
import org.jdesktop.swingx.JXTreeTable;

public class TableUtilities {

    public static StringBuilder getCopyContents(JTable table) {
        return getStringRepresentation(getValuesToCopy(table));
    }

    public static StringBuilder getCopyContents(JXTreeTable treeTableView) {
        int nRow = treeTableView.getRowCount();
        int nColumn = treeTableView.getColumnCount();
        StringBuilder builder = new StringBuilder();
        builder.append("Depth").append("\t");
        for (int j = 0; j < nColumn; j++) {
            builder.append(treeTableView.getColumnName(j)).append("\t");
        }
        builder.append(GoBeanUtils.NEW_LINE);
        for (int i = 0; i < nRow; i++) {
            TreePath path = treeTableView.getPathForRow(i);
            int pathCount = path.getPathCount();
            builder.append(pathCount).append("\t");
            for (int j = 0; j < nColumn; j++) {
                builder.append(treeTableView.getStringAt(i, j));
                if (j < nColumn - 1) {
                    builder.append("\t");
                }
            }
            builder.append(GoBeanUtils.NEW_LINE);
        }
        return builder;
    }

    private static Object[][] getValuesToCopy(JTable tableView) {
        if (tableView.getSelectedColumnCount() > 0 && tableView.getSelectedRowCount() > 0) {
            return getSelectedValues(tableView);
        } else {
            return getAllValues(tableView);
        }
    }

    private static StringBuilder getStringRepresentation(Object[][] values) {
        StringBuilder buffer = new StringBuilder();
        for (Object[] row : values) {
            for (int i = 0; i < row.length; i++) {
                Object value = row[i];
                if (value != null) {
                    buffer.append(value.toString());
                }
                if (i < row.length - 1) {
                    buffer.append("\t");
                }
            }
            buffer.append(GoBeanUtils.NEW_LINE);
        }
        return buffer;
    }

    private static Object[][] getSelectedValues(JTable tableView) {
        int[] selectedRows = tableView.getSelectedRows();
        int[] selectedColumns = tableView.getSelectedColumns();
        Object[][] results = new Object[selectedRows.length + 1][selectedColumns.length];
        int j = 0;
        for (int column : selectedColumns) {
            results[0][j++] = tableView.getColumnName(column);
        }
        int i = 0;
        for (int row : selectedRows) {
            j = 0;
            for (int column : selectedColumns) {
                results[i + 1][j++] = tableView.getValueAt(row, column);
            }
            i++;
        }
        return results;
    }

    private static Object[][] getAllValues(JTable tableView) {
        int nColumn = tableView.getColumnCount();
        int nRow = tableView.getRowCount();
        Object[][] results = new Object[nRow + 1][nColumn];
        for (int j = 0; j < nColumn; j++) {
            results[0][j] = tableView.getColumnName(j);
        }
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nColumn; j++) {
                results[i + 1][j] = tableView.getValueAt(i, j);
            }
        }
        return results;
    }
}
