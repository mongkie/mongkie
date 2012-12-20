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
package org.mongkie.datatable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import prefuse.data.column.Column;
import prefuse.util.StringLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum ColumnType {

    STRING(String.class, "String", "",
    new ValueParser<String>() {
        @Override
        public String parse(String text) {
            return text.isEmpty() ? null : text;
        }
    }),
    BOOLEAN(boolean.class, "Boolean", true,
    new ValueParser<Boolean>() {
        @Override
        public Boolean parse(String text) {
            return Boolean.valueOf(text);
        }
    }),
    INTEGER(int.class, "Integer", -1,
    new ValueParser<Integer>() {
        @Override
        public Integer parse(String text) {
            try {
                return Integer.valueOf(text);
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
    }),
    DOUBLE(double.class, "Double", 0.0D,
    new ValueParser<Double>() {
        @Override
        public Double parse(String text) {
            try {
                return Double.valueOf(text);
            } catch (NumberFormatException ex) {
                return 0.0D;
            }
        }
    }),
    STRING_ARRAY(String.class, "StringArray", "",
    new ValueParser<String>() {
        @Override
        public String parse(String text) {
            String[] array = text.split(",\\s*");
            String val = StringLib.concatStringArray(array, Column.MULTI_VAL_SEPARATOR);
            System.out.println("> " + text);
            System.out.println(">> " + Arrays.toString(array));
            System.out.println(">>> " + val);
            return val;
        }
    });
    private final String name;
    private final Class type;
    private final Object defaultValue;
    private final ValueParser parser;
    private static final Map<Class, ColumnType> types = new HashMap<Class, ColumnType>();

    static {
        for (ColumnType type : values()) {
            if (type != STRING_ARRAY) {
                types.put(type.getType(), type);
            }
        }
    }

    private ColumnType(Class type, String name, Object defaultValue, ValueParser parser) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Object getValue(String text) {
        return text != null && !text.isEmpty() ? parser.parse(text) : null;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static ColumnType valueOf(Class type) {
        return types.get(type);
    }

    private static interface ValueParser<T> {

        public T parse(String text);
    }
}
