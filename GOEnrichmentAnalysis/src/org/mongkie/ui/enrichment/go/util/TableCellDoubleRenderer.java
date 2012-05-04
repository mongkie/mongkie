package org.mongkie.ui.enrichment.go.util;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableCellDoubleRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Object formatted;
        if (null == value) {
            formatted = "";
        } else {
            formatted = UIUtilities.DECIMAL_FORMATTER.format(value);
        }
        return super.getTableCellRendererComponent(table, formatted, isSelected, hasFocus, row, column);
    }
}
