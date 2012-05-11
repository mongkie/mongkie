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
package kobic.prefuse.display;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.util.StringLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DataEditSupport {

    public static final String PROP_KEY = "supportEditingDataTableInDisplay";
    private final Table table;

    public DataEditSupport(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public abstract boolean isEditable(String field);

    public abstract boolean isAddColumnSupported();

    public abstract boolean isRemoveColumnSupported();

    public abstract boolean isAddDataSupported();

    public abstract boolean isRemoveDataSupported();

    public Object getValueAt(Tuple data, String field) {
        Object val = data.get(field);
        if (val != null && data.getTable().getMetadata(field).hasMultipleValues()) {
            val = ((String) val).split(Column.MULTI_VAL_SEPARATOR);
        }
        return val;
    }

    public <T> T setValueAt(Tuple data, String field, T val) {
        T old = (T) data.get(field);
        if (val != null && data.getTable().getMetadata(field).hasMultipleValues()) {
            data.set(field, StringLib.concatStringArray((String[]) val, Column.MULTI_VAL_SEPARATOR));
        } else {
            data.set(field, val);
        }
        return old;
    }

    public Class getColumnType(String field) {
        return getTable().getMetadata(field).hasMultipleValues() ? String[].class : getTable().getColumnType(field);
    }
}
