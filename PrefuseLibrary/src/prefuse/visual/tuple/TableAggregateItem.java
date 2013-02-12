/**
 * Copyright (c) 2004-2006 Regents of the University of California.
 * See "license-prefuse.txt" for licensing terms.
 */
package prefuse.visual.tuple;

import java.util.Collection;
import java.util.Iterator;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.util.FilterIterator;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

/**
 * AggregateItem implementation that uses data values from a backing
 * AggregateTable.
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class TableAggregateItem extends TableVisualItem
        implements AggregateItem {

    /**
     * Initialize a new TableAggregateItem for the given table and row. This
     * method is used by the appropriate TupleManager instance, and should not
     * be called directly by client code, unless by a client-supplied custom
     * TupleManager.
     *
     * @param table the data Table
     * @param graph ignored by this class
     * @param row   the table row index
     */
    @Override
    protected void init(Table table, Graph graph, int row) {
        m_table = table;
        m_row = m_table.isValidRow(row) ? row : -1;
    }

    /**
     * @see prefuse.visual.AggregateItem#getAggregateSize()
     */
    @Override
    public int getAggregateSize() {
        return ((AggregateTable) m_table).getAggregateSize(m_row);
    }

    /**
     * @see prefuse.visual.AggregateItem#containsItem(prefuse.visual.VisualItem)
     */
    @Override
    public boolean containsItem(VisualItem item) {
        return ((AggregateTable) m_table).aggregateContains(m_row, item);
    }

    /**
     * @see prefuse.visual.AggregateItem#addItem(prefuse.visual.VisualItem)
     */
    @Override
    public void addItem(VisualItem item) {
        ((AggregateTable) m_table).addToAggregate(m_row, item);
    }

    @Override
    public void addItems(Iterator<VisualItem> items) {
        ((AggregateTable) m_table).addToAggregate(m_row, items);
    }

    @Override
    public void addItems(Collection<? extends Tuple> tuples, String group) {
        ((AggregateTable) m_table).addToAggregate(m_row, tuples, group);
    }

    /**
     * @see prefuse.visual.AggregateItem#removeItem(prefuse.visual.VisualItem)
     */
    @Override
    public void removeItem(VisualItem item) {
        ((AggregateTable) m_table).removeFromAggregate(m_row, item);
    }

    /**
     * @see prefuse.visual.AggregateItem#removeAllItems()
     */
    @Override
    public void removeAllItems() {
        ((AggregateTable) m_table).removeAllFromAggregate(m_row);
    }

    /**
     * @see prefuse.visual.AggregateItem#items()
     */
    @Override
    public Iterator items() {
        return ((AggregateTable) m_table).aggregatedTuples(m_row);
    }

    /**
     * @see prefuse.visual.AggregateItem#items()
     */
    @Override
    public Iterator items(Predicate filter) {
        return new FilterIterator(
                ((AggregateTable) m_table).aggregatedTuples(m_row), filter);
    }

    @Override
    public VisualItem[] toArray() {
        VisualItem[] itemsArray = new VisualItem[getAggregateSize()];
        Iterator<VisualItem> itemsIter = items();
        for (int i = 0; itemsIter.hasNext(); i++) {
            itemsArray[i] = itemsIter.next();
        }
        return itemsArray;
    }

    @Override
    public int compareTo(AggregateItem o) {
        return this.getAggregateSize() - o.getAggregateSize();
    }

    @Override
    public String toString() {
        return canGetString(AGGR_NAME) ? getString(AGGR_NAME) : super.toString();
    }
} // end of class TableAggregateItem

