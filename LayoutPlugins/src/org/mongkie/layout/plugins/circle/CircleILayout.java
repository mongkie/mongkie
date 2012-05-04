package org.mongkie.layout.plugins.circle;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import prefuse.action.layout.CircleLayout;
import static kobic.prefuse.Constants.*;

/**
 *
 * @author yjjang
 */
public class CircleILayout extends PrefuseLayout<CircleILayout, CircleLayout> {

    CircleILayout(LayoutBuilder<CircleILayout> builder) {
        super(builder);
    }

    @Override
    protected CircleLayout createPrefuseLayout() {
        return new CircleLayout(NODES);
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertyValues() {
    }

    @Override
    protected void setLayoutParameters(CircleLayout layout) {
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
