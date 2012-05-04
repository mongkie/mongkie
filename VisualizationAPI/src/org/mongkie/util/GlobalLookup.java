/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.util;

import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GlobalLookup extends AbstractLookup {

    private final InstanceContent content;

    public GlobalLookup() {
        this(new InstanceContent());
    }

    public GlobalLookup(InstanceContent content) {
        super(content);
        this.content = content;
    }

    public void add(Object instance) {
        content.add(instance);
    }

    public void remove(Object instance) {
        content.remove(instance);
    }

    public <T> void clear(Class<T> clazz) {
        for (T instance : lookupAll(clazz)) {
            content.remove(instance);
        }
    }

    public <T> void set(Class<T> clazz, T... instances) {
        clear(clazz);
        for (T instance : instances) {
            content.add(instance);
        }
    }

    public <T> void touch(Class<T> clazz) {
        for (T instance : lookupAll(clazz)) {
            content.remove(instance);
            content.add(instance);
        }
    }

    public <T> boolean has(Class<T> clazz) {
        return !lookupAll(clazz).isEmpty();
    }

    public static GlobalLookup getDefault() {
        return Holder.DEFAULT;
    }

    private static class Holder {

        private static final GlobalLookup DEFAULT = new GlobalLookup();
    }
}
