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
package org.mongkie.layout.spi;

import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Exceptions;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.layout.Layout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @param <I>
 * @param <L> 
 */
public abstract class PrefuseLayout<I extends PrefuseLayout, L extends Layout> implements ILayout {

    private final LayoutBuilder<I> builder;
    protected Visualization v;
    protected MongkieDisplay display;
    private L prefuseLayout;
    private boolean completed;
    private final PrefuseLayoutListener prefuseListener;
    private LayoutProperty[] properties;

    private static class PrefuseLayoutListener extends ActivityAdapter {

        private final PrefuseLayout layout;

        public PrefuseLayoutListener(PrefuseLayout layout) {
            this.layout = layout;
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
            layout.setCompleted(true);
            synchronized (layout) {
                layout.notifyAll();
            }
        }
    }

    public PrefuseLayout(LayoutBuilder<I> builder) {
        this.builder = builder;
        prefuseListener = new PrefuseLayoutListener(this);
    }

    @Override
    public LayoutBuilder<I> getBuilder() {
        return builder;
    }

    @Override
    public void setDisplay(MongkieDisplay d) {
        if (display != null) {
            display.getLayoutAction().removeActivityListener(prefuseListener);
        }
        d.getLayoutAction().addActivityListener(prefuseListener);
        display = d;
        v = d.getVisualization();
        getPrefuseLayout().setVisualization(v);
    }

    @Override
    public LayoutProperty[] getProperties() {
        if (properties == null) {
            properties = createProperties();
        }
        return properties;
    }

    protected abstract LayoutProperty[] createProperties();

    protected L getPrefuseLayout() {
        if (prefuseLayout == null) {
            prefuseLayout = createPrefuseLayout();
        }
        return prefuseLayout;
    }

    protected abstract L createPrefuseLayout();

    protected abstract boolean isRunOnce();

    protected boolean isCompleted() {
        return completed;
    }

    protected void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public void initAlgo() {
        L layout = getPrefuseLayout();
        setLayoutParameters(layout);
        display.setLayout(layout, isRunOnce() ? 0 : Action.INFINITY);
        setCompleted(false);
    }

    protected abstract void setLayoutParameters(L layout);

    @Override
    public boolean canAlgo() {
        return !isCompleted() && display != null;
    }

    /**
     * Just wait until prefuse's layout activity finished.
     */
    @Override
    public void goAlgo() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public void cancel() {
        display.cancelLayout();
    }
}
