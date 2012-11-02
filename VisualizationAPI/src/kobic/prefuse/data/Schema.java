/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class Schema {

    private String[] names;
    private Class[] types;
    private Object[] defaults;
    private Map lookup;
    private int size;
    private String keyField, labelField;

    public Schema(String[] names, Class[] types, Object[] defaults) {
        // check the schema validity
        if (names.length != types.length
                || types.length != defaults.length) {
            throw new IllegalArgumentException(
                    "Input arrays should be the same length");
        }
        this.names = new String[names.length];
        this.types = new Class[types.length];
        this.defaults = new Object[defaults.length];

        for (int i = 0; i < names.length; ++i) {
            addColumn(names[i], types[i], defaults[i]);
        }
    }

    protected void initLookup() {
        lookup = new HashMap();
        for (int i = 0; i < names.length; ++i) {
            lookup.put(names[i], new Integer(i));
        }
    }

    public Schema addColumn(String name, Class type, Object defaultValue) {
        // check for validity
        if (name == null) {
            throw new IllegalArgumentException(
                    "Null column names are not allowed.");
        }
        if (type == null) {
            throw new IllegalArgumentException(
                    "Null column types are not allowed.");
        }
        for (int i = 0; i < size; ++i) {
            if (names[i].equals(name)) {
                throw new IllegalArgumentException(
                        "Duplicate column names are not allowed: " + names[i]);
            }
        }

        // resize if necessary
        // TODO put resizing functionality into library routines?
        if (names.length == size) {
            int capacity = (3 * names.length) / 2 + 1;
            String[] _names_ = new String[capacity];
            Class[] _types_ = new Class[capacity];
            Object[] _defaults_ = new Object[capacity];
            System.arraycopy(names, 0, _names_, 0, size);
            System.arraycopy(types, 0, _types_, 0, size);
            System.arraycopy(defaults, 0, _defaults_, 0, size);
            names = _names_;
            types = _types_;
            defaults = _defaults_;
        }

        names[size] = name;
        types[size] = type;
        defaults[size] = defaultValue;

        if (lookup != null) {
            lookup.put(name, new Integer(size));
        }

        ++size;
        return this;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        if (getColumnIndex(keyField) < 0) {
            throw new IllegalArgumentException(
                    "Field is not found: " + keyField);
        }
        this.keyField = keyField;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        if (getColumnIndex(labelField) < 0) {
            throw new IllegalArgumentException(
                    "Field is not found: " + labelField);
        }
        this.labelField = labelField;
    }

    public String[] getColumnNames() {
        return Arrays.copyOf(names, size);
    }

    /**
     * Get the number of columns in this schema.
     *
     * @return the number of columns
     */
    public int getColumnCount() {
        return size;
    }

    /**
     * The name of the column at the given position.
     *
     * @param col the column index
     * @return the column name
     */
    public String getColumnName(int col) {
        return names[col];
    }

    /**
     * The column index for the column with the given name.
     *
     * @param field the column name
     * @return the column index
     */
    public int getColumnIndex(String field) {
        if (lookup == null) {
            initLookup();
        }

        Integer idx = (Integer) lookup.get(field);
        return (idx == null ? -1 : idx.intValue());
    }

    /**
     * The type of the column at the given position.
     *
     * @param col the column index
     * @return the column type
     */
    public Class getColumnType(int col) {
        return types[col];
    }

    /**
     * The type of the column with the given name.
     *
     * @param field the column name
     * @return the column type
     */
    public Class getColumnType(String field) {
        int idx = getColumnIndex(field);
        return (idx < 0 ? null : types[idx]);
    }

    /**
     * The default value of the column at the given position.
     *
     * @param col the column index
     * @return the column's default value
     */
    public Object getDefault(int col) {
        return defaults[col];
    }

    /**
     * The default value of the column with the given name.
     *
     * @param field the column name
     * @return the column's default value
     */
    public Object getDefault(String field) {
        int idx = getColumnIndex(field);
        return (idx < 0 ? null : defaults[idx]);
    }

    /**
     * Set the default value for the given field.
     *
     * @param col the column index of the field to set the default for
     * @param val the new default value
     */
    public void setDefault(int col, Object val) {
        defaults[col] = val;
    }

    /**
     * Set the default value for the given field.
     *
     * @param field the name of column to set the default for
     * @param val the new default value
     */
    public void setDefault(String field, Object val) {
        int idx = getColumnIndex(field);
        defaults[idx] = val;
    }

    /**
     * Set the default value for the given field as an int.
     *
     * @param field the name of column to set the default for
     * @param val the new default value
     */
    public void setDefault(String field, int val) {
        setDefault(field, new Integer(val));
    }

    /**
     * Set the default value for the given field as a long.
     *
     * @param field the name of column to set the default for
     * @param val the new default value
     */
    public void setDefault(String field, long val) {
        setDefault(field, new Long(val));
    }

    /**
     * Set the default value for the given field as a float.
     *
     * @param field the name of column to set the default for
     * @param val the new default value
     */
    public void setDefault(String field, float val) {
        setDefault(field, new Float(val));
    }

    /**
     * Set the default value for the given field as a double.
     *
     * @param field the name of column to set the default for
     * @param val the new default value
     */
    public void setDefault(String field, double val) {
        setDefault(field, new Double(val));
    }

    /**
     * Set the default value for the given field as a boolean.
     *
     * @param field the name of column to set the default for
     * @param val the new default value
     */
    public void setDefault(String field, boolean val) {
        setDefault(field, val ? Boolean.TRUE : Boolean.FALSE);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Schema)) {
            return false;
        }

        Schema s = (Schema) o;
        if (size != s.getColumnCount()) {
            return false;
        }

        for (int i = 0; i < size; ++i) {
            if (!(names[i].equals(s.getColumnName(i))
                    && types[i].equals(s.getColumnType(i))
                    && defaults[i].equals(s.getDefault(i)))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashcode = 0;
        for (int i = 0; i < size; ++i) {
            int idx = i + 1;
            int code = idx * names[i].hashCode();
            code ^= idx * types[i].hashCode();
            if (defaults[i] != null) {
                code ^= defaults[i].hashCode();
            }
            hashcode ^= code;
        }
        return hashcode;
    }

    /**
     * Returns a descriptive String for this schema.
     */
    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Schema[");
        for (int i = 0; i < size; ++i) {
            if (i > 0) {
                sbuf.append(' ');
            }
            sbuf.append('(').append(names[i]).append(", ");
            sbuf.append(types[i].getName()).append(", ");
            sbuf.append(defaults[i]).append(')');
        }
        sbuf.append(']');
        return sbuf.toString();
    }
}
