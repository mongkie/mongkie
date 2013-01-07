/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mongkie.util.Persistence.Value;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class Persistence<V> {

    protected final Preferences getRootPreferences() {
        return NbPreferences.forModule(getClass()).node(getRootName());
    }

    protected final Preferences getChildPreferences(String name) {
        return getRootPreferences().node(name);
    }

    protected final byte[] marshall(V v) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(v);
        return bos.toByteArray();
    }

    protected final V unmarshall(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (V) in.readObject();
    }

    protected abstract String getRootName();

    public interface Value {

        public String getName();
    }

    public static abstract class Values<V extends Value> extends Persistence<V> {

        private Set<V> values = new LinkedHashSet<V>();

        protected Values() {
            Preferences root = getRootPreferences();
            try {
                for (String name : root.childrenNames()) {
                    V value = Values.this.load(root.node(name));
                    if (value != null) {
                        values.add(value);
                    } else {
                        continue;
                    }
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public final Set<V> getValues() {
            return values;
        }

        protected abstract V load(Preferences node);

        public final void save(V value) {
            store(getChildPreferences(value.getName()), value);
        }

        protected abstract void store(Preferences node, V value);
    }

    public static abstract class KeyValues<K, V extends Value> extends Persistence<V> {

        private final Map<String, Set<V>> store = new HashMap<String, Set<V>>();

        protected KeyValues() {
            Preferences root = getRootPreferences();
            try {
                for (String key : root.childrenNames()) {
                    Set<V> values = new LinkedHashSet<V>();
                    Preferences pref = root.node(key);
                    for (String name : pref.keys()) {
                        byte[] bytes = pref.getByteArray(name, null);
                        if (bytes != null) {
                            try {
                                values.add(unmarshall(bytes));
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ClassNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                    if (!values.isEmpty()) {
                        store.put(key, values);
                    }
                }
            } catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        public abstract void load(K key, V value);

        public final boolean save(K key, String name) {
            try {
                V value = addValue(key, name);
                if (value != null) {
                    getChildPreferences(getKeyName(key)).putByteArray(name, marshall(value));
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            return false;
        }

        private V addValue(K key, String name) {
            String keyName = getKeyName(key);
            Set<V> values = store.get(keyName);
            if (values == null) {
                values = new LinkedHashSet<V>();
                store.put(keyName, values);
            }
            V v = valueOf(key, name);
            if (v != null) {
                // Remove the existing value with same name
                for (Iterator<V> valueIter = values.iterator(); valueIter.hasNext();) {
                    if (name.equals(valueIter.next().getName())) {
                        valueIter.remove();
                    }
                }
                values.add(v);
            }
            return v;
        }

        protected abstract String getKeyName(K key);

        protected abstract V valueOf(K key, String name);

        public Set<V> getValues(K key) {
            return store.get(getKeyName(key));
        }
    }
}
