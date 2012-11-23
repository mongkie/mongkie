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
package org.mongkie.ui.layout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.mongkie.layout.LayoutController;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.Layout;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class LayoutPersistenceImpl {

    private final Map<Layout, List<Preset>> presets = new HashMap<Layout, List<Preset>>();

    private LayoutPersistenceImpl() {
        Preferences root = getPresetsPreferences();
        try {
            for (String layoutName : root.childrenNames()) {
                Layout l = Lookup.getDefault().lookup(LayoutController.class).lookupLayout(layoutName);
                if (l != null) {
                    List<Preset> layoutPresets = new ArrayList<Preset>();
                    Preferences pref = root.node(layoutName);
                    for (String presetName : pref.keys()) {
                        byte[] bytes = pref.getByteArray(presetName, null);
                        if (bytes != null) {
                            try {
                                layoutPresets.add(toObject(bytes));
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (ClassNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                    if (!layoutPresets.isEmpty()) {
                        presets.put(l, layoutPresets);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static LayoutPersistenceImpl getDefault() {
        return DEFAULT.INSTANCE;
    }

    private static class DEFAULT {

        private static final LayoutPersistenceImpl INSTANCE = new LayoutPersistenceImpl();
    }

    public List<Preset> getPresets(Layout l) {
        return presets.get(l);
    }

    public void loadPreset(Layout l, Preset preset) {
        for (LayoutProperty p : l.getProperties()) {
            Object val = preset.get(p.getName());
            try {
                if (val != null && !val.equals(p.getValue())) {
                    p.setValue(val);
                }
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public boolean savePreset(Layout l, String name) {
        try {
            getPresetsPreferences(l).putByteArray(name, toByteArray(addPreset(l, name)));
            return true;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    private byte[] toByteArray(Preset p) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(p);
        return bos.toByteArray();
    }

    private Preset toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        return (Preset) in.readObject();
    }

    private Preset addPreset(Layout l, String name)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<Preset> layoutPresets = presets.get(l);
        if (layoutPresets == null) {
            layoutPresets = new ArrayList<Preset>();
            presets.put(l, layoutPresets);
        }
        Preset preset = new Preset(l, name);
        if (layoutPresets.contains(preset)) {
            layoutPresets.remove(preset);
        }
        layoutPresets.add(preset);
        return preset;
    }

    private Preferences getPresetsPreferences(Layout l) {
        return getPresetsPreferences().node(l.getBuilder().getName());
    }

    private Preferences getPresetsPreferences() {
        return NbPreferences.forModule(LayoutPersistenceImpl.class).node("LayoutPresets");
    }

    public static class Preset implements Serializable {

        private final String name;
        private final Map<String, Object> properties = new HashMap<String, Object>();

        private Preset(Layout l, String name)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.name = name;
            for (LayoutProperty p : l.getProperties()) {
                properties.put(p.getName(), p.getValue());
            }
        }

        public String getName() {
            return name;
        }

        public Object get(String property) {
            return properties.get(property);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Preset other = (Preset) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }
    }
}
