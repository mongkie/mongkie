package prefuse.data.tuple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.DataLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class TuplesTableModel extends AbstractTableModel {

    private Table table;
    private final List<Integer> rowList;
    private boolean boxingPrimitive;
    private Schema s;

    public TuplesTableModel(Schema s) {
        this(s, true);
    }

    public TuplesTableModel(Schema s, boolean boxingPrimitive) {
        this.s = s;
        this.boxingPrimitive = boxingPrimitive;
        table = boxingPrimitive ? s.instantiateBoxingPrimitive() : s.instantiate();
        rowList = new ArrayList();
    }

    public void setSchema(Schema s) {
        clear();
        table = boxingPrimitive ? s.instantiateBoxingPrimitive() : s.instantiate();
        this.s = s;
        fireTableStructureChanged();
    }

    public Schema getSchema() {
        return s;
    }

    public Table getTable() {
        return table;
    }

    public void clear() {
        int count = table.getRowCount();
        if (count == 0) {
            return;
        }
        Iterator<TableTuple> tuples = table.tuples();
        while (tuples.hasNext()) {
            tuples.next().setSourceTuple(null);
        }
        table.clear();
        rowList.clear();
        fireTableRowsDeleted(0, count - 1);
    }

    public boolean isEmpty() {
        return table.getRowCount() == 0;
    }

    public int addTuple(Tuple source) {
        TableTuple added = getTuple(source);
        if (added != null) {
            return indexOf(added.getRow());
        }
        added = (TableTuple) table.addSuple(source);
        added.setSourceTuple(source);
        int rowIndex = addRow(added.getRow());
        fireTableRowsInserted(rowIndex, rowIndex);
        return rowIndex;
    }

    public TableTuple getTuple(Tuple source) {
        Iterator<TableTuple> tuples = table.tuples();
        while (tuples.hasNext()) {
            TableTuple it = tuples.next();
            if (it.getSourceTuple().equals(source)) {
                return it;
            }
        }
        return null;
    }

    public boolean removeTuple(Tuple source) {
        TableTuple it = getTuple(source);
        if (it == null) {
            return false;
        }
        int row = it.getRow();
        boolean removed = table.removeTuple(it);
        if (removed) {
            it.setSourceTuple(null);
            int rowIndex = removeRow(row);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
        return removed;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return table.getColumnType(columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return table.getColumnName(column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
//        return table.isCellEditable(rowIndex, columnIndex);
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        System.out.println("setValueAt(" + rowIndex + ", " + columnIndex + ", " + aValue + ")");
        table.set(rowOf(rowIndex), columnIndex, aValue);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public int getRowCount() {
        return table.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return table.getColumnCount();
    }

    public Tuple getSourceTuple(int rowIndex) {
        return (getTuple(rowIndex)).getSourceTuple();
    }

    public TableTuple getTuple(int rowIndex) {
        return (TableTuple) table.getTuple(rowOf(rowIndex));
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return table.get(rowOf(rowIndex), columnIndex);
    }

    public int rowOf(int index) {
        if (!isValidIndex(index)) {
            throw new IllegalArgumentException("The row index is out of bounds : " + index);
        }
        return rowList.get(index);
    }

    public int indexOf(int row) {
        int rowIndex = rowList.indexOf(row);
        if (rowIndex < -1) {
            throw new IllegalArgumentException("The row number is not valid : " + row);
        }
        return rowIndex;
    }

    public boolean isValidIndex(int index) {
        if (index < 0 || index >= rowList.size()) {
            return false;
        }
        return true;
    }

    public int indexOf(Tuple source) {
        TableTuple tuple = getTuple(source);
        if (tuple == null) {
            return -1;
        }
        return indexOf(tuple.getRow());
    }

    public int equivalentIndexOf(Tuple equivalent) {

        Iterator<TableTuple> tuplesIter = table.tuples();
        while (tuplesIter.hasNext()) {
            Tuple it = tuplesIter.next();
            if (DataLib.isEquivalent(it, equivalent)) {
                return indexOf(it.getRow());
            }
        }

        return -1;
    }

    private int addRow(int row) {
        rowList.add(row);
        return rowList.size() - 1;
    }

    private int removeRow(int row) {
        int rowIndex = indexOf(row);
        rowList.remove(rowIndex);
        return rowIndex;
    }
}
