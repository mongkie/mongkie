package org.mongkie.layout.plugins.radialtree;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import org.openide.util.Exceptions;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.data.Graph;

/**
 *
 * @author yjjang
 */
public class RadialTreeILayout extends PrefuseLayout<RadialTreeILayout, RadialTreeLayout> {

    private double radiusIncrement = 50;
    private boolean autoScale = true;

    RadialTreeILayout(LayoutBuilder<RadialTreeILayout> builder) {
        super(builder);
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
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
            properties.add(LayoutProperty.createProperty(this, boolean.class,
                    "Auto scale",
                    "Parameters",
                    "Set whether or not the layout should automatically scale itself to fit the display bounds.",
                    "isAutoScale", "setAutoScale"));
            properties.add(LayoutProperty.createProperty(this, double.class,
                    "Radius increment",
                    "Parameters",
                    "Set the radius increment to use between concentric circles. Note that this value is used only if auto-scaling is disabled.",
                    "getRadiusIncrement", "setRadiusIncrement"));
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[properties.size()]);
    }

    @Override
    public void resetPropertyValues() {
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
