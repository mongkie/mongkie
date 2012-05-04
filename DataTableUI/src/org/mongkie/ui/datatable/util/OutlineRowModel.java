/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.ui.datatable.util;

import org.netbeans.swing.outline.RowModel;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class OutlineRowModel implements RowModel {

    private Table table;

    public OutlineRowModel(Table table) {
        this.table = table;
    }

    public OutlineRowModel() {
        this.table = new Table();
    }

    @Override
    public int getColumnCount() {
        return table.getColumnCount();
    }

    @Override
    public Object getValueFor(Object o, int i) {
        return ((Tuple) o).get(i);
    }

    @Override
    public Class getColumnClass(int i) {
        Class c = table.getColumnType(i);
        if (c == int.class) {
            c = Integer.class;
        } else if (c == long.class) {
            c = Long.class;
        } else if (c == float.class) {
            c = Float.class;
        } else if (c == double.class) {
            c = Double.class;
        } else if (c == boolean.class) {
            c = Boolean.class;
        }
        return c;
    }

    @Override
    public boolean isCellEditable(Object o, int i) {
        return false;
    }

    @Override
    public void setValueFor(Object o, int i, Object o1) {
    }

    @Override
    public String getColumnName(int i) {
        return table.getColumnName(i);
    }
}
