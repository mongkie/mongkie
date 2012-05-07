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

import java.lang.reflect.InvocationTargetException;
import kobic.prefuse.display.DataEditSupport;
import kobic.prefuse.display.DataViewSupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DataNode extends AbstractNode {

    public DataNode(Tuple data, String labelColumn) {
        super(Children.LEAF, Lookups.singleton(data));
        String label = labelColumn != null ? data.getString(labelColumn) : null;
        setName(String.valueOf(data.getRow()));
        setDisplayName(label == null ? "" : label);
    }

    public Tuple getTuple() {
        return getLookup().lookup(Tuple.class);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        sheet.put(preparePropertySet(getTuple()));
        return sheet;
    }

    protected abstract Sheet.Set preparePropertySet(Tuple data);

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
