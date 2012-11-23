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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.action.Action;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PrefuseLayout<L extends prefuse.action.layout.Layout>
        extends AbstractLayout implements PropertyChangeListener {

    private L prefuseLayout;

    public PrefuseLayout(LayoutBuilder<? extends PrefuseLayout> builder) {
        super(builder);
    }

    @Override
    public void setDisplay(MongkieDisplay d) {
        super.setDisplay(d);
        getPrefuseLayout().setVisualization(d.getVisualization());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setLayoutParameters(getPrefuseLayout());
    }

    @Override
    public LayoutProperty[] getProperties() {
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
    public void resetPropertyValues() {
        super.resetPropertyValues();
        if (!isRunOnce()) {
            setLayoutParameters(getPrefuseLayout());
        }
    }

    protected L getPrefuseLayout() {
        if (prefuseLayout == null) {
            prefuseLayout = createPrefuseLayout();
        }
        return prefuseLayout;
    }

    protected abstract L createPrefuseLayout();

    @Override
    public void initAlgo() {
        L layout = getPrefuseLayout();
        setLayoutParameters(layout);
        display.setLayout(layout, isRunOnce() ? 0 : Action.INFINITY);
        setCompleted(false);
    }

    protected abstract void setLayoutParameters(L layout);

    @Override
    public void run(double frac) {
        throw new IllegalStateException();
    }
}
