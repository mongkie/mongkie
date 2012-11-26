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
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class AbstractLayout implements Layout {

    private final LayoutBuilder<? extends Layout> builder;
    protected MongkieDisplay display;
    protected LayoutProperty[] _properties;
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
    public void setDisplay(MongkieDisplay d) {
        this.display = d;
    }

    @Override
    public final LayoutProperty[] getProperties() {
        if (_properties == null) {
            _properties = createProperties();
        }
        return _properties;
    }

    protected abstract LayoutProperty[] createProperties();

    @Override
    public final void resetPropertyValues() {
        resetProperties();
        firePropertyChange("resetPropertyValues", null, this);
    }

    protected abstract void resetProperties();

    @Override
    public final LayoutBuilder<? extends Layout> getBuilder() {
        return builder;
    }

    @Override
    public void initAlgo() {
        prefuseLayoutEnabled = display.isLayoutActionEnabled();
        display.setLayoutActionEnabled(false);
        canceled = false;
        step = 0;
        prepare();
    }
    private boolean prefuseLayoutEnabled;
    private int step;

    protected abstract void prepare();

    @Override
    public final void goAlgo() {
        run(++step);
    }

    protected abstract void run(int step);

    @Override
    public final void endAlgo() {
        display.setLayoutActionEnabled(prefuseLayoutEnabled);
        finish();
    }

    protected abstract void finish();

    @Override
    public final boolean hasNextStep() {
        return !canceled && more();
    }

    protected abstract boolean more();

    @Override
    public final boolean cancelAlgo() {
        canceled = true;
        return true;
    }
    private volatile boolean canceled;

    protected final boolean isCanceled() {
        return canceled;
    }

    protected void setX(VisualItem item, double x) {
        PrefuseLib.setX(item, null, x);
    }

    protected void setY(VisualItem item, double y) {
        PrefuseLib.setY(item, null, y);
    }
}
