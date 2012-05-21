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

import java.util.Iterator;
import static kobic.prefuse.Constants.EDGES;
import static kobic.prefuse.Constants.PROPKEY_DATAGROUP;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.Predicate;
import prefuse.util.StringLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DataViewSupport {

    public static final String PROP_KEY = "supportViewingDataTableInDisplay";
    private final Table table;
    private final AndPredicate filters;

    public DataViewSupport(Table table) {
        this(table, null);
    }

    public DataViewSupport(final Table table, Predicate filter) {
        this.table = table;
        String dataGroup = (String) table.getClientProperty(PROPKEY_DATAGROUP);
        if (EDGES.equals(dataGroup)) {
            filters = new AndPredicate(new AbstractPredicate() {

                private final NetworkDisplay display = (NetworkDisplay) table.getClientProperty(NetworkDisplay.PROP_KEY);

                @Override
                public boolean getBoolean(Tuple t) {
                    return !display.getVisualization().getVisualItem(EDGES, t).isAggregating();
                }
            });
        } else {
            filters = new AndPredicate() {

                @Override
                public boolean getBoolean(Tuple t) {
                    return size() > 0 ? super.getBoolean(t) : true;
                }
            };
        }
        if (filter != null) {
            filters.add(filter);
        }
    }

    public Predicate getFilter() {
        return filters;
    }

    public void addFilter(Predicate filter) {
        filters.add(filter);
    }

    public boolean removeFilter(Predicate filter) {
        return filters.remove(filter);
    }

    public final Iterator<Tuple> tuples() {
        return filters.size() > 0 ? table.tuples(filters) : table.tuples();
    }

    public abstract Schema getOutlineSchema();

    public Table getTable() {
        return table;
    }

    public Schema getPropertySchema() {
        return getOutlineSchema();
    }

    public Schema getTooltipSchema() {
        return getOutlineSchema();
    }

    public String getColumnTitle(String field) {
        return field;
    }

    public String getStringAt(Tuple data, String field) {
        if (data.getTable().getMetadata(field).hasMultipleValues()) {
            Object val = data.get(field);
            return val != null ? StringLib.concatStringArray(((String) val).split(Column.MULTI_VAL_SEPARATOR), ", ") : "";
        }
        String string = null;
        return data.canGetString(field) && (string = data.getString(field)) != null ? string : "";
    }
}
