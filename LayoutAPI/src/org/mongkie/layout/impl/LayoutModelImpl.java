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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.layout.LayoutModel;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.Layout;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Exceptions;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class LayoutModelImpl implements LayoutModel {

    private Layout selectedLayout;
    private final List<PropertyChangeListener> listeners;
    private final Map<LayoutPropertyKey, Object> properties;
    private LongTaskExecutor executor;
    private MongkieDisplay d;

    protected LayoutModelImpl(MongkieDisplay d) {
        this.d = d;
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

    protected void setSelectedLayout(Layout layout) {
        Layout oldLayout = selectedLayout;
        selectedLayout = layout;
        if (oldLayout != null) {
            saveProperties(oldLayout);
        }
        if (selectedLayout != null) {
            loadProperties(selectedLayout);
        }
        firePropertyChangeEvent(SELECTED_LAYOUT, oldLayout, selectedLayout);
    }

    @Override
    public boolean isRunning() {
        return executor.isRunning();
    }

    protected void setRunning(boolean on) {
        firePropertyChangeEvent(IS_RUNNING, !on, on);
    }

    protected LongTaskExecutor getExecutor() {
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

    protected void saveProperties(Layout layout) {
        for (LayoutProperty p : layout.getProperties()) {
            try {
                Object value = p.getValue();
                if (value != null) {
                    properties.put(new LayoutPropertyKey(p.getName(), layout.getClass().getName()), value);
                }
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadProperties(Layout layout) {
        List<LayoutPropertyKey> layoutValues = new ArrayList<LayoutPropertyKey>();
        for (LayoutPropertyKey val : properties.keySet()) {
            if (val.className.equals(layout.getClass().getName())) {
                layoutValues.add(val);
            }
        }
        for (LayoutProperty property : layout.getProperties()) {
            for (LayoutPropertyKey l : layoutValues) {
                if (property.getName().equals(l.propertyName)) {
                    try {
                        property.setValue(properties.get(l));
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
        private final String propertyName;
        private final String className;

        public LayoutPropertyKey(String propertyName, String className) {
            this.propertyName = propertyName;
            this.className = className;
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
            if (key.className.equals(className) && key.propertyName.equals(propertyName)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                int h = 7;
                h += 53 * className.hashCode();
                h += 53 * propertyName.hashCode();
                hash = h;
            }
            return hash;
        }
    }
}
