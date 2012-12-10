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
import java.util.Iterator;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.layout.LayoutController;
import org.mongkie.layout.LayoutProperty;
import static org.mongkie.visualization.Config.*;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.activity.Activity;
import prefuse.activity.ActivityListener;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PrefuseLayout extends prefuse.action.layout.Layout
        implements Layout, ActivityListener, DisplayListener<MongkieDisplay> {

    protected MongkieDisplay display;
    protected LayoutProperty[] _properties;
    protected volatile boolean completed = true;
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
        if (!completed) {
            throw new IllegalStateException("Can not change the display while a layout is running");
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

    protected final boolean isSelectionOnly() {
        return supportsSelectionOnly()
                && Lookup.getDefault().lookup(LayoutController.class).getModel().isSelectionOnly();
    }

    protected final Iterator<NodeItem> getSelectedNodes() {
        return Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedNodes(display.getVisualization());
    }

    protected final TupleSet getSelectedItems() {
        return Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedItems(display.getVisualization());
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

    @Override
    public void initAlgo() {
        display.setGraphLayout(this, isRunOnce() ? 0 : Action.INFINITY);
        completed = false;
        canceled = false;
        setEnabled(!isSelectionOnly() || isEnabledOnSelectionOnly());
    }

    protected boolean isEnabledOnSelectionOnly() {
        return getSelectedNodes().hasNext();
    }

    protected TupleSet getLayoutTargetNodes() {
        return isSelectionOnly() ? getSelectedItems() : display.getVisualGraph().getNodes();
    }

    protected abstract boolean isRunOnce();

    @Override
    public final void goAlgo() {
        display.rerunLayoutAction();
        synchronized (this) {
            if (!completed) {
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
        return isEnabled() && !completed && display != null;
    }

    @Override
    public boolean cancelAlgo() {
        display.cancelLayoutAction();
        canceled = true;
        return true;
    }
    protected volatile boolean canceled;

    protected final boolean isCanceled() {
        return canceled;
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
        if (isCurrentLayout(a)) {
            completeLayout();
        }
    }

    @Override
    public void activityCancelled(Activity a) {
        if (isCurrentLayout(a)) {
            completeLayout();
        }
    }

    protected boolean isCurrentLayout(Activity layoutAction) {
        return ((ActionList) layoutAction).get(0) == this;
    }

    private void completeLayout() {
        if (!completed) {
            synchronized (this) {
                completed = true;
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

    protected boolean isBigGraph() {
        return display.getGraph().getNodeCount() > BIGGRAPH_NUMNODES
                || display.getVisualization().getVisualGroup(Visualization.AGGR_ITEMS).getTupleCount() > BIGGRAPH_NUMGROUPS;
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
        public void initAlgo() {
            L layout = getDeligateLayout();
            setLayoutParameters(layout);
            display.setGraphLayout(layout, isRunOnce() ? 0 : Action.INFINITY);
            completed = false;
            canceled = false;
            setEnabled(!isSelectionOnly() || isEnabledOnSelectionOnly());
        }

        @Override
        protected final boolean isCurrentLayout(Activity layoutAction) {
            return ((ActionList) layoutAction).get(0) == getDeligateLayout();
        }

        protected abstract void setLayoutParameters(L layout);

        @Override
        public final void run(double frac) {
            throw new IllegalStateException();
        }
    }
}
