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
package kobic.prefuse.data.io;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SerializableTable implements Serializable {

    private transient Table base, table;
    private transient Map<Integer, Integer> rowMap;

    public SerializableTable(Table table) {
        this(table, (Schema) table.getSchema().clone());
    }

    public SerializableTable(VisualTable table) {
        this(table, VisualItem.SCHEMA);
    }

    public SerializableTable(AggregateTable table) {
        this(table, PrefuseLib.getAggregateItemSchema().addColumn(
                AggregateItem.AGGR_ID, int.class).addColumn(
                AggregateItem.AGGR_NAME, String.class));
    }

    public SerializableTable(Table base, Schema s) {
        if (!base.getSchema().isAssignableFrom(s)) {
            throw new IllegalArgumentException("The given schema must be assignable from the base table");
        }
        this.base = base;
        this.table = s.instantiate();
        rowMap = new HashMap<Integer, Integer>();
        Iterator<Tuple> originals = base.tuples();
        while (originals.hasNext()) {
            Tuple original = originals.next();
            int row = this.table.addRow();
            rowMap.put(original.getRow(), row);
            for (int i = 0; i < s.getColumnCount(); i++) {
                String field = s.getColumnName(i);
                this.table.set(row, field, original.get(field));
            }
        }
    }

    public Table getTable() {
        return table;
    }

    public Table getBaseTable() {
        return base;
    }

    public int getRow(int originalRow) {
        return rowMap.get(originalRow);
    }

    /**
     * Write a table to the ObjectOutputStream out
     *
     * @param out
     * @throws IOException
     * @serialData
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        int ccount = table.getColumnCount();
        out.writeInt(ccount);
        for (int col = 0; col < ccount; col++) {
            String cname = table.getColumnName(col);
            Class ctype = table.getColumnType(col);
            out.writeObject(cname);
            out.writeObject(ctype);
            out.writeObject(marshall(ctype, table.getDefault(cname)));
        }
        out.writeObject(rowMap);
        out.writeInt(table.getTupleCount());
        Iterator<Tuple> tuples = table.tuples();
        while (tuples.hasNext()) {
            Tuple tuple = tuples.next();
            for (int col = 0; col < ccount; col++) {
                out.writeObject(marshall(table.getColumnType(col), tuple.get(col)));
            }
        }
    }

    /**
     * Read a table from the ObjectInputStream in
     *
     * @param in
     * @throws ClassNotFoundException IOException
     * @serialData
     */
    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        int ccount = in.readInt();
        table = new Table();
        for (int col = 0; col < ccount; col++) {
            Class ctype;
            table.addColumn((String) in.readObject(), ctype = (Class) in.readObject(), unmarshall(ctype, in.readObject()));
        }
        rowMap = (Map<Integer, Integer>) in.readObject();
        int tupleCount = in.readInt();
        while ((tupleCount--) > 0) {
            int row = table.addRow();
            for (int col = 0; col < ccount; col++) {
                table.set(row, col, unmarshall(table.getColumnType(col), in.readObject()));
            }
        }
    }

    private Object marshall(Class type, Object unserializable) {
        if (type == Stroke.class) {
            return new SerializableBasicStroke((BasicStroke) unserializable);
        }
        return unserializable;
    }

    private Object unmarshall(Class type, Object serializable) {
        if (type == Stroke.class) {
            return ((SerializableBasicStroke) serializable).getStroke();
        } else if (type == Font.class) {
            Font f = (Font) serializable;
            return FontLib.getFont(f.getName(), f.getStyle(), f.getSize());
        }
        return serializable;
    }
}
