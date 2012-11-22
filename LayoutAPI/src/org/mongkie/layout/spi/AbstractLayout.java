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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Exceptions;
import prefuse.action.Action;
import prefuse.activity.Activity;
import prefuse.activity.ActivityListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class AbstractLayout extends prefuse.action.layout.Layout
        implements Layout, ActivityListener {

    protected MongkieDisplay display;
    private LayoutProperty[] properties;
    private boolean completed;
    private LayoutBuilder<? extends Layout> builder;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected AbstractLayout(LayoutBuilder<? extends Layout> builder) {
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
    public LayoutBuilder<? extends Layout> getBuilder() {
        return builder;
    }

    @Override
    public void setDisplay(MongkieDisplay d) {
        if (display != null) {
            display.getLayoutAction().removeActivityListener(this);
        }
        d.getLayoutAction().addActivityListener(this);
        display = d;
        setVisualization(d.getVisualization());
    }

    @Override
    public LayoutProperty[] getProperties() {
        if (properties == null) {
            properties = createProperties();
        }
        return properties;
    }

    protected abstract LayoutProperty[] createProperties();

    protected boolean isCompleted() {
        return completed;
    }

    protected void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public void initAlgo() {
        display.setLayout(this, isRunOnce() ? 0 : Action.INFINITY);
        setCompleted(false);
    }

    protected abstract boolean isRunOnce();

    @Override
    public void goAlgo() {
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
    public boolean hasNextStep() {
        return !isCompleted() && display != null;
    }

    @Override
    public void cancelAlgo() {
        cancel();
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
        synchronized (this) {
            setCompleted(true);
            notifyAll();
        }
    }
}
