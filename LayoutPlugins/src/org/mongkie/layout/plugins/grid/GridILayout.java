package org.mongkie.layout.plugins.grid;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutProperty;
import org.mongkie.layout.spi.PrefuseLayout;
import prefuse.action.layout.GridLayout;
import static kobic.prefuse.Constants.*;

/**
 *
 * @author yjjang
 */
public class GridILayout extends PrefuseLayout<GridILayout, GridLayout> {

    GridILayout(LayoutBuilder<GridILayout> builder) {
        super(builder);
    }

    @Override
    protected GridLayout createPrefuseLayout() {
        return new GridLayout(NODES);
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
    protected void setLayoutParameters(GridLayout layout) {
    }

    @Override
    protected boolean isRunOnce() {
        return true;
    }
}
