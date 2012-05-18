/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.datatable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import kobic.prefuse.display.DataEditSupport;
import kobic.prefuse.display.DataViewSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DataNode extends AbstractNode implements TableListener {

    public DataNode(Tuple data, String labelColumn) {
        super(Children.LEAF, Lookups.singleton(data));
        String label = labelColumn != null ? data.getString(labelColumn) : null;
        setName(String.valueOf(data.getRow()));
        setDisplayName(label == null ? "" : label);
        data.getTable().addTableListener(DataNode.this);
    }

    public Tuple getTuple() {
        return getLookup().lookup(Tuple.class);
    }

    @Override
    public final PropertySet[] getPropertySets() {
        return new PropertySet[]{preparePropertySet(getTuple())};
    }

    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws IOException {
        getTuple().getTable().removeTableListener(this);
        super.destroy();
    }

    @Override
    public void tableChanged(Table t, int start, int end, int col, int type) {
        Tuple data = getTuple();
        Schema s = getPropertySchema(data);
        switch (type) {
            case EventConstants.INSERT:
            case EventConstants.DELETE:
                // column added or deleted
                if (col != EventConstants.ALL_COLUMNS) {
                    firePropertySetsChange(null, getPropertySets());
                }
                break;
            case EventConstants.UPDATE:
                for (int row = start; row <= end; row++) {
                    if (row == data.getRow()) {
                        if (col == EventConstants.ALL_COLUMNS) {
                            for (int i = 0; i < s.getColumnCount(); i++) {
                                firePropertyChange(s.getColumnName(i), null, data.get(s.getColumnName(i)));
                            }
//                            firePropertySetsChange(null, getPropertySets());
                        } else if (s.getColumnIndex(t.getColumnName(col)) > -1) {
                            firePropertyChange(t.getColumnName(col), null, data.get(col));
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    protected abstract Sheet.Set preparePropertySet(Tuple data);

    protected abstract Schema getPropertySchema(Tuple data);

    public static class Property<T> extends PropertySupport.ReadWrite<T> {

        private final Tuple data;
        private final String field;

        public Property(Tuple data, String field, Class<T> type) {
            super(field, type, null, type.getName());
            this.data = data;
            this.field = field;
            setName(field);
            String strVal = ((DataViewSupport) data.getTable().getClientProperty(DataViewSupport.PROP_KEY)).getStringAt(data, field);
            setShortDescription(strVal == null ? "" : strVal);
        }

        public Property(Tuple data, String field, String displayName, Class<T> type) {
            this(data, field, type);
            setDisplayName(displayName);
        }

        public Tuple getTuple() {
            return data;
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
//            setValue("htmlDisplayValue", "<html>" + getShortDescription() + "</html>");
            return (T) ((DataEditSupport) data.getTable().getClientProperty(DataEditSupport.PROP_KEY)).getValueAt(data, field);
        }

        @Override
        public void setValue(T val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            ((DataEditSupport) data.getTable().getClientProperty(DataEditSupport.PROP_KEY)).setValueAt(data, field, val);
        }

        @Override
        public boolean canWrite() {
            return ((DataEditSupport) data.getTable().getClientProperty(DataEditSupport.PROP_KEY)).isEditable(field);
        }
    }
}
