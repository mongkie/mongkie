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
package org.mongkie.layout.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.layout.LayoutModel;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.Layout;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class LayoutModelImpl implements LayoutModel {

    private final Map<String, Layout> layouts;
    private Layout selectedLayout;
    private final List<PropertyChangeListener> listeners;
    private final Map<LayoutPropertyKey, Object> properties;
    private LongTaskExecutor executor;
    private MongkieDisplay d;

    protected LayoutModelImpl(MongkieDisplay d) {
        this.d = d;
        layouts = new HashMap<String, Layout>();
        for (LayoutBuilder builder : Lookup.getDefault().lookupAll(LayoutBuilder.class)) {
            Layout layout = builder.buildLayout();
            layout.resetPropertyValues();
            layout.setDisplay(d);
            layouts.put(builder.getName(), layout);
        }
        listeners = new ArrayList<PropertyChangeListener>();
        properties = new HashMap<LayoutPropertyKey, Object>();
        executor = new LongTaskExecutor(true, "Layout");
        executor.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskFinished(LongTask task) {
                setRunning(false);
            }

            @Override
            public void taskStarted(LongTask task) {
                setRunning(true);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        });
    }

    @Override
    public Layout getSelectedLayout() {
        return selectedLayout;
    }

    void setSelectedLayout(LayoutBuilder builder) {
        Layout oldLayout = selectedLayout;
        selectedLayout = (builder != null) ? layouts.get(builder.getName()) : null;
        /* Layout instance is not a singleton anymore, thus not need to save/load properties */
//        if (oldLayout != null) {
//            saveProperties(oldLayout);
//        }
//        if (selectedLayout != null) {
//            selectedLayout.resetPropertyValues();
//            loadProperties(selectedLayout);
//        }
        firePropertyChangeEvent(SELECTED_LAYOUT, oldLayout, selectedLayout);
    }

    Layout lookupLayout(String name) {
        return layouts.get(name);
    }

    @Override
    public boolean isRunning() {
        return executor.isRunning();
    }

    void setRunning(boolean on) {
        firePropertyChangeEvent(IS_RUNNING, !on, on);
    }

    LongTaskExecutor getExecutor() {
        return executor;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        listeners.remove(l);
    }

    private void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        if (!propertyName.equals(SELECTED_LAYOUT) && !propertyName.equals(IS_RUNNING)) {
            return;
        }
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(e);
        }
    }

    void saveProperties(Layout layout) {
        for (LayoutProperty p : layout.getProperties()) {
            try {
                Object value = p.getValue();
                if (value != null) {
                    properties.put(new LayoutPropertyKey(layout.getBuilder().getName(), p.getName()), value);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    void loadProperties(Layout layout) {
        List<LayoutPropertyKey> propKeys = new ArrayList<LayoutPropertyKey>();
        for (Iterator<LayoutPropertyKey> keyIter = properties.keySet().iterator(); keyIter.hasNext();) {
            LayoutPropertyKey k = keyIter.next();
            if (k.layoutName.equals(layout.getBuilder().getName())) {
                propKeys.add(k);
            }
        }
        for (LayoutProperty property : layout.getProperties()) {
            for (LayoutPropertyKey k : propKeys) {
                if (property.getName().equals(k.propertyName)) {
                    try {
                        property.setValue(properties.get(k));
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            }
        }
    }

    @Override
    public MongkieDisplay getDisplay() {
        return d;
    }

    private static class LayoutPropertyKey {

        private volatile int hash = 0;
        private final String layoutName;
        private final String propertyName;

        public LayoutPropertyKey(String layoutName, String propertyName) {
            this.layoutName = layoutName;
            this.propertyName = propertyName;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LayoutPropertyKey)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            LayoutPropertyKey key = (LayoutPropertyKey) obj;
            if (key.layoutName.equals(layoutName) && key.propertyName.equals(propertyName)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                int h = 7;
                h += 53 * layoutName.hashCode();
                h += 53 * propertyName.hashCode();
                hash = h;
            }
            return hash;
        }
    }
}
