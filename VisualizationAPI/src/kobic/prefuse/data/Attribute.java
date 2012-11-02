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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class Attribute<T> {

    private final String name;
    private final T value;

    public Attribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public Class<T> getType() {
        return (Class<T>) value.getClass();
    }

    public static class Set implements Iterable<Attribute> {

        private List<Attribute> attributes = new ArrayList<Attribute>();
        private Map<String, Attribute> lookup = new HashMap<String, Attribute>();

        public boolean add(Attribute a) {
            if (attributes.add(a)) {
                lookup.put(a.getName(), a);
                return true;
            } else {
                return false;
            }
        }

        public void clear() {
            lookup.clear();
            attributes.clear();
        }

        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        public List<Attribute> getList() {
            return Collections.unmodifiableList(attributes);
        }

        public Object getValue(String name) {
            Attribute a = getAttribute(name);
            return a != null ? a.getValue() : null;
        }

        public Attribute getAttribute(String name) {
            return lookup.get(name);
        }

        @Override
        public Iterator<Attribute> iterator() {
            return attributes.listIterator();
        }
    }
}
