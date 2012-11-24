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
package org.mongkie.layout.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Exceptions;
import prefuse.action.Action;
import prefuse.activity.Activity;
import prefuse.activity.ActivityListener;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PrefuseLayout extends prefuse.action.layout.Layout
        implements Layout, ActivityListener, DisplayListener<MongkieDisplay> {

    protected MongkieDisplay display;
    protected LayoutProperty[] _properties;
    private volatile boolean completed;
    private final LayoutBuilder<? extends Layout> builder;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected PrefuseLayout(LayoutBuilder<? extends Layout> builder) {
        this.builder = builder;
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected final void firePropertyChange(String name, Object o, Object n) {
        pcs.firePropertyChange(name, o, n);
    }

    @Override
    public final LayoutBuilder<? extends Layout> getBuilder() {
        return builder;
    }

    @Override
    public void setDisplay(MongkieDisplay d) {
        if (display == d) {
            return;
        }
        if (display != null) {
            display.getLayoutAction().removeActivityListener(this);
            display.removeDisplayListener(this);
        }
        d.getLayoutAction().addActivityListener(this);
        d.addDisplayListener(this);
        display = d;
        setVisualization(d.getVisualization());
    }

    @Override
    public LayoutProperty[] getProperties() {
        if (_properties == null) {
            _properties = createProperties();
        }
        return _properties;
    }

    protected abstract LayoutProperty[] createProperties();

    @Override
    public void resetPropertyValues() {
        resetProperties();
        firePropertyChange("resetPropertyValues", null, this);
    }

    protected abstract void resetProperties();

    protected final boolean isCompleted() {
        return completed;
    }

    protected final void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public void initAlgo() {
        display.setLayout(this, isRunOnce() ? 0 : Action.INFINITY);
        setCompleted(false);
    }

    protected abstract boolean isRunOnce();

    @Override
    public final void goAlgo() {
        display.rerunNetworkLayout();
        synchronized (this) {
            if (!isCompleted()) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public final boolean hasNextStep() {
        return !isCompleted() && display != null;
    }

    @Override
    public void cancelAlgo() {
        display.cancelLayout();
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public void activityScheduled(Activity a) {
    }

    @Override
    public void activityStarted(Activity a) {
    }

    @Override
    public void activityStepped(Activity a) {
    }

    @Override
    public void activityFinished(Activity a) {
        completeLayout();
    }

    @Override
    public void activityCancelled(Activity a) {
        completeLayout();
    }

    private void completeLayout() {
        if (!isCompleted()) {
            synchronized (this) {
                setCompleted(true);
                notifyAll();
            }
        }
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        d.getLayoutAction().removeActivityListener(this);
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        d.getLayoutAction().addActivityListener(this);
    }

    public abstract static class Delegation<L extends prefuse.action.layout.Layout>
            extends PrefuseLayout implements PropertyChangeListener {

        private L deligate;

        protected Delegation(LayoutBuilder<? extends Delegation> builder) {
            super(builder);
        }

        @Override
        public final void setDisplay(MongkieDisplay d) {
            super.setDisplay(d);
            getDeligateLayout().setVisualization(d.getVisualization());
        }

        @Override
        public final void propertyChange(PropertyChangeEvent evt) {
            setLayoutParameters(getDeligateLayout());
        }

        @Override
        public final LayoutProperty[] getProperties() {
            if (_properties == null) {
                _properties = createProperties();
                if (!isRunOnce()) {
                    for (LayoutProperty p : _properties) {
                        p.getPropertyEditor().addPropertyChangeListener(this);
                    }
                }
            }
            return _properties;
        }

        @Override
        public final void resetPropertyValues() {
            super.resetPropertyValues();
            if (!isRunOnce()) {
                setLayoutParameters(getDeligateLayout());
            }
        }

        protected final L getDeligateLayout() {
            if (deligate == null) {
                deligate = createDeligateLayout();
            }
            return deligate;
        }

        protected abstract L createDeligateLayout();

        @Override
        public final void initAlgo() {
            L layout = getDeligateLayout();
            setLayoutParameters(layout);
            display.setLayout(layout, isRunOnce() ? 0 : Action.INFINITY);
            setCompleted(false);
        }

        protected abstract void setLayoutParameters(L layout);

        @Override
        public final void run(double frac) {
            throw new IllegalStateException();
        }
    }
}
