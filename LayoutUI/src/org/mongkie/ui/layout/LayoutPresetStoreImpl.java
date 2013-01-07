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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.Layout;
import org.mongkie.ui.layout.LayoutPresetStoreImpl.Preset;
import org.mongkie.util.Persistence;
import org.openide.util.Exceptions;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class LayoutPresetStoreImpl extends Persistence.KeyValues<Layout, Preset> {

    public static LayoutPresetStoreImpl getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final LayoutPresetStoreImpl INSTANCE = new LayoutPresetStoreImpl();
    }

    private LayoutPresetStoreImpl() {
    }

    @Override
    protected String getKeyName(Layout l) {
        return l.getBuilder().getName();
    }

    @Override
    protected Preset valueOf(Layout l, String name) {
        try {
            return new Preset(l, name);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    protected String getRootName() {
        return "LayoutPresets";
    }

    @Override
    public void load(Layout l, Preset preset) {
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

    public static class Preset implements Persistence.Value, Serializable {

        private final String name;
        private final Map<String, Object> properties = new HashMap<String, Object>();

        private Preset(Layout l, String name)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            this.name = name;
            for (LayoutProperty p : l.getProperties()) {
                properties.put(p.getName(), p.getValue());
            }
        }

        @Override
        public String getName() {
            return name;
        }

        public Object get(String property) {
            return properties.get(property);
        }

        public boolean contains(String property) {
            return properties.containsKey(property);
        }
    }
}
