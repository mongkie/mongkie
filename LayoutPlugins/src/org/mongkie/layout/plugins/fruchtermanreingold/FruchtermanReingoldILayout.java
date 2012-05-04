package org.mongkie.layout.plugins.fruchtermanreingold;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.data.Graph;

/**
 *
 * @author yjjang
 */
public class FruchtermanReingoldILayout extends PrefuseLayout<FruchtermanReingoldILayout, FruchtermanReingoldLayout> {

    FruchtermanReingoldILayout(LayoutBuilder<FruchtermanReingoldILayout> builder) {
        super(builder);
    }

    @Override
    protected FruchtermanReingoldLayout createPrefuseLayout() {
        return new FruchtermanReingoldLayout(Graph.GRAPH);
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
    protected void setLayoutParameters(FruchtermanReingoldLayout layout) {
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
