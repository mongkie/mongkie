/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.layout.plugins.radialtree;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.PrefuseLayout;
import org.openide.util.Exceptions;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RadialTree extends PrefuseLayout<RadialTreeLayout> {

    private double radiusIncrement = 50;
    private boolean autoScale = true;
    private LayoutProperty radiusIncrementProperty;

    RadialTree(LayoutBuilder<RadialTree> builder) {
        super(builder);
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        boolean o = this.autoScale;
        this.autoScale = autoScale;
        if (radiusIncrementProperty != null) {
            radiusIncrementProperty.setHidden(autoScale);
            firePropertyChange(radiusIncrementProperty.getName(), o, autoScale);
        }
    }

    public double getRadiusIncrement() {
        return radiusIncrement;
    }

    public void setRadiusIncrement(double radiusIncrement) {
        this.radiusIncrement = radiusIncrement;
    }

    @Override
    protected RadialTreeLayout createPrefuseLayout() {
        RadialTreeLayout l = new RadialTreeLayout(Graph.GRAPH);
        l.setAutoScale(autoScale);
        return l;
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty("Auto scale",
                    "Set whether or not the layout should automatically scale itself to fit the display bounds.",
                    "Parameters",
                    this, boolean.class, "isAutoScale", "setAutoScale"));
            radiusIncrementProperty = LayoutProperty.createProperty("Radius increment",
                    "Set the radius increment to use between concentric circles. Note that this value is used only if auto-scaling is disabled.",
                    "Parameters",
                    this, double.class, "getRadiusIncrement", "setRadiusIncrement");
            radiusIncrementProperty.setHidden(autoScale);
            properties.add(radiusIncrementProperty);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[properties.size()]);
    }

    @Override
    public void resetProperties() {
        setRadiusIncrement(50);
        setAutoScale(true);
    }

    @Override
    protected void setLayoutParameters(RadialTreeLayout layout) {
        layout.setAutoScale(autoScale);
        layout.setRadiusIncrement(radiusIncrement);
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
