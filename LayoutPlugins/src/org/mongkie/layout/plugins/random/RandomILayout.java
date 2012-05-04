package org.mongkie.layout.plugins.random;

import java.util.ArrayList;
import java.util.List;
import static kobic.prefuse.Constants.NODES;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import prefuse.action.layout.RandomLayout;

/**
 *
 * @author yjjang
 */
public class RandomILayout extends PrefuseLayout<RandomILayout, RandomLayout> {

    RandomILayout(LayoutBuilder<RandomILayout> builder) {
        super(builder);
    }

    @Override
    protected RandomLayout createPrefuseLayout() {
        return new RandomLayout(NODES);
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
    protected void setLayoutParameters(RandomLayout layout) {
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
